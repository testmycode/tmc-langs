package fi.helsinki.cs.tmc.langs.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CompileResultTest {

    @Test
    public void constructorSetsValues() {
        byte[] stdout = new byte[] {0x01};
        byte[] stderr = new byte[] {0x02};
        int statusCode = 0;

        CompileResult result = new CompileResult(statusCode, stdout, stderr);

        assertEquals(statusCode, result.getStatusCode());
        assertEquals(stdout, result.getStdout());
        assertEquals(stderr, result.getStderr());
    }

    @Test
    public void canSetStatusCode() {
        CompileResult result = new CompileResult(0, null, null);
        result.setStatusCode(1);
        assertEquals(1, result.getStatusCode());
    }

    @Test
    public void canSetStdout() {
        byte[] stdout = new byte[] {0x01};
        CompileResult result = new CompileResult(0, null, null);
        result.setStdout(stdout);
        assertEquals(stdout, result.getStdout());
    }

    @Test
    public void canSetStderr() {
        byte[] stderr = new byte[] {0x02};
        CompileResult result = new CompileResult(0, null, null);
        result.setStderr(stderr);
        assertEquals(stderr, result.getStderr());
    }
}
