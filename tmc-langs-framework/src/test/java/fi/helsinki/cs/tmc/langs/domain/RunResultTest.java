package fi.helsinki.cs.tmc.langs.domain;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Test;

public class RunResultTest {

    private RunResult.Status status = RunResult.Status.PASSED;
    private ImmutableList<TestResult> testResults = ImmutableList.of();
    private ImmutableMap<String, byte[]> logs = ImmutableMap.of();

    @Test
    public void constructorSetsValues() {
        RunResult result = new RunResult(status, testResults, logs);

        assertEquals(status, result.status);
        assertEquals(testResults, result.testResults);
        assertEquals(logs, result.logs);
    }

    @Test(expected = NullPointerException.class)
    public void canNotSetNullStatus() {
        new RunResult(null, testResults, logs);
    }

    @Test(expected = NullPointerException.class)
    public void canNotSetNullTestResults() {
        new RunResult(status, null, logs);
    }

    @Test(expected = NullPointerException.class)
    public void canNotSetNullLogs() {
        new RunResult(status, testResults, null);
    }
}
