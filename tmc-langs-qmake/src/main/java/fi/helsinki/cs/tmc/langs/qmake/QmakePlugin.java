package fi.helsinki.cs.tmc.langs.qmake;

import fi.helsinki.cs.tmc.langs.AbstractLanguagePlugin;
import fi.helsinki.cs.tmc.langs.abstraction.Strategy;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationError;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.ExerciseBuilder;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
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
import fi.helsinki.cs.tmc.langs.domain.RunResult.Status;
import fi.helsinki.cs.tmc.langs.domain.SpecialLogs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class QmakePlugin extends AbstractLanguagePlugin {

    private static final Path TMC_TEST_RESULTS = Paths.get("tmc_test_results.xml");

    // POINT(exercise_name, 1)
    private static final Pattern POINT_PATTERN
            = Pattern.compile("POINT\\(\\s*(\\w+),\\s*(\\w+)\\s*\\)\\s*;");
    private static final Pattern COMMENT_PATTERN
            = Pattern.compile("(^[^\"\\r\\n]*\\/\\*{1,2}.*?\\*\\/|(^[^\"\\r\\n]*\\/\\/[^\\r\\n]*))", Pattern.MULTILINE | Pattern.DOTALL);

    private static final RunResult EMPTY_FAILURE
            = new RunResult(
                    Status.COMPILE_FAILED,
                    ImmutableList.<TestResult>of(),
                    new ImmutableMap.Builder<String, byte[]>().build());

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
     * Resolve the excercise .pro file from excercise directory. The file should
     * be named after the directory.
     */
    private Path getProFile(Path basePath) {
        Path proFile = new File(basePath.toFile().getName() + ".pro").toPath();
        return basePath.resolve(proFile);
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
        return Files.isRegularFile(getProFile(path));
    }

    /**
     * Gets a language specific {@link StudentFilePolicy}.
     *
     * <p>
     * The project root path must be specified for the {@link StudentFilePolicy}
     * to read any configuration files such as <tt>.tmcproject.yml</tt>.
     *
     * @param projectPath The project's root path
     */
    @Override
    protected StudentFilePolicy getStudentFilePolicy(Path projectPath) {
        return new QmakeStudentFilePolicy(projectPath);
    }

    @Override
    public RunResult runTests(Path path) {
        Optional<RunResult> result = build(path);
        if (result.isPresent()) {
            return result.get();
        }

        Path testResults = path.resolve(TMC_TEST_RESULTS);

        String target = "check";
        String config = "TESTARGS=-o " + testResults.toString() + ",xml";
        String[] makeCommand = {"make", target, config};

        log.info("Testing project with command {}", Arrays.toString(makeCommand));

        Optional<ProcessResult> test = run(makeCommand, path);
        if (test.isPresent()) {
            if (!Files.exists(testResults)) {
                log.error("Failed to get test output at {}", testResults);
                return filledFailure(test.get());
            }

            return new QTestResultParser(testResults).result();
        }

        return EMPTY_FAILURE;
    }

    private Optional<RunResult> build(Path path) {
        Optional<RunResult> result = buildWithQmake(path);
        if (result.isPresent()) {
            log.warn("Failed to compile project with qmake");
            return result;
        }

        result = buildWithMake(path);
        if (result.isPresent()) {
            log.warn("Failed to compile project with make");
            return result;
        }

        return Optional.absent();
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

    private Optional<RunResult> buildWithQmake(Path dir) {
        String qmakeArguments = "CONFIG+=test";
        Path pro = getProFile(dir);
        String[] qmakeCommand = {"qmake", qmakeArguments, pro.toString()};

        log.info("Building project with command {}", Arrays.deepToString(qmakeCommand));
        Optional<ProcessResult> result = run(qmakeCommand, dir);
        return checkBuildResult(result);
    }

    private Optional<RunResult> buildWithMake(Path dir) {
        String[] makeCommand = {"make"};
        log.info("Building project with command {}", Arrays.deepToString(makeCommand));
        Optional<ProcessResult> result = run(makeCommand, dir);
        return checkBuildResult(result);
    }

    private Optional<RunResult> checkBuildResult(Optional<ProcessResult> result) {
        if (result.isPresent()) {
            if (result.get().statusCode == 0) {
                return Optional.absent();
            }
            return Optional.of(filledFailure(result.get()));
        }
        return Optional.of(EMPTY_FAILURE);
    }

    @Override
    public void clean(Path path) {
        String[] command = {"make", "clean"};
        if (run(command, path).isPresent()) {
            log.info("Cleaned project");
        } else {
            log.warn("Cleaning project was not successful");

        }
    }

    private Optional<ProcessResult> run(String[] command, Path dir) {
        ProcessRunner runner = new ProcessRunner(command, dir);

        try {
            return Optional.of(runner.call());
        } catch (Exception e) {
            log.error("Running command {} failed {}", Arrays.deepToString(command), e);
        }

        return Optional.absent();
    }

    private RunResult filledFailure(ProcessResult processResult) {
        byte[] errorOutput = processResult.errorOutput.getBytes(StandardCharsets.UTF_8);
        ImmutableMap<String, byte[]> logs
                = new ImmutableMap.Builder()
                .put(SpecialLogs.COMPILER_OUTPUT, errorOutput)
                .<String, byte[]>build();
        return new RunResult(Status.COMPILE_FAILED, ImmutableList.<TestResult>of(), logs);
    }

    /**
     * TODO: This is copy paste from tmc-langs regex branch. Regex branch method
     * 'availablePoints' does not parse test names at this time.
     *
     * POINT_PATTERN matches the #define macro in the tests, for example:
     *
     * POINT(test_name2, suite_point);
     *
     * POINT(test_name, 1);
     *
     * etc.
     */
    public Optional<ImmutableList<TestDesc>> availablePoints(final Path rootPath) {
        return findAvailablePoints(rootPath, POINT_PATTERN, COMMENT_PATTERN, ".cpp");
    }

    protected Optional<ImmutableList<TestDesc>> findAvailablePoints(final Path rootPath,
            Pattern pattern, Pattern commentPattern, String suffix) {
        Map<String, List<String>> tests = new HashMap<>();
        List<TestDesc> descs = new ArrayList<>();
        try {
            final List<Path> potentialPointFiles = getPotentialPointFiles(rootPath, suffix);
            for (Path p : potentialPointFiles) {

                String contents = new String(Files.readAllBytes(p), "UTF-8");
                String withoutComments = commentPattern.matcher(contents).replaceAll("");
                Matcher matcher = pattern.matcher(withoutComments);
                while (matcher.find()) {
                    MatchResult matchResult = matcher.toMatchResult();
                    String testName = matchResult.group(1).trim();
                    String point = matchResult.group(2).trim();
                    tests.putIfAbsent(testName, new ArrayList<String>());
                    tests.get(testName).add(point);
                }
            }
            for (String testName : tests.keySet()) {
                List<String> points = tests.getOrDefault(testName, new ArrayList<String>());
                descs.add(new TestDesc(testName, ImmutableList.<String>copyOf(points)));
            }
            return Optional.of(ImmutableList.copyOf(descs));
        } catch (IOException e) {
            // We could scan the exercise but that could be a security risk
            return Optional.absent();
        }
    }

    private List<Path> getPotentialPointFiles(final Path rootPath, final String suffix)
            throws IOException {
        final StudentFilePolicy studentFilePolicy = getStudentFilePolicy(rootPath);
        final List<Path> potentialPointFiles = new ArrayList<>();

        Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs)
                    throws IOException {
                if (studentFilePolicy.isStudentFile(path, rootPath)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                if (!Files.isDirectory(path) && path.toString().endsWith(suffix)) {
                    potentialPointFiles.add(path);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return potentialPointFiles;
    }
}
