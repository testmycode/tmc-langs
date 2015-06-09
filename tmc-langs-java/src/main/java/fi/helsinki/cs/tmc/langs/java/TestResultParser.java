package fi.helsinki.cs.tmc.langs.java;

import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.langs.TestResult;
import fi.helsinki.cs.tmc.langs.java.testrunner.TestCase;
import fi.helsinki.cs.tmc.langs.java.testrunner.TestCaseList;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

    /**
     * Parse run results from a JSON string.
     *
     * @param resultsJson   A JSON representation of the test results.
     * @return              Parsed RunResult
     */
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

        Collections.addAll(points, testCase.pointNames);

        String name = testCase.className + " " + testCase.methodName;
        boolean passed = testCase.status == TestCase.Status.PASSED;
        String message = testCase.message == null ? "" : testCase.message;

        return new TestResult(name,
                             passed,
                             ImmutableList.copyOf(points),
                             message,
                             ImmutableList.copyOf(exception));
    }
}
