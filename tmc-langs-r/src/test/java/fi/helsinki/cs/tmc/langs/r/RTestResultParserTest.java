package fi.helsinki.cs.tmc.langs.r;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.TestResult;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;

public class RTestResultParserTest {

    private final Path allPassProjectPath;
    private final Path someFailProjectPath;

    public RTestResultParserTest() {
        Path jsonsDir = TestUtils.getPath(getClass(), "example_jsons");
        allPassProjectPath = jsonsDir.resolve("simple_all_tests_pass");
        someFailProjectPath = jsonsDir.resolve("simple_some_tests_fail");
    }

    private void testResultAsExpected(TestResult result, boolean successful, String name,
                                      String[] points) {
        assertEquals(successful, result.isSuccessful());
        assertEquals(name, result.getName());
        assertArrayEquals(points, result.points.toArray());
    }

    @Test
    public void testThatParseWorksForAllPass() throws IOException {
        RunResult result = new RTestResultParser(allPassProjectPath).parse();

        assertEquals(RunResult.Status.PASSED, result.status);

        ImmutableList<TestResult> results = result.testResults;
        testResultAsExpected(results.get(0), true,
                "ret_true works.", new String[]{"r1", "r1.1"});
        testResultAsExpected(results.get(1), true,
                "ret_one works.", new String[]{"r1", "r1.2"});
        testResultAsExpected(results.get(2), true,
                "add works.", new String[]{"r1", "r1.3", "r1.4"});
        testResultAsExpected(results.get(3), true,
                "minus works", new String[]{"r2", "r2.1"});
    }

    @Test
    public void testThatParseWorksForSomeFail() throws IOException {
        RunResult result = new RTestResultParser(someFailProjectPath).parse();

        assertEquals(RunResult.Status.TESTS_FAILED, result.status);

        ImmutableList<TestResult> results = result.testResults;
        testResultAsExpected(results.get(0), true,
                "ret_true works.", new String[]{"r1", "r1.1"});
        testResultAsExpected(results.get(1), true,
                "ret_one works.", new String[]{"r1", "r1.2"});
        testResultAsExpected(results.get(2), true,
                "add works.", new String[]{"r1", "r1.3", "r1.4"});
        testResultAsExpected(results.get(3), false,
                "ret_false returns true", new String[]{"r1", "r1.5"});
        testResultAsExpected(results.get(4), true,
                "ret_true works but there are no points.", new String[]{"r1"});
    }
}
