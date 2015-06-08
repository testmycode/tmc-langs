package fi.helsinki.cs.tmc.langs.make;

import fi.helsinki.cs.tmc.langs.AbstractLanguagePlugin;
import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.langs.TestDesc;
import fi.helsinki.cs.tmc.langs.TestResult;
import fi.helsinki.cs.tmc.langs.utils.ProcessResult;
import fi.helsinki.cs.tmc.langs.utils.ProcessRunner;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MakePlugin extends AbstractLanguagePlugin {

    private static final Logger log = Logger.getLogger(MakePlugin.class.getName());

    private final String testDir = File.separatorChar + "test";

    @Override
    public String getLanguageName() {
        return "make";
    }

    @Override
    public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName) {
        if (!isExerciseTypeCorrect(path)) {
            return Optional.absent();
        }

        final File projectDir = new File(String.valueOf(path));
        try {
            runTests(projectDir, false);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.absent();
        }

        final File availablePoints = new File(projectDir.getAbsolutePath() + File.separatorChar
            + "test" + File.separatorChar + "tmc_available_points.txt");

        if (!availablePoints.exists()) {
            return Optional.absent();
        }

        return Optional.of(parseExerciseDesc(availablePoints));
    }

    private ExerciseDesc parseExerciseDesc(File availablePoints) {
        Scanner scanner;
        try {
            scanner = new Scanner(availablePoints);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        Map<String, List<String>> idsToPoints = mapIdsToPoints(availablePoints);
        List<TestDesc> tests = new ArrayList<>();
        List<String> addedTests = new ArrayList<>();

        while (scanner.hasNextLine()) {
            String row = scanner.nextLine();
            String[] parts = row.split("\\[|\\]| ");

            String testClass = parts[1];
            String testMethod = parts[parts.length - 3];
            String testName = testClass + "." + testMethod;

            List<String> points = idsToPoints.get(testMethod);

            if (!addedTests.contains(testName)) {
                tests.add(new TestDesc(testName, ImmutableList.copyOf(points)));
                addedTests.add(testName);
            }
        }

        String exerciseName = parseExerciseName(availablePoints);

        return new ExerciseDesc(exerciseName, ImmutableList.copyOf(tests));
    }

    private String parseExerciseName(File availablePoints) {
        String[] pathParts = availablePoints.getAbsolutePath().split(File.separatorChar + "");
        String name = pathParts[pathParts.length - 3];
        return name;
    }

    private Map<String, List<String>> mapIdsToPoints(File availablePoints) {
        Scanner scanner;
        try {
            scanner = new Scanner(availablePoints);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new HashMap<>();
        }

        Map<String, List<String>> idsToPoints = new HashMap<>();
        while (scanner.hasNextLine()) {
            String row = scanner.nextLine();
            String[] parts = row.split("\\[|\\]| ");

            String key = parts[parts.length - 3];
            String value = parts[parts.length - 1];
            addPointsToId(idsToPoints, key, value);
        }

        return idsToPoints;
    }

    private void addPointsToId(Map<String, List<String>> idsToPoints, String key, String value) {
        if (!idsToPoints.containsKey(key)) {
            idsToPoints.put(key, new ArrayList<String>());
        }
        idsToPoints.get(key).add(value);
    }


    @Override
    protected boolean isExerciseTypeCorrect(Path path) {
        return new File(path.toString() + File.separatorChar + "Makefile").exists();
    }

    @Override
    public RunResult runTests(Path path) {
        final File projectDir = new File(String.valueOf(path));
        boolean withValgrind = true;

        if (!builds(projectDir)) {
            return new RunResult(RunResult.Status.COMPILE_FAILED,
                ImmutableList.copyOf(new ArrayList<TestResult>()),
                new ImmutableMap.Builder<String, byte[]>().build());
        }

        try {
            runTests(projectDir, withValgrind);
        } catch (Exception e) {
            withValgrind = false;

            try {
                runTests(projectDir, withValgrind);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        File valgrindLog = withValgrind ? new File(projectDir.getAbsolutePath() + File.separatorChar
            + "test" + File.separatorChar + "valgrind.log") : null;
        File resultsFile = new File(projectDir.getAbsolutePath() + File.separatorChar + "test"
            + File.separatorChar + "tmc_test_results.xml");

        log.info("Locating exercise");

        return new CTestResultParser(resultsFile, valgrindLog, projectDir).result();
    }

    private void runTests(File dir, boolean withValgrind) throws Exception {
        String[] command;

        String target = withValgrind ? "run-test-with-valgrind" : "run-test";
        command = new String[]{"make", target};

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
        String[] command;
        command = new String[]{"make", "test"};
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
