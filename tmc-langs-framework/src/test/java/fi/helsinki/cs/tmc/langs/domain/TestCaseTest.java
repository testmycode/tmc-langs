package fi.helsinki.cs.tmc.langs.domain;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.domain.TestCase.Status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.notification.Failure;

import java.io.IOException;

public class TestCaseTest {

    TestCase testCase;

    @Before
    public void setUp() {
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

    @Test
    public void constructingFromAnotherTestCaseCopiesValues() {
        TestCase copy = new TestCase(testCase);

        assertEquals(copy.methodName, testCase.methodName);
        assertEquals(copy.className, testCase.className);
        assertEquals(copy.message, testCase.message);
        assertEquals(copy.status, testCase.status);
        assertArrayEquals(copy.pointNames, testCase.pointNames);
        assertEquals(copy.exception, testCase.exception);
    }

    @Test
    public void constructingFromAnotherTestCaseCopiesException() {
        testCase.testFailed(new Failure(null, new IOException("message")));
        TestCase copy = new TestCase(testCase);

        assertEquals(copy.exception.toString(), testCase.exception.toString());
    }

    @Test
    public void settingAsFailureSetsException() {
        IOException exception = new IOException("message");
        Failure failure = new Failure(null, exception);

        testCase.testFailed(failure);

        assertEquals(exception.getMessage(), testCase.exception.message);
    }

    @Test
    public void settingAsFailureSetsMessageBasedOnExceptionWhenExceptionHasMessage() {
        IOException exception = new IOException("message");
        Failure failure = new Failure(null, exception);

        testCase.testFailed(failure);

        assertEquals("IOException: message", testCase.message);
    }

    @Test
    public void settingAsFailureSetsMessageBasedOnExceptionWhenExceptionHasNoMessage() {
        IOException exception = new IOException();
        Failure failure = new Failure(null, exception);

        testCase.testFailed(failure);

        assertEquals("IOException", testCase.message);
    }

    @Test
    public void settingAsFailureSetsMessageBasedOnMessageWhenExceptionIsAssertionError() {
        AssertionError err = new AssertionError("message");
        Failure failure = new Failure(null, err);

        testCase.testFailed(failure);

        assertEquals("message", testCase.message);
    }

    @Test
    public void settingAsFailureSetsMessageCorrectlyWhenExceptionIsMessagelessAssertionError() {
        AssertionError err = new AssertionError();
        Failure failure = new Failure(null, err);

        testCase.testFailed(failure);

        assertEquals("AssertionError", testCase.message);
    }

    @Test
    public void settingAsFailureWithNullMessageSetsMessageToNull() {
        Failure failure = new Failure(null, null);

        testCase.testFailed(failure);

        assertNull(testCase.message);
    }

    @Test
    public void toStringPrintsMethodClassAndStatus() {
        String output = testCase.toString();

        assertTrue(output.contains(testCase.methodName));
        assertTrue(output.contains(testCase.className));
        assertTrue(output.contains(testCase.status.toString()));
    }

    @Test
    public void toStringPrintsNonNullMessage() {
        IOException exception = new IOException("message");
        Failure failure = new Failure(null, exception);

        testCase.testFailed(failure);

        assertTrue(testCase.toString().contains("IOException: message"));
    }

    @Test
    public void toStringDoesNotPrintNullMessageOrNullException() {
        testCase.testFailed(new Failure(null, null));

        assertFalse(testCase.toString().contains("null"));
    }

    @Test
    public void toStringPrintsNonNullException() {
        IOException exception = new IOException("testMessage");
        testCase.testFailed(new Failure(null, exception));

        assertTrue(testCase.toString().contains(exception.toString()));
    }
}
