package fi.helsinki.cs.tmc.langs.make;

import fi.helsinki.cs.tmc.langs.AbstractLanguagePlugin;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.Configuration;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public final class MakePlugin extends AbstractLanguagePlugin {

    private static final Path TEST_DIR = Paths.get("test");
    private static final Path AVAILABLE_POINTS = Paths.get("tmc_available_points.txt");
    private static final Path MAKEFILE = Paths.get("Makefile");
    private static final Path TMC_TEST_RESULTS = Paths.get("tmc_test_results.xml");
    private static final Path VALGRIND_LOG = Paths.get("valgrind.log");

    private static final String TEST_FAIL_MESSAGE = "Failed to run tests.";
    private static final String WRONG_EXERCISE_TYPE_MESSAGE =
            "Failed to scan exercise due to missing Makefile.";
    private static final String CANT_PARSE_EXERCISE_DESCRIPTION =
            "Couldn't parse exercise description.";
    private static final String COMPILE_FAILED_MESSAGE = "Failed to compile project.";

    private static final Logger log = LoggerFactory.getLogger(MakePlugin.class);

    private MakeUtils makeUtils;

    /**
     * Creates a new MakePlugin.
     */
    public MakePlugin() {
        super(
                new ExerciseBuilder(),
                new StudentFileAwareSubmissionProcessor(),
                new StudentFileAwareZipper(),
                new StudentFileAwareUnzipper());
        this.makeUtils = new MakeUtils();
    }

    @Override
    public String getPluginName() {
        return "make";
    }

    @Override
    public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName) {
        if (!isExerciseTypeCorrect(path)) {
            log.error(WRONG_EXERCISE_TYPE_MESSAGE);
            return Optional.absent();
        }

        try {
            runTests(path, false);
        } catch (Exception e) {
            log.error(TEST_FAIL_MESSAGE);
            log.error(e.toString());
            return Optional.absent();
        }

        final Path availablePoints =
                path.toAbsolutePath().resolve(TEST_DIR).resolve(AVAILABLE_POINTS);

        if (!Files.exists(availablePoints)) {
            log.info(CANT_PARSE_EXERCISE_DESCRIPTION);
            return Optional.absent();
        }

        return Optional.of(parseExerciseDesc(availablePoints, exerciseName));
    }

    private ExerciseDesc parseExerciseDesc(Path availablePoints, String exerciseName) {
        Scanner scanner = this.makeUtils.initFileScanner(availablePoints);
        if (scanner == null) {
            log.info(CANT_PARSE_EXERCISE_DESCRIPTION);
            return null;
        }

        Map<String, List<String>> idsToPoints = this.makeUtils.mapIdsToPoints(availablePoints);
        List<TestDesc> tests = createTestDescs(idsToPoints, scanner);

        return new ExerciseDesc(exerciseName, ImmutableList.copyOf(tests));
    }

    private List<TestDesc> createTestDescs(Map<String, List<String>> idsToPoints, Scanner scanner) {
        List<TestDesc> tests = new ArrayList<>();
        List<String> addedTests = new ArrayList<>();

        while (scanner.hasNextLine()) {
            String[] parts = this.makeUtils.rowParts(scanner);

            String testClass = parts[0];
            String testMethod = parts[1];
            String testName = testClass + "." + testMethod;

            List<String> points = idsToPoints.get(testMethod);

            if (!addedTests.contains(testName)) {
                tests.add(new TestDesc(testName, ImmutableList.copyOf(points)));
                addedTests.add(testName);
            }
        }

        return tests;
    }

    @Override
    public boolean isExerciseTypeCorrect(Path path) {
        return Files.isRegularFile(path.resolve(MAKEFILE));
    }

    /**
     * Gets a language specific {@link StudentFilePolicy}.
     *
     * <p>The project root path must be specified for the {@link StudentFilePolicy} to read
     * any configuration files such as <tt>.tmcproject.yml</tt>.
     *
     * @param projectPath The project's root path
     */
    @Override
    protected StudentFilePolicy getStudentFilePolicy(Path projectPath) {
        return new MakeStudentFilePolicy(projectPath);
    }

    @Override
    public RunResult runTests(Path path) {
        boolean withValgrind = true;

        if (!builds(path)) {
            log.info(COMPILE_FAILED_MESSAGE);
            return new RunResult(
                    RunResult.Status.COMPILE_FAILED,
                    ImmutableList.<TestResult>of(),
                    new ImmutableMap.Builder<String, byte[]>().build());
        }

        try {
            runTests(path, withValgrind);
        } catch (Exception e) {
            withValgrind = false;

            try {
                runTests(path, withValgrind);
            } catch (Exception e1) {
                log.error(e1.toString());
                throw new RuntimeException(TEST_FAIL_MESSAGE);
            }
        }

        Path baseTestPath = path.toAbsolutePath().resolve(TEST_DIR);
        Path testResults = baseTestPath.resolve(TMC_TEST_RESULTS);
        Path valgrindOutput = withValgrind ? baseTestPath.resolve(VALGRIND_LOG) : null;
        Configuration configuration = new Configuration(path);

        return new CTestResultParser(path, testResults, valgrindOutput, configuration, withValgrind)
                .result();
    }

    private void runTests(Path dir, boolean withValgrind) throws Exception {
        String target = withValgrind ? "run-test-with-valgrind" : "run-test";
        String[] command = {"make", target};

        log.info("Running tests with command {0}", new Object[] {Arrays.deepToString(command)});

        ProcessRunner runner = new ProcessRunner(command, dir);
        ProcessResult result = runner.call();
        if (result.statusCode != 0) {
            log.warn(result.errorOutput);
            throw new IllegalArgumentException(result.errorOutput);
        }
    }

    @Override
    public ValidationResult checkCodeStyle(Path path) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private boolean builds(Path dir) {
        String[] command = {"make", "test"};
        ProcessRunner runner = new ProcessRunner(command, dir);

        try {
            ProcessResult result = runner.call();
            int ret = result.statusCode;
            if (ret != 0) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
