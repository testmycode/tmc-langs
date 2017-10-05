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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class QmakePlugin extends AbstractLanguagePlugin {

    private static final Path TEST_DIR = Paths.get("test");
    private static final Path TMC_TEST_RESULTS = Paths.get("tmc_test_results.xml");

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
        return basePath.resolve(new File(basePath.toFile().getName() + ".pro").toPath());
    }

    @Override
    public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName) {
        if (!isExerciseTypeCorrect(path)) {
            log.error("Failed to scan exercise due to missing qmake project file.");
            return Optional.absent();
        }

        try {
            runTests(path);
        } catch (Exception e) {
            log.error("Failed to run tests {}", e);
            return Optional.absent();
        }

        return Optional.of(parseExerciseDesc(path, exerciseName));
    }

    private ExerciseDesc parseExerciseDesc(Path testResults, String exerciseName) {
        List<TestDesc> tests = createTestDescs(testResults);
        return new ExerciseDesc(exerciseName, ImmutableList.copyOf(tests));
    }

    private List<TestDesc> createTestDescs(Path testResults) {
        List<TestDesc> tests = new ArrayList<>();
        List<String> addedTests = new ArrayList<>();

        List<TestResult> testCases = readTestResults(testResults);

        for (int i = 0; i < testCases.size(); i++) {
            TestResult testCase = testCases.get(i);

            String testClass = testCase.getName();
            String testMethod = testCase.getName();
            String testName = testClass + "." + testMethod;

            List<String> points = Arrays.asList(testCase.getMessage().split(""));

            if (!addedTests.contains(testName)) {
                tests.add(new TestDesc(testName, ImmutableList.copyOf(points)));
                addedTests.add(testName);
            }
        }

        return tests;
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

    private List<TestResult> readTestResults(Path testPath) {
        Path baseTestPath = testPath.toAbsolutePath().resolve(TEST_DIR);
        Path testResults = baseTestPath.resolve(TMC_TEST_RESULTS);

        return new QTestResultParser(testResults).getTestResults();
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
}
