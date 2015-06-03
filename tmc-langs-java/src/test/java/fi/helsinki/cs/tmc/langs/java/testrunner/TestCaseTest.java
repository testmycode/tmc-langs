package fi.helsinki.cs.tmc.langs.java.testrunner;

import static org.junit.Assert.assertEquals;

import fi.helsinki.cs.tmc.langs.java.testrunner.TestCase.Status;

import org.junit.Test;
import org.junit.runner.notification.Failure;

public class TestCaseTest {

    TestCase testCase;

    public TestCaseTest() {
        testCase = new TestCase("Test", "Method", new String[]{"a", "b", "c"});
    }

    @Test
    public void testTestStarted() {
        testCase.testStarted();
        assert (testCase.status == Status.RUNNING);
    }

    @Test
    public void testTestFinished() {
        testCase.testFinished();
        assert (testCase.status == Status.PASSED);
    }

    @Test
    public void testTestFinishedWhenTestFailed() {
        testCase.testFailed(new Failure(null, null));
        testCase.testFinished();
        assert (testCase.status == Status.FAILED);
    }

    @Test
    public void testTestFailed() {
        testCase.testFailed(new Failure(null, null));
        assert (testCase.status == Status.FAILED);
    }

    @Test
    public void testToString() {
        assertEquals("Method (Test) NOT_STARTED", testCase.toString());
        testCase.testFinished();
        assertEquals("Method (Test) PASSED", testCase.toString());
    }

}
