package fi.helsinki.cs.tmc.langs.domain;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Test;

public class TestCaseTest {

    private TestCase.Status status = TestCase.Status.PASSED;
    private ImmutableList<TestResult> testResults = ImmutableList.of();
    private ImmutableMap<String, byte[]> logs = ImmutableMap.of();

    @Test
    public void constructorSetsValues() {
        TestCase result = new TestCase(status, testResults, logs);

        assertEquals(status, result.status);
        assertEquals(testResults, result.testResults);
        assertEquals(logs, result.logs);
    }

    @Test(expected = NullPointerException.class)
    public void canNotSetNullStatus() {
        new TestCase(null, testResults, logs);
    }

    @Test(expected = NullPointerException.class)
    public void canNotSetNullTestResults() {
        new TestCase(status, null, logs);
    }

    @Test(expected = NullPointerException.class)
    public void canNotSetNullLogs() {
        new TestCase(status, testResults, null);
    }
}
