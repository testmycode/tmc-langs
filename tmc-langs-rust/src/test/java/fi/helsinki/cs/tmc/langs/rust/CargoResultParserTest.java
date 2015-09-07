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
    private final String fail = new StringBuilder()
            .append("   Compiling example version (path)\n")
            .append("     Running target\\debug\\mod-hash.exe\n")
            .append("\n")
            .append("running 1 test\n")
            .append("test name ... FAILED\n")
            .append("\n")
            .append("failures:\n")
            .append("\n")
            .append("---- name stdout ----\n")
            .append("\tthread 'name' panicked at 'description', tests\\mod.rs:line\n")
            .append("\n")
            .append("\n")
            .append("failures:\n")
            .append("    name\n")
            .append("\n")
            .append("test result: FAILED. 0 passed; 1 failed; 0 ignored; 0 measured")
            .toString();
    private ProcessResult failResult;

    @Before
    public void setUp() {
        parser = new CargoResultParser();
        failResult = new ProcessResult(0, fail, "");
    }

    @Test
    public void failFails() {
        RunResult parsed = parser.parse(failResult);
        assertEquals(Status.TESTS_FAILED, parsed.status);
    }

    @Test
    public void failContainsRightResult() {
        RunResult parsed = parser.parse(failResult);
        assertEquals(1, parsed.testResults.size());
        assertEquals("description", parsed.testResults.get(0).errorMessage);
        assertEquals("name", parsed.testResults.get(0).name);
        assertEquals(false, parsed.testResults.get(0).passed);
    }

    @Test
    public void failPreservesStdout() {
        RunResult parsed = parser.parse(failResult);
        assertTrue(parsed.logs.containsKey(SpecialLogs.STDOUT));
        assertEquals(fail, new String(parsed.logs.get(SpecialLogs.STDOUT)));
    }
}
