package fi.helsinki.cs.tmc.langs.r;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.TestResult;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;

public class RTestResultParserTest {

    private RunResult rr;
    private Path jsonDir;

    public RTestResultParserTest() {
        jsonDir = TestUtils.getPath(getClass(), "example_json");
        try {
            rr = new RTestResultParser(jsonDir).parse();
        } catch (IOException e) {
            System.out.println("Something wrong: " + e.getMessage());
        }
    }

    @Test
    public void testThatParseSeemsToWorkOnExampleJson() {
        assertEquals(RunResult.Status.TESTS_FAILED, rr.status);
        assertEquals(22, rr.testResults.size());

        for (TestResult tr : rr.testResults) {
            if (tr.getName().equals("Addition works")) {
                assertTrue(tr.isSuccessful());
                assertEquals(2, tr.points.size());
                assertTrue(tr.points.contains("r1.1"));
                assertTrue(tr.points.contains("r1.2"));
            }
            if (!tr.isSuccessful()) {
                assertEquals("Dummy test set to fail", tr.getName());
            }
        }
    }
   
}
