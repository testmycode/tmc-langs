package fi.helsinki.cs.tmc.langs.domain;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class CaughtExceptionTest {

    private IOException subCause;
    private Exception cause;
    private StackTraceElement[] stackTraceElements;
    private CaughtException caughtException;

    @Before
    public void setUp() {
        subCause = new IOException("subCauseMessage");
        cause = new Exception("causeMessage", subCause);
        stackTraceElements = new StackTraceElement[]{new StackTraceElement("class",
                "method", "file", 1 )};
        cause.setStackTrace(stackTraceElements);

        caughtException = new CaughtException(cause);
    }

    @Test
    public void constructorSetsValues() {
        assertEquals(cause.getMessage(), caughtException.message);
        assertEquals(cause.getClass().getName(), caughtException.className);
        assertArrayEquals(stackTraceElements, caughtException.stackTrace);
        assertEquals(subCause.getMessage(), caughtException.cause.message);
    }

    @Test
    public void testCloneCreatesIdenticalObject() {
        CaughtException clone = caughtException.clone();

        assertEquals(cause.getMessage(), caughtException.message);
        assertEquals(cause.getClass().getName(), caughtException.className);
        assertArrayEquals(stackTraceElements, caughtException.stackTrace);
        assertEquals(subCause.getMessage(), caughtException.cause.message);
    }

    @Test
    public void toStringWritesAllValues() {
        String output = caughtException.toString();

        assertTrue(output.contains(caughtException.className));
        assertTrue(output.contains(caughtException.message));
        assertTrue(output.contains(caughtException.stackTrace[0].getFileName()));
        assertTrue(output.contains("" + caughtException.stackTrace[0].getLineNumber()));
        assertTrue(output.contains(caughtException.stackTrace[0].getClassName()));
        assertTrue(output.contains(caughtException.stackTrace[0].getMethodName()));
        assertTrue(output.contains(caughtException.cause.toString()));
    }

    @Test
    public void toStringHandlesNullMessage() {
        caughtException = new CaughtException(new IOException());

        String output = caughtException.toString();
        assertTrue(output.contains(caughtException.className));
    }
}
