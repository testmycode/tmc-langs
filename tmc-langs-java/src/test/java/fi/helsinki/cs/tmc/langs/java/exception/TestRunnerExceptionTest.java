package fi.helsinki.cs.tmc.langs.java.exception;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestRunnerExceptionTest {

    @Test
    public void isConstructable() {
        TestRunnerException exception = new TestRunnerException();
        assertNotNull(exception);
    }

    @Test
    public void isConstructableFromThrowable() {
        Throwable original = new Throwable("Test");
        TestRunnerException exception = new TestRunnerException(original);
        assertEquals(original, exception.getCause());
    }
}
