package fi.helsinki.cs.tmc.langs.java;

import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.TestResult;
import fi.helsinki.cs.tmc.testrunner.TestCaseList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import org.apache.commons.io.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TestResultParser {

    private static final Logger log = LoggerFactory.getLogger(TestResultParser.class);

    /**
     * Parse tmc-testrunner output file for RunResult information.
     *
     * @param resultsFile to be parsed.
     * @return RunResult object containing information about the tests.
     */
    public RunResult parseTestResult(TestRunFileAndLogs resultsFile) {
        try {
            return parseTestResult(
                    FileUtils.readFileToString(resultsFile.getTestResultsFile(), "UTF-8"),
                    resultsFile.getStdout(),
                    resultsFile.getStderr());
        } catch (IOException e) {
            log.error("Unable to parse test results from {}", resultsFile, e);
            // The testrun VM crashed, most likely due to System.exit command in tested code.
            return new RunResult(
                    RunResult.Status.TESTRUN_INTERRUPTED,
                    ImmutableList.<TestResult>of(),
                    ImmutableMap.<String, byte[]>of());
        }
    }

    /**
     * Parse run results from a JSON string.
     *
     * @param resultsJson   A JSON representation of the test results.
     * @return              Parsed RunResult
     */
    public RunResult parseTestResult(String resultsJson, byte[] stdout, byte[] stderr) {
        List<TestResult> testResults = new ArrayList<>();

        TestCaseList testCaseRecords = new Gson().fromJson(resultsJson, TestCaseList.class);
        boolean passed = true;

        for (fi.helsinki.cs.tmc.testrunner.TestCase tc : testCaseRecords) {
            testResults.add(convertTestCaseResult(tc));

            if (tc.status == fi.helsinki.cs.tmc.testrunner.TestCase.Status.FAILED) {
                passed = false;
            }
        }

        RunResult.Status status = passed ? RunResult.Status.PASSED : RunResult.Status.TESTS_FAILED;

        return new RunResult(
                status,
                ImmutableList.copyOf(testResults),
                ImmutableMap.of("stdout", stdout, "stderr", stderr));
    }

    private TestResult convertTestCaseResult(fi.helsinki.cs.tmc.testrunner.TestCase testCase) {
        List<String> exception = new ArrayList<>();
        List<String> points = new ArrayList<>();

        if (testCase.exception != null) {
            if (testCase.exception.message != null) {
                exception.add(testCase.exception.message);
            }
            for (StackTraceElement stackTrace : testCase.exception.stackTrace) {
                exception.add(stackTrace.toString());
            }
        }

        Collections.addAll(points, testCase.pointNames);

        String name = testCase.className + " " + testCase.methodName;
        boolean passed = testCase.status == fi.helsinki.cs.tmc.testrunner.TestCase.Status.PASSED;
        String message = testCase.message == null ? "" : testCase.message;

        return new TestResult(
                name,
                passed,
                ImmutableList.copyOf(points),
                message,
                ImmutableList.copyOf(exception));
    }
}
