package fi.helsinki.cs.tmc.langs.make;

import fi.helsinki.cs.tmc.langs.AbstractLanguagePlugin;
import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.langs.TestDesc;
import fi.helsinki.cs.tmc.langs.TestResult;
import fi.helsinki.cs.tmc.langs.sandbox.SubmissionProcessor;
import fi.helsinki.cs.tmc.langs.utils.ProcessResult;
import fi.helsinki.cs.tmc.langs.utils.ProcessRunner;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class MakePlugin extends AbstractLanguagePlugin {

    private static final Path TEST_DIR = Paths.get("test");
    private static final Path AVAILABLE_POINTS = Paths.get("tmc_available_points.txt");
    private static final Path MAKEFILE = Paths.get("Makefile");
    private static final Path TMC_TEST_RESULTS = Paths.get("tmc_test_results.xml");
    private static final Path VALGRIND_LOG = Paths.get("valgrind.log");

    private static final String TEST_FAIL_MESSAGE = "Failed to run tests.";

    private static final Logger log = LoggerFactory.getLogger(MakePlugin.class);

    private MakeUtils makeUtils;

    public MakePlugin() {
        super(new SubmissionProcessor(new MakeFileMovingPolicy()));
        this.makeUtils = new MakeUtils();
    }

    @Override
    public String getLanguageName() {
        return "make";
    }

    @Override
    public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName) {
        if (!isExerciseTypeCorrect(path)) {
            return Optional.absent();
        }
        try {
            runTests(path, false);
        } catch (Exception e) {
            log.error(e.toString());
            return Optional.absent();
        }

        final Path availablePoints = path.toAbsolutePath().resolve(TEST_DIR).resolve(AVAILABLE_POINTS);

        if (!Files.exists(availablePoints)) {
            return Optional.absent();
        }

        return Optional.of(parseExerciseDesc(availablePoints));
    }

    private ExerciseDesc parseExerciseDesc(Path availablePoints) {
        Scanner scanner = this.makeUtils.initFileScanner(availablePoints.toFile());
        if (scanner == null) {
            return null;
        }

        Map<String, List<String>> idsToPoints = this.makeUtils.mapIdsToPoints(availablePoints.toFile());
        List<TestDesc> tests = createTestDescs(idsToPoints, scanner);

        String exerciseName = parseExerciseName(availablePoints);

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

    private String parseExerciseName(Path availablePoints) {
        return availablePoints.getParent().getParent().getFileName().toString();
    }

    @Override
    protected boolean isExerciseTypeCorrect(Path path) {
        return Files.exists(path.resolve(MAKEFILE));
    }

    @Override
    public RunResult runTests(Path path) {
        boolean withValgrind = true;

        if (!builds(path)) {
            return new RunResult(RunResult.Status.COMPILE_FAILED,
                ImmutableList.<TestResult>of(), new ImmutableMap.Builder<String, byte[]>().build());
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
        File valgrindOutput = withValgrind ? baseTestPath.resolve(VALGRIND_LOG).toFile() : null;

        log.info("Locating exercise");

        return new CTestResultParser(path.toFile(), testResults.toFile(), valgrindOutput).result();
    }

    private void runTests(Path dir, boolean withValgrind) throws Exception {
        String target = withValgrind ? "run-test-with-valgrind" : "run-test";
        String[] command = new String[]{"make", target};

        log.info("Running tests with command {0}",
                new Object[]{Arrays.deepToString(command)});

        ProcessRunner runner = new ProcessRunner(command, dir);

        runner.call();
    }

    @Override
    public ValidationResult checkCodeStyle(Path path) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private boolean builds(Path dir) {
        String[] command = new String[]{"make", "test"};
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
