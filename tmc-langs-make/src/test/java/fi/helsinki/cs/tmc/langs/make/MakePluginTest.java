package fi.helsinki.cs.tmc.langs.make;

import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MakePluginTest {

    MakePlugin makePlugin;

    public MakePluginTest() {
        makePlugin = new MakePlugin();
    }

    @Test
    public void testGetLanguageName() {
        assertEquals("make", makePlugin.getLanguageName());
    }

//    @Test
//    public void testCheckCodeStyle() {
//        ValidationResult result = makePlugin.checkCodeStyle(TestUtils.getPath(getClass(), "most_errors"));
//        Map<File, List<ValidationError>> res = result.getValidationErrors();
//        assertEquals("Should be one erroneous file", 1, res.size());
//        for (File file : res.keySet()) {
//            List<ValidationError> errors = res.get(file);
//            assertEquals("Should return the right amount of errors", 23, errors.size());
//        }
//    }
//
//    @Test
//    public void testCheckCodeStyleWithUntestableProject() {
//        File projectToTest = new File("src/test/resources/arith_funcs/");
//        ValidationResult result = makePlugin.checkCodeStyle(projectToTest.toPath());
//        assertNull(result);
//    }

    @Test
    public void testPassingmakeBuild() throws IOException {
        //CompileResult result = makePlugin.buildmake(TestUtils.getPath(getClass(), "failing"));
        //assertEquals("Compile status should be 0 when build passes", 0, result.getStatusCode());
    }

    @Test
    public void testFailingmakeBuild() throws IOException {
//        CompileResult result = makePlugin.buildmake(TestUtils.getPath(getClass(), "build-failing"));
//        assertEquals("Compile status should be 1 when build fails", 1, result.getStatusCode());
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
    public void testFailingmakeProjectHasOneFailedTest() {
        Path path = TestUtils.getPath(getClass(), "failing");
        RunResult result = makePlugin.runTests(path);

        assertEquals(1, result.testResults.size());
        assertEquals(false, result.testResults.get(0).passed);
    }

    @Test
    public void testFailingmakeProjectHasCorrectError() {
        Path path = TestUtils.getPath(getClass(), "failing");
        RunResult result = makePlugin.runTests(path);

        assertEquals("[Task 1.1] one returned 2. Should have returned: 1", result.testResults.get(0)
                .errorMessage);
    }

    @Test
    public void testFailingmakeProjectHasStackTrace() {
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
    public void testPassingmakeProjectHasNoError() {
        Path path = TestUtils.getPath(getClass(), "passing");
        RunResult result = makePlugin.runTests(path);

        assertEquals("", result.testResults.get(0).errorMessage);
    }
}
