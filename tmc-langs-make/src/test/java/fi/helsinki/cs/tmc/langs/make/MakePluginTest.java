package fi.helsinki.cs.tmc.langs.make;

import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationError;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

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
    public void testCheckCodeStyle() {
        ValidationResult result = makePlugin.checkCodeStyle(TestUtils.getPath(getClass(), "most_errors"));
        Map<File, List<ValidationError>> res = result.getValidationErrors();
        assertEquals("Should be one erroneous file", 1, res.size());
        for (File file : res.keySet()) {
            List<ValidationError> errors = res.get(file);
            assertEquals("Should return the right amount of errors", 23, errors.size());
        }
    }

    @Test
    public void testCheckCodeStyleWithUntestableProject() {
        File projectToTest = new File("src/test/resources/arith_funcs/");
        ValidationResult result = makePlugin.checkCodeStyle(projectToTest.toPath());
        assertNull(result);
    }

    @Test
    public void testPassingmakeBuild() throws IOException {
        //CompileResult result = makePlugin.buildmake(TestUtils.getPath(getClass(), "make_exercise"));
        //assertEquals("Compile status should be 0 when build passes", 0, result.getStatusCode());
    }

    @Test
    public void testFailingmakeBuild() throws IOException {
//        CompileResult result = makePlugin.buildmake(TestUtils.getPath(getClass(), "failing_make_exercise"));
//        assertEquals("Compile status should be 1 when build fails", 1, result.getStatusCode());
    }

    @Test
    public void testRunTestsWhenBuildFailing() {
        RunResult runResult = makePlugin.runTests(TestUtils.getPath(getClass(), "failing_make_exercise"));
        assertEquals(RunResult.Status.COMPILE_FAILED, runResult.status);
    }

    @Test
    public void testmakeProjectWithFailingTestsCompilesAndFailsTests() {
        Path path = TestUtils.getPath(getClass(), "make_exercise");
        RunResult result = makePlugin.runTests(path);

        assertEquals(RunResult.Status.TESTS_FAILED, result.status);
    }

    @Test
    public void testFailingmakeProjectHasOneFailedTest() {
        Path path = TestUtils.getPath(getClass(), "make_exercise");
        RunResult result = makePlugin.runTests(path);

        assertEquals(1, result.testResults.size());
        assertEquals(false, result.testResults.get(0).passed);
    }

    @Test
    public void testFailingmakeProjectHasCorrectError() {
        Path path = TestUtils.getPath(getClass(), "make_exercise");
        RunResult result = makePlugin.runTests(path);

        assertEquals("ComparisonFailure: expected:\u003c[Hello make!\n]\u003e but was:\u003c[]\u003e", result.testResults.get(0).errorMessage);
    }

    @Test
    public void testFailingmakeProjectHasStackTrace() {
        Path path = TestUtils.getPath(getClass(), "make_exercise");
        RunResult result = makePlugin.runTests(path);

        assertTrue(result.testResults.get(0).backtrace.size() > 0);
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
