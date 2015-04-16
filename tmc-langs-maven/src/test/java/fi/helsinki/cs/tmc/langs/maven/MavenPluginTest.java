package fi.helsinki.cs.tmc.langs.maven;

import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationError;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class MavenPluginTest {

    MavenPlugin mavenPlugin;

    public MavenPluginTest() {
        mavenPlugin = new MavenPlugin();
    }

    @Test
    public void testGetLanguageName() {
        assertEquals("apache-maven", mavenPlugin.getLanguageName());
    }

    @Test
    public void testCheckCodeStyle() {
        ValidationResult result = mavenPlugin.checkCodeStyle(TestUtils.getPath(getClass(), "most_errors"));
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
        ValidationResult result = mavenPlugin.checkCodeStyle(projectToTest.toPath());
        assertNull(result);
    }

    @Test
    public void testPassingMavenBuild() {
        MavenPlugin.CompileResult result = mavenPlugin.buildMaven(TestUtils.getPath(getClass(), "maven_exercise"));
        assertEquals("Compile status should be 0 when build passes", 0, result.compileResult);
        assertTrue("Output should contain 'BUILD SUCCESS'", result.output.toString().contains("BUILD SUCCESS"));
    }

    @Test
    public void testFailingMavenBuild() {
        MavenPlugin.CompileResult result = mavenPlugin.buildMaven(TestUtils.getPath(getClass(), "failing_maven_exercise"));
        assertEquals("Compile status should be 1 when build fails", 1, result.compileResult);
        assertTrue("Output should contain 'BUILD FAILURE'", result.output.toString().contains("BUILD FAILURE"));
        assertTrue("Output should contain 'App.java:[5,8] error: not a statement'", result.output.toString().contains("App.java:[5,8] error: not a statement"));
    }

    @Test
    public void testRunTestsWhenBuildFailing() {
        RunResult runResult = mavenPlugin.runTests(TestUtils.getPath(getClass(), "failing_maven_exercise"));
        assertEquals(RunResult.Status.COMPILE_FAILED, runResult.status);
    }
}
