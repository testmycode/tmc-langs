package fi.helsinki.cs.tmc.langs.rust;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.RunResult.Status;
import fi.helsinki.cs.tmc.langs.domain.SpecialLogs;
import fi.helsinki.cs.tmc.langs.utils.ProcessResult;

import org.junit.Before;
import org.junit.Test;

public class CargoResultParserTest {

    private CargoResultParser parser;
    private String fail;

    @Before
    public void setUp() {
        parser = new CargoResultParser();

        StringBuilder builder = new StringBuilder();
        builder.append("   Compiling example version (path)\n");
        builder.append("     Running target\\debug\\mod-hash.exe\n");
        builder.append("\n");
        builder.append("running 1 test\n");
        builder.append("test name ... FAILED\n");
        builder.append("\n");
        builder.append("failures:\n");
        builder.append("\n");
        builder.append("---- name stdout ----\n");
        builder.append("\tthread 'name' panicked at 'description', tests\\mod.rs:line\n");
        builder.append("\n");
        builder.append("\n");
        builder.append("failures:\n");
        builder.append("    name\n");
        builder.append("\n");
        builder.append("test result: FAILED. 0 passed; 1 failed; 0 ignored; 0 measured");
        fail = builder.toString();
    }

    @Test
    public void failFails() {
        ProcessResult result = new ProcessResult(0, fail, "");
        RunResult parsed = parser.parse(result);
        assertEquals(Status.TESTS_FAILED, parsed.status);
    }

    @Test
    public void failContainsRightResult() {
        ProcessResult result = new ProcessResult(0, fail, "");
        RunResult parsed = parser.parse(result);
        assertEquals(1, parsed.testResults.size());
        assertEquals("description", parsed.testResults.get(0).errorMessage);
        assertEquals("name", parsed.testResults.get(0).name);
        assertEquals(false, parsed.testResults.get(0).passed);
    }

    @Test
    public void failPreservesStdout() {
        ProcessResult result = new ProcessResult(0, fail, "");
        RunResult parsed = parser.parse(result);
        assertTrue(parsed.logs.containsKey(SpecialLogs.STDOUT));
        assertEquals(fail, new String(parsed.logs.get(SpecialLogs.STDOUT)));
    }
}
