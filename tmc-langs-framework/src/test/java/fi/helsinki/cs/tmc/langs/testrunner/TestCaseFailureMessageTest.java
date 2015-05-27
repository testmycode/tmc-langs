package fi.helsinki.cs.tmc.langs.testrunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

import static org.junit.Assert.assertEquals;

public class TestCaseFailureMessageTest {

    private TestCase testCase;

    @Before
    public void setUp() {
        this.testCase = new TestCase("foo", "bar", new String[]{"pt1", "pt2"});
    }

    private Failure makeFailure(Throwable t) {
        return new Failure(Description.EMPTY, t);
    }

    @Test
    public void itShouldContainTheExceptionNameUnlessItsAssertionError() {
        testCase.testFailed(makeFailure(new IllegalArgumentException("bla")));
        assertEquals("IllegalArgumentException: bla", testCase.message);
    }

    @Test
    public void itShouldNotContainTheExceptionNameIfItsAssertionError() {
        testCase.testFailed(makeFailure(new AssertionError("bla")));
        assertEquals("bla", testCase.message);
    }

    @Test
    public void itShouldContainOnlyTheExceptionNameIfItHasNoMessage() {
        testCase.testFailed(makeFailure(new IllegalStateException()));
        assertEquals("IllegalStateException", testCase.message);
    }

    @Test
    public void itShouldContainOnlyTheExceptionNameIfItHasNoMessageEvenForAssertionError() {
        testCase.testFailed(makeFailure(new AssertionError()));
        assertEquals("AssertionError", testCase.message);
    }
}
