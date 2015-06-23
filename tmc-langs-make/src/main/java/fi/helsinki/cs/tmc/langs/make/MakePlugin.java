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

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MakePlugin extends AbstractLanguagePlugin {

    private static final String TEST_DIR = File.separatorChar + "test";
    private static final String AVAILABLE_POINTS = File.separatorChar + "tmc_available_points.txt";
    private static final String MAKEFILE = File.separatorChar + "Makefile";
    private static final String TEST_FAIL_MESSAGE = "Failed to run tests.";
    private static final String TMC_TEST_RESULTS = File.separatorChar + "tmc_test_results.xml";
    private static final String VALGRIND_LOG = File.separatorChar + "valgrind.log";

    private static final Logger log = Logger.getLogger(MakePlugin.class.getName());

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

        final File projectDir = path.toFile();

        try {
            runTests(projectDir, false);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.absent();
        }

        final File availablePoints = new File(projectDir.getAbsolutePath() + TEST_DIR
            + AVAILABLE_POINTS);

        if (!availablePoints.exists()) {
            return Optional.absent();
        }

        return Optional.of(parseExerciseDesc(availablePoints));
    }

    private ExerciseDesc parseExerciseDesc(File availablePoints) {
        Scanner scanner = this.makeUtils.initFileScanner(availablePoints);
        if (scanner == null) {
            return null;
        }

        Map<String, List<String>> idsToPoints = this.makeUtils.mapIdsToPoints(availablePoints);
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

    private String parseExerciseName(File availablePoints) {
        String[] pathParts = availablePoints.getAbsolutePath().split(File.separatorChar + "");
        return pathParts[pathParts.length - 3];
    }

    @Override
    protected boolean isExerciseTypeCorrect(Path path) {
        File makefile;
        try {
            makefile = new File(path.toString() + MAKEFILE);
        } catch (Exception e) {
            return false;
        }
        return makefile.exists();
    }

    @Override
    public RunResult runTests(Path path) {
        final File projectDir = path.toFile();
        boolean withValgrind = true;

        if (!builds(projectDir)) {
            return new RunResult(RunResult.Status.COMPILE_FAILED,
                ImmutableList.<TestResult>of(), new ImmutableMap.Builder<String, byte[]>().build());
        }

        try {
            runTests(projectDir, withValgrind);
        } catch (Exception e) {
            withValgrind = false;

            try {
                runTests(projectDir, withValgrind);
            } catch (Exception e1) {
                e1.printStackTrace();
                throw new RuntimeException(TEST_FAIL_MESSAGE);
            }
        }

        String baseTestPath = projectDir.getAbsolutePath() + TEST_DIR;
        File testResults = new File(baseTestPath + TMC_TEST_RESULTS);
        File valgrindOutput = withValgrind ? new File(baseTestPath + VALGRIND_LOG) : null;

        log.info("Locating exercise");

        return new CTestResultParser(projectDir, testResults, valgrindOutput).result();
    }

    private void runTests(File dir, boolean withValgrind) throws Exception {
        String target = withValgrind ? "run-test-with-valgrind" : "run-test";
        String[] command = new String[]{"make", target};

        log.log(Level.INFO, "Running tests with command {0}",
                new Object[]{Arrays.deepToString(command)});

        ProcessRunner runner = new ProcessRunner(command, dir);

        runner.call();
    }

    @Override
    public ValidationResult checkCodeStyle(Path path) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private boolean builds(File dir) {
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
