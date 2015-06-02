package fi.helsinki.cs.tmc.langs.make;

import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotEquals;

public class MakePluginTest {

    MakePlugin makePlugin;

    public MakePluginTest() {
        makePlugin = new MakePlugin();
    }



    @Test
    public void testGetLanguageName() {
        assertEquals("make", makePlugin.getLanguageName());
    }

    @Test
    public void testRunTestsWhenBuildFailing() {
        RunResult runResult = makePlugin.runTests(TestUtils.getPath(getClass(), "build-failing"));
        assertEquals(RunResult.Status.COMPILE_FAILED, runResult.status);
    }

    @Test
    public void testmakeProjectWithFailingTestsCompilesAndFailsTests() {
        Path path = TestUtils.getPath(getClass(), "failing");
        RunResult result = makePlugin.runTests(path);

        assertEquals(RunResult.Status.TESTS_FAILED, result.status);
    }

    @Test
    public void testFailingMakeProjectHasOneFailedTest() {
        Path path = TestUtils.getPath(getClass(), "failing");
        RunResult result = makePlugin.runTests(path);

        assertEquals(1, result.testResults.size());
        assertEquals(false, result.testResults.get(0).passed);
    }

    @Test
    public void testFailingMakeProjectHasCorrectError() {
        Path path = TestUtils.getPath(getClass(), "failing");
        RunResult result = makePlugin.runTests(path);

        assertEquals("[Task 1.1] one returned 2. Should have returned: 1", result.testResults.get(0)
                .errorMessage);
    }

    @Test
    public void testFailingMakeProjectHasStackTrace() {
        Path path = TestUtils.getPath(getClass(), "failing");
        RunResult result = makePlugin.runTests(path);

        assertTrue(result.testResults.get(0).backtrace.size() > 3);
    }

    @Test
    public void testmakeProjectWithPassingTestsCompilesAndPassesTests() {
        Path path = TestUtils.getPath(getClass(), "passing");
        RunResult result = makePlugin.runTests(path);

        assertEquals(RunResult.Status.PASSED, result.status);
    }

    @Test
    public void testPassingmakeProjectHasOnePassingTest() {
        Path path = TestUtils.getPath(getClass(), "passing");
        RunResult result = makePlugin.runTests(path);
        assertEquals(1, result.testResults.size());
        assertEquals(true, result.testResults.get(0).passed);
    }

    @Test
    public void testPassingMakeProjectHasNoError() {
        Path path = TestUtils.getPath(getClass(), "passing");
        RunResult result = makePlugin.runTests(path);

        assertEquals("", result.testResults.get(0).errorMessage);
    }

    @Test
    public void testPassingMakeProjectHasCorrectPoints() {
        Path path = TestUtils.getPath(getClass(), "passing");
        RunResult result = makePlugin.runTests(path);

        assertEquals("1.1", result.testResults.get(0).points.get(0));
        assertEquals(1, result.testResults.get(0).points.size());
    }

    @Test
    public void testFailingMakeProjectHasCorrectPoints() {
        Path path = TestUtils.getPath(getClass(), "failing");
        RunResult result = makePlugin.runTests(path);

        assertEquals(0, result.testResults.get(0).points.size());
    }
}
