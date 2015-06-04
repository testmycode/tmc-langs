package fi.helsinki.cs.tmc.langs.java.exception;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class TestScannerExceptionTest {

    @Test
    public void isConstructable() {
        TestScannerException exception = new TestScannerException();
        assertNotNull(exception);
    }
}
