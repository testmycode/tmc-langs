package fi.helsinki.cs.tmc.langs.utils;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.langs.TestDesc;
import fi.helsinki.cs.tmc.langs.TestResult;
import fi.helsinki.cs.tmc.testrunner.TestCase;
import fi.helsinki.cs.tmc.testrunner.TestCaseList;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestResultParser {

    /**
     * Parse tmc-testrunner output file for RunResult information.
     *
     * @param resultsFile to be parsed.
     * @return RunResult object containing information about the tests.
     */
    public RunResult parseTestResult(File resultsFile) {
        try {
            return parseTestResult(FileUtils.readFileToString(resultsFile, "UTF-8"));
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public RunResult parseTestResult(String resultsJson) {
        List<TestResult> testResults = new ArrayList<>();
        Map<String, byte[]> logs = new HashMap<>();

        TestCaseList testCaseRecords = new Gson().fromJson(resultsJson, TestCaseList.class);
        boolean passed = true;

        for (TestCase tc : testCaseRecords) {
            testResults.add(convertTestCaseResult(tc));

            if (tc.status == TestCase.Status.FAILED) {
                passed = false;
            }
        }

        RunResult.Status status = passed ? RunResult.Status.PASSED : RunResult.Status.TESTS_FAILED;

        return new RunResult(status, ImmutableList.copyOf(testResults), ImmutableMap.copyOf(logs));
    }

    private TestResult convertTestCaseResult(TestCase testCase) {
        List<String> exception = new ArrayList<>();
        List<String> points = new ArrayList<>();

        if (testCase.exception != null) {
            for (StackTraceElement stackTrace : testCase.exception.stackTrace) {
                exception.add(stackTrace.toString());
            }
        }

        for (String point : testCase.pointNames) {
            points.add(point);
        }

        String name = testCase.className + " " + testCase.methodName;
        boolean passed = testCase.status == TestCase.Status.PASSED;
        String message = testCase.message == null ? "" : testCase.message;

        return new TestResult(name, passed, ImmutableList.copyOf(points), message, ImmutableList.copyOf(exception));
    }

    /**
     * Parse and convert tmc-testscanner output into ExerciseDescription.
     *
     * @param output Output from the tmc-testscanner.
     * @param exerciseName The name of the exercise.
     * @return Parsed exercise description.
     */
    public ExerciseDesc parseScannerOutput(String output, String exerciseName) {
        List<TestDesc> tests = new ArrayList<>();
        JsonElement data = new JsonParser().parse(output);

        for (JsonElement test : data.getAsJsonArray()) {
            String testName = parseTestName(test);
            JsonArray points = test.getAsJsonObject().get("points").getAsJsonArray();
            tests.add(generateTestDesc(testName, points));
        }

        return new ExerciseDesc(exerciseName, ImmutableList.<TestDesc>copyOf(tests));
    }

    /**
     * Parse and return the name of the test from the JsonElement.
     *
     * @param test JsonElement containing test information.
     * @return Name of the test.
     */
    private String parseTestName(JsonElement test) {
        String testName = test.getAsJsonObject().get("className").toString();
        testName = testName.substring(1, testName.length() - 1);
        return testName + " " + test.getAsJsonObject().get("methodName").getAsString();
    }

    /**
     * Generate TestDesc from the given JsonArray and name. Assigns the given name as
     * the name of the test description.
     *
     * @param name to be given to the description.
     * @param pointsArray JsonArray containing test points information.
     * @return TestDesc object.
     */
    private TestDesc generateTestDesc(String name, JsonArray pointsArray) {
        List<String> points = new ArrayList<>();

        for (int i = 0; i < pointsArray.size(); i++) {
            points.add(pointsArray.get(i).getAsString());
        }

        ImmutableList<String> immutablePoints = ImmutableList.copyOf(points);
        return new TestDesc(name, immutablePoints);
    }
}
