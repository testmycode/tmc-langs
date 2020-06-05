package fi.helsinki.cs.tmc.langs.csharp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.RunResult.Status;
import fi.helsinki.cs.tmc.langs.domain.TestResult;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;

public class CSharpTestResultParserTest {

    private final Path passingSampleDir;
    private final Path failingSampleDir;

    public CSharpTestResultParserTest() {
        Path descSamplesDir = TestUtils.getPath(getClass(), "Test_samples");
        passingSampleDir = descSamplesDir.resolve("passingsample");
        failingSampleDir = descSamplesDir.resolve("failingsample");
    }

    @Test
    public void testPassingSampleParse() throws IOException {
        RunResult result = CSharpTestResultParser.parse(passingSampleDir);

        boolean allTestsPassed = true;
        for (TestResult res : result.testResults) {
            if (!res.isSuccessful()) {
                allTestsPassed = false;
            }
        }

        assertEquals(Status.PASSED, result.status);
        assertTrue(allTestsPassed);
    }

    @Test
    public void testFailingSampleParse() throws IOException {
        RunResult result = CSharpTestResultParser.parse(failingSampleDir);

        boolean allTestsPassed = true;
        for (TestResult res : result.testResults) {
            if (!res.isSuccessful()) {
                allTestsPassed = false;
            }
        }

        assertEquals(Status.TESTS_FAILED, result.status);
        assertFalse(allTestsPassed);
    }
}
