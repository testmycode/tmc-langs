package fi.helsinki.cs.tmc.langs.qmake;

import fi.helsinki.cs.tmc.langs.AbstractLanguagePlugin;
import fi.helsinki.cs.tmc.langs.abstraction.Strategy;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationError;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.ExerciseBuilder;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.ExercisePackagingConfiguration;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.RunResult.Status;
import fi.helsinki.cs.tmc.langs.domain.SpecialLogs;
import fi.helsinki.cs.tmc.langs.domain.TestDesc;
import fi.helsinki.cs.tmc.langs.domain.TestResult;
import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.io.sandbox.StudentFileAwareSubmissionProcessor;
import fi.helsinki.cs.tmc.langs.io.zip.StudentFileAwareUnzipper;
import fi.helsinki.cs.tmc.langs.io.zip.StudentFileAwareZipper;
import fi.helsinki.cs.tmc.langs.utils.ProcessResult;
import fi.helsinki.cs.tmc.langs.utils.ProcessRunner;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class QmakePlugin extends AbstractLanguagePlugin {

    private static final Path TMC_TEST_RESULTS = Paths.get("tmc_test_results.xml");

    // Finds pattern 'POINT(exercise_name, 1.1)'
    private static final Pattern QTEST_POINT_PATTERN
            = Pattern.compile("POINT\\(\\s*"
            + "(?<testName>\\w+),\\s*"
            + "(?<point>[^\\s|\\)]+)\\s*\\)\\s*;");

    // Finds pattern 'quickPOINT("testclass::exercise_name", "1.1")'
    private static final Pattern QUICKTEST_POINT_PATTERN
            = Pattern.compile("quickPOINT\\(\\s*"
            + "\"(?<testName>.+)\",\\s*"
            + "\"(?<point>[^\\s|\\)]+)\"\\s*\\)\\s*;");

    // Pattern to find comments
    private static final Pattern COMMENT_PATTERN
            = Pattern.compile("(^[^\"\\r\\n]*\\/\\*{1,2}.*?\\*\\/"
                    + "|(^[^\"\\r\\n]*\\/\\/[^\\r\\n]*))", Pattern.MULTILINE | Pattern.DOTALL);

    private static final Logger log = LoggerFactory.getLogger(QmakePlugin.class);

    /**
     * Creates a new QmakePlugin.
     */
    public QmakePlugin() {
        super(
                new ExerciseBuilder(),
                new StudentFileAwareSubmissionProcessor(),
                new StudentFileAwareZipper(),
                new StudentFileAwareUnzipper());
    }

    @Override
    public String getPluginName() {
        return "qmake";
    }

    /**
     * Resolve an exercise .pro file from an exercise directory.
     *
     * @throws IOException if .pro file is not found in basePath
     */
    private Path getProFile(Path basePath) throws IOException {
        List<Path> matchingFiles = Files.walk(basePath, 1)
                .filter(f -> f.toString().endsWith(".pro"))
                .sorted(Comparator.comparing(a -> a.getFileName().toString()))
                .collect(Collectors.toList());

        if (matchingFiles.isEmpty()) {
            throw new IOException("Could not find .pro file!");
        }

        Path proFile = matchingFiles.get(0);

        if (matchingFiles.size() > 1) {
            log.warn("Found multiple .pro files: {}", matchingFiles);
            log.warn("Chose {}", proFile);
        }

        return proFile;
    }

    @Override
    public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName) {
        if (!isExerciseTypeCorrect(path)) {
            log.error("Failed to scan exercise due to missing qmake project file.");
            return Optional.absent();
        }

        Optional<ImmutableList<TestDesc>> tests = availablePoints(path);
        if (!tests.isPresent()) {
            log.error("Failed to scan exercise due to parsing error.");
            return Optional.absent();
        }

        return Optional.of(new ExerciseDesc(exerciseName, ImmutableList.copyOf(tests.get())));
    }

    @Override
    public boolean isExerciseTypeCorrect(Path path) {
        try {
            return Files.isRegularFile(getProFile(path));
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Gets a language specific {@link StudentFilePolicy}.
     *
     * <p>
     * The project root path must be specified for the {@link StudentFilePolicy}
     * to read any configuration files such as <tt>.tmcproject.yml</tt>.
     * </p>
     *
     * @param projectPath The project's root path
     */
    @Override
    protected StudentFilePolicy getStudentFilePolicy(Path projectPath) {
        return new QmakeStudentFilePolicy(projectPath);
    }

    @Override
    public RunResult runTests(Path path) {
        Path fullPath;
        try {
            fullPath = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        } catch (IOException e) {
            log.error("Exercise directory not found", e);
            return filledFailure(Status.GENERIC_ERROR, "Exercise directory not found");
        }

        Path shadowDir;
        try {
            shadowDir = makeShadowBuildDir(fullPath);
        } catch (IOException e) {
            log.error("Preparing exercise failed", e);
            return filledFailure(Status.GENERIC_ERROR, "Could not create build directory");
        }

        try {
            ProcessResult qmakeBuild = buildWithQmake(shadowDir);
            if (qmakeBuild.statusCode != 0) {
                log.error("Building project with qmake failed: {}", qmakeBuild.errorOutput);
                return filledFailure(Status.COMPILE_FAILED, qmakeBuild.errorOutput);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Building project with qmake failed", e);
            return filledFailure(Status.GENERIC_ERROR, "Building project with qmake failed");
        }

        try {
            ProcessResult makeBuild = buildWithMake(shadowDir);
            if (makeBuild.statusCode != 0) {
                log.error("Building project with make failed: {}", makeBuild.errorOutput);
                return filledFailure(Status.COMPILE_FAILED, makeBuild.errorOutput);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Building project with make failed", e);
            return filledFailure(Status.GENERIC_ERROR, "Building project with make failed");
        }

        Path testResults = shadowDir.resolve(TMC_TEST_RESULTS);

        String target = "check";
        String config = "TESTARGS=-o " + testResults.toString() + ",xml";
        String[] makeCommand = {"make", target, config};

        log.info("Testing project with command {}", Arrays.toString(makeCommand));

        try {
            ProcessResult testRun = run(makeCommand, shadowDir);

            if (!Files.exists(testResults)) {
                log.error("Failed to get test output at {}", testResults);
                log.error("Test stdout\n {}", testRun.output);
                log.error("Test stderr\n {}", testRun.errorOutput);
                return filledFailure(Status.GENERIC_ERROR, testRun.output);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Testing with make check failed", e);
            return filledFailure(Status.GENERIC_ERROR, "Testing with make check failed");
        }

        QTestResultParser parser = new QTestResultParser();
        parser.loadTests(testResults);
        return parser.result();
    }

    @Override
    public ValidationResult checkCodeStyle(Path path, Locale locale) {
        return new ValidationResult() {
            @Override
            public Strategy getStrategy() {
                return Strategy.DISABLED;
            }

            @Override
            public Map<File, List<ValidationError>> getValidationErrors() {
                return Maps.newHashMap();
            }
        };
    }

    private Path makeShadowBuildDir(Path dir) throws IOException {
        Path shadowPath = dir.resolve("build");
        if (Files.exists(shadowPath)) {
            log.info("Shadow dir already exists at {}", shadowPath);
            return shadowPath;
        }

        File buildDir = shadowPath.toFile();

        log.info("Making shadow build dir to {}", buildDir.toPath());
        if (!buildDir.mkdirs()) {
            throw new IOException(
                    "Unable to create shadow build directory: "
                    + buildDir.toPath());
        }

        return buildDir.toPath();
    }

    private ProcessResult buildWithQmake(Path dir) throws IOException, InterruptedException {
        String qmakeArguments = "CONFIG+=test";
        Path pro = getProFile(dir.getParent());
        String[] qmakeCommand = {"qmake", qmakeArguments, pro.toString()};

        log.info("Building project with command {}", Arrays.deepToString(qmakeCommand));
        return run(qmakeCommand, dir);
    }

    private ProcessResult buildWithMake(Path dir) throws IOException, InterruptedException {
        String[] makeCommand = {"make"};
        log.info("Building project with command {}", Arrays.deepToString(makeCommand));
        return run(makeCommand, dir);
    }

    @Override
    public void clean(Path path) {
        String[] command = {"make", "clean"};
        try {
            ProcessResult result = run(command, path);
            if (result.statusCode != 0) {
                log.error("Cleaning project was not successful", result.errorOutput);
            }
            log.info("Cleaned project");
        } catch (Exception e) {
            log.error("Cleaning project was not successful", e);
        }
    }

    private ProcessResult run(String[] command, Path dir) throws IOException, InterruptedException {
        ProcessRunner runner = new ProcessRunner(command, dir);
        return runner.call();
    }

    private RunResult filledFailure(Status status, String output) {
        byte[] errorOutput = output.getBytes(StandardCharsets.UTF_8);
        ImmutableMap<String, byte[]> logs
                = new ImmutableMap.Builder<String, byte[]>()
                .put(SpecialLogs.COMPILER_OUTPUT, errorOutput)
                .<String, byte[]>build();
        return new RunResult(status, ImmutableList.<TestResult>of(), logs);
    }

    /**
     * Parses available points from test source files (.cpp and .qml)
     * <p>
     * QTEST_POINT_PATTERN matches the #define macro in the QTests, for example:
     *      POINT(test_name2, suite_point);
     *      POINT(test_name, 1);
     * </p>
     * <p>QUICKTEST_POINT_PATTERN matches the QML js syntax: </p>
     * <p>
     *      qmlPOINT("test_name", "1.1");
     *      qmlPOINT("test_name2", "point");
     * </p>
     */
    public Optional<ImmutableList<TestDesc>> availablePoints(final Path rootPath) {
        ImmutableList<Pattern> pointPatterns =
                ImmutableList.of(QTEST_POINT_PATTERN, QUICKTEST_POINT_PATTERN);
        ImmutableList<String> fileSuffixes = ImmutableList.of(".cpp", ".qml");
        return findAvailablePoints(rootPath, pointPatterns, COMMENT_PATTERN, fileSuffixes);
    }

    protected Optional<ImmutableList<TestDesc>> findAvailablePoints(final Path rootPath,
            List<Pattern> patterns, Pattern commentPattern, List<String> suffix) {
        Map<String, List<String>> tests = new HashMap<>();
        List<TestDesc> descs = new ArrayList<>();
        try {
            final List<Path> potentialPointFiles = getPotentialPointFiles(rootPath, suffix);
            for (Path p : potentialPointFiles) {

                String contents = new String(Files.readAllBytes(p), "UTF-8");
                String withoutComments = commentPattern.matcher(contents).replaceAll("");
                for (Pattern pointPattern : patterns) {
                    Matcher matcher = pointPattern.matcher(withoutComments);
                    while (matcher.find()) {
                        String testName = matcher.group("testName").trim();
                        String point = matcher.group("point").trim();
                        tests.putIfAbsent(testName, new ArrayList<String>());
                        tests.get(testName).add(point);
                    }
                }
            }
            for (String testName : tests.keySet()) {
                List<String> points = tests.getOrDefault(testName, new ArrayList<String>());
                descs.add(new TestDesc(testName, ImmutableList.<String>copyOf(points)));
            }
            return Optional.of(ImmutableList.copyOf(descs));
        } catch (IOException e) {
            return Optional.absent();
        }
    }

    private List<Path> getPotentialPointFiles(final Path rootPath, List<String> suffixes)
            throws IOException {
        final StudentFilePolicy studentFilePolicy = getStudentFilePolicy(rootPath);
        final List<Path> potentialPointFiles = new ArrayList<>();

        Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                if (studentFilePolicy.isStudentFile(path, rootPath)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }

                if (Files.isDirectory(path)) {
                    return FileVisitResult.CONTINUE;
                }

                for (String suf : suffixes) {
                    if (path.toString().endsWith(suf)) {
                        potentialPointFiles.add(path);
                    }
                }

                return FileVisitResult.CONTINUE;
            }
        });

        return potentialPointFiles;
    }

    @Override
    protected ImmutableList<String> getDefaultExerciseFilePaths() {
        return ImmutableList.of("test_runner", "test");
    }

    @Override
    protected ImmutableList<String> getDefaultStudentFilePaths() {
        return ImmutableList.of("src");
    }
}
