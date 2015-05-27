package fi.helsinki.cs.tmc.langs.ant;

import com.google.common.collect.ImmutableList;

import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.langs.TestDesc;
import fi.helsinki.cs.tmc.langs.TestResult;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import org.apache.commons.io.FileUtils;

import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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

    @After
    public void tearDown() throws IOException {
        TestUtils.removeDirRecursively(getClass(), "ant_arith_funcs/build");
        TestUtils.removeDirRecursively(getClass(), "trivial/build");
    }

    @Test
    public void testScanExerciseReturnExerciseDesc() {
        String name = "Ant Test";
        ExerciseDesc description = antPlugin.scanExercise(TestUtils.getPath(getClass(), "ant_arith_funcs"), name).get();
        assertEquals(name, description.name);
        assertEquals(4, description.tests.size());
    }

    @Test
    public void testScanExerciseReturnsCorrectTests() {
        ExerciseDesc description = antPlugin.scanExercise(TestUtils.getPath(getClass(), "ant_arith_funcs"), "AntTestSubject").get();
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
    public void testScanExerciseReturnsNullWhenWrongProjectType() {
        assertFalse(antPlugin.scanExercise(TestUtils.getPath(getClass(), "non_ant_project"), "Dummy").isPresent());
    }

    @Test
    public void testRunTestsReturnsRunResultCorrectly() throws IOException {
        RunResult runResult = antPlugin.runTests(TestUtils.getPath(getClass(), "ant_arith_funcs"));
        assertEquals(RunResult.Status.TESTS_FAILED, runResult.status);
        assertTrue("Logs should be empty", runResult.logs.isEmpty());
        assertEquals(4, runResult.testResults.size());
    }

    @Test
    public void testRunTestsAwardsCorrectPoints() throws IOException {
        ImmutableList<TestResult> testResults = antPlugin.runTests(TestUtils.getPath(getClass(), "ant_arith_funcs")).testResults;
        assertEquals(4, testResults.size());
        assertTrue(testResults.get(0).passed);
        assertFalse(testResults.get(3).passed);
    }

    @Test
    public void testBuildAntProjectRunsBuildFile() throws IOException {
        Path path = TestUtils.getPath(getClass(), "ant_arith_funcs").toAbsolutePath();
        File buildDir = Paths.get(path.toString() + File.separatorChar + "build").toFile();
        antPlugin.buildAntProject(path);
        assertNotNull("Build directory should exist after building.", buildDir);
    }

    @Test
    public void testRunTestsReturnPassedCorrectly() throws IOException {
        RunResult runResult = antPlugin.runTests(TestUtils.getPath(getClass(), "trivial"));
        assertEquals(RunResult.Status.PASSED, runResult.status);
        TestResult testResult = runResult.testResults.get(0);
        assertTestResult(testResult, "", "TrivialTest testF", true);
        assertEquals("trivial", testResult.points.get(0));
        assertEquals("When all tests pass backtrace should be empty.", 0, testResult.backtrace.size());
    }

    @Test
    public void testRunTestsReturnsCompileFailedCorrectly() throws IOException {
        RunResult runResult = antPlugin.runTests(TestUtils.getPath(getClass(), "failing_trivial"));
        assertEquals("When the build fails the returned status should report it.", RunResult.Status.COMPILE_FAILED, runResult.status);
        assertTrue("When the build fails no test results should be returned", runResult.testResults.isEmpty());
        assertFalse(runResult.logs.isEmpty());
    }

    private void assertTestResult(TestResult testResult, String expectedErrorMessage, String expectedName, boolean expectedPassed) {
        assertEquals(expectedErrorMessage, testResult.errorMessage);
        assertEquals(expectedName, testResult.name);
        assertEquals(expectedPassed, testResult.passed);
    }

    @Test
    public void testAntCompileGivesOutputLogging() throws IOException {
        TestUtils.removeDirRecursively(getClass(), "ant_arith_funcs/build");
        File expected = new File("src/test/resources/arith_funcs_build.log");
        RunResult runResult = antPlugin.runTests(TestUtils.getPath(getClass(), "ant_arith_funcs"));
        File actual = new File("target/test-classes/ant_arith_funcs/build_log.txt");
        assertFileLines(expected, actual);

    }

    private void assertFileLines(File expected, File actual) throws IOException {

        List<String> expectedLines = FileUtils.readLines(expected);
        List<String> actualLines = FileUtils.readLines(actual);
        assertEquals("Build log should match by length", expectedLines.size(), actualLines.size());
    }
}
