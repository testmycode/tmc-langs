package fi.helsinki.cs.tmc.langs.ant;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.langs.TestDesc;
import fi.helsinki.cs.tmc.langs.TestResult;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationError;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class AntPluginTest {

    private AntPlugin antPlugin;

    public AntPluginTest() {
        antPlugin = new AntPlugin();
    }

    @Test
    public void testGetLanguageName() {
        assertEquals("apache-ant", antPlugin.getLanguageName());
    }

    @Test
    public void scanExerciseReturnExerciseDesc() {
        String name = "Ant Test";
        ExerciseDesc description = antPlugin.scanExercise(getPath("ant_arith_funcs"), name);
        assertEquals(name, description.name);
        assertEquals(4, description.tests.size());
    }

    @Test
    public void scanExerciseReturnsCorrectTests() {
        ExerciseDesc description = antPlugin.scanExercise(getPath("ant_arith_funcs"), "AntTestSubject");
        assertEquals(4, description.tests.size());

        TestDesc test = description.tests.get(0);
        assertEquals("ArithTest testAdd", test.name);
        assertEquals("arith-funcs", test.points.get(0));

        test = description.tests.get(2);
        assertEquals("ArithTest testMul", test.name);
        assertEquals("arith-funcs", test.points.get(0));
        assertEquals(1, test.points.size());
    }

    @Test
    public void scanExerciseReturnsNullWhenWrongProjectType() {
        assertNull(antPlugin.scanExercise(getPath("non_ant_project"), "Dummy"));
    }

    @Test
    public void runTestsReturnsRunResultCorrectly() {
        RunResult runResult = antPlugin.runTests(getPath("ant_arith_funcs"));
        assertEquals(RunResult.Status.TESTS_FAILED, runResult.status);
        assertTrue("Logs should be empty", runResult.logs.isEmpty());
        assertEquals(4, runResult.testResults.size());
    }

    @Test
    public void runTestsAwardsCorrectPoints() {
        ImmutableList<TestResult> testResults = antPlugin.runTests(getPath("ant_arith_funcs")).testResults;
        assertEquals(4, testResults.size());
        assertTrue(testResults.get(0).passed);
        assertFalse(testResults.get(3).passed);
	}

	@Test
    public void buildAntProjectRunsBuildFile() {
        Path path = getPath("ant_arith_funcs").toAbsolutePath();
        antPlugin.buildAntProject(path);
        File buildDir = Paths.get(path.toString() + File.separatorChar + "build").toFile();
        assertNotNull(buildDir);
        buildDir.delete();
    }

    @Test
    public void runTestsReturnPassedCorrectly() {
        RunResult runResult = antPlugin.runTests(getPath("trivial"));
        System.out.println(runResult.testResults.get(0).passed);
        assertEquals(RunResult.Status.PASSED, runResult.status);
        TestResult testResult = runResult.testResults.get(0);
        assertTestResult(testResult, "", "TrivialTest testF", true);
        assertEquals("trivial", testResult.points.get(0));
        assertEquals("When all tests pass backtrace should be empty.", 0, testResult.backtrace.size());
    }

    @Test
    public void testCheckCodeStyle() {
        File projectToTest = new File("src/test/resources/most_errors/");
        ValidationResult result = antPlugin.checkCodeStyle(projectToTest.toPath());
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
        ValidationResult result = antPlugin.checkCodeStyle(projectToTest.toPath());
        assertNull(result);
    }

    private void assertTestResult(TestResult testResult, String expectedErrorMessage, String expectedName, boolean expectedPassed) {
        assertEquals(expectedErrorMessage, testResult.errorMessage);
        assertEquals(expectedName, testResult.name);
        assertEquals(expectedPassed, testResult.passed);
    }

    private Path getPath(String location) {
        Path path;
        try {
            path = Paths.get(getClass().getResource(File.separatorChar + location).toURI());
        } catch (URISyntaxException e) {
            throw Throwables.propagate(e);
        }
        return path;
    }
}
