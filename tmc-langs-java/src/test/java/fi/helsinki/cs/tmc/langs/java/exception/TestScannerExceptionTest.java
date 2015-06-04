package fi.helsinki.cs.tmc.langs.java.exception;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class TestScannerExceptionTest {

    @Test
    public void isConstructable() {
        TestScannerException exception = new TestScannerException();
        assertNotNull(exception);
    }
}
