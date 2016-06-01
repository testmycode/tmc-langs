package fi.helsinki.cs.tmc.langs.java.ant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.TestCase;
import fi.helsinki.cs.tmc.langs.domain.TestDesc;
import fi.helsinki.cs.tmc.langs.domain.TestResult;
import fi.helsinki.cs.tmc.langs.java.ClassPath;
import fi.helsinki.cs.tmc.langs.java.exception.TestRunnerException;
import fi.helsinki.cs.tmc.langs.java.exception.TestScannerException;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import org.apache.commons.io.FileUtils;

import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
        Path project = TestUtils.getPath(getClass(), "ant_arith_funcs");
        ExerciseDesc description = antPlugin.scanExercise(project, name).get();
        assertEquals(name, description.name);
        assertEquals(4, description.tests.size());
    }

    @Test
    public void testScanExerciseReturnsCorrectTests() {
        Path project = TestUtils.getPath(getClass(), "ant_arith_funcs");
        ExerciseDesc description = antPlugin.scanExercise(project, "AntTestSubject").get();
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
        Path project = TestUtils.getPath(getClass(), "non_ant_project");
        assertFalse(antPlugin.scanExercise(project, "Dummy").isPresent());
    }

    @Test
    public void testRunTestsReturnsRunResultCorrectly() throws IOException {
        TestCase testCase = antPlugin.runTests(TestUtils.getPath(getClass(), "ant_arith_funcs"));
        assertEquals(TestCase.Status.TESTS_FAILED, testCase.status);
        assertTrue("Stdout should be empty", testCase.logs.get("stdout").length == 0);
        assertTrue("Stderr should be empty", testCase.logs.get("stderr").length == 0);
        assertEquals(4, testCase.testResults.size());
    }

    @Test
    public void testRunTestsAwardsCorrectPoints() throws IOException {
        Path project = TestUtils.getPath(getClass(), "ant_arith_funcs");
        ImmutableList<TestResult> testResults = antPlugin.runTests(project).testResults;
        assertEquals(4, testResults.size());
        assertTrue(testResults.get(0).passed);
        assertFalse(testResults.get(3).passed);
    }

    @Test
    public void testBuildAntProjectRunsBuildFile() throws IOException {
        Path path = TestUtils.getPath(getClass(), "ant_arith_funcs").toAbsolutePath();
        File buildDir = Paths.get(path.toString() + File.separatorChar + "build").toFile();
        antPlugin.build(path);
        assertNotNull("Build directory should exist after building.", buildDir);
    }

    @Test
    public void testRunTestsReturnPassedCorrectly() throws IOException {
        TestCase testCase = antPlugin.runTests(TestUtils.getPath(getClass(), "trivial"));
        assertEquals(TestCase.Status.PASSED, testCase.status);
        TestResult testResult = testCase.testResults.get(0);
        assertTestResult(testResult, "", "TrivialTest testF", true);
        assertEquals("trivial", testResult.points.get(0));
        assertEquals(
                "When all tests pass backtrace should be empty.", 0, testResult.backtrace.size());
    }

    @Test
    public void testRunTestsReturnsCompileFailedCorrectly() throws IOException {
        Path project = TestUtils.getPath(getClass(), "failing_trivial");
        TestCase testCase = antPlugin.runTests(project);
        assertEquals(
                "When the build fails the returned status should report it.",
                TestCase.Status.COMPILE_FAILED,
                testCase.status);
        assertTrue(
                "When the build fails no test results should be returned",
                testCase.testResults.isEmpty());
        assertFalse(testCase.logs.isEmpty());
    }

    private void assertTestResult(
            TestResult testResult,
            String expectedErrorMessage,
            String expectedName,
            boolean expectedPassed) {
        assertEquals(expectedErrorMessage, testResult.errorMessage);
        assertEquals(expectedName, testResult.name);
        assertEquals(expectedPassed, testResult.passed);
    }

    @Test
    public void pluginHandlesProjectThatUsesReflectionUtils() {
        Path project = TestUtils.getPath(getClass(), "reflection_utils_ant_test_case");
        TestCase result = antPlugin.runTests(project);
        assertEquals(TestCase.Status.PASSED, result.status);
    }

    @Test(expected = TestScannerException.class)
    public void createRunResultFileThrowsTestScannerExceptionOnTestScannerFailure()
            throws TestScannerException, TestRunnerException {
        AntPlugin plugin =
                new AntPlugin() {
                    @Override
                    public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName) {
                        return Optional.absent();
                    }
                };

        plugin.createRunResultFile(Paths.get(""));
    }

    private void assertFileLinesCount(File expected, File actual) throws IOException {

        List<String> expectedLines = FileUtils.readLines(expected);
        List<String> actualLines = FileUtils.readLines(actual);
        assertEquals("Build log should match by length", expectedLines.size(), actualLines.size());
    }

    @Test
    public void toolsJarIsIncludedToThePath() {
        Path project = TestUtils.getPath(getClass(), "UsingToolsJar");
        ClassPath cp = antPlugin.getProjectClassPath(project);
        assertPathContains(cp, "tools.jar");
        String name = "ToolsJar";
        ExerciseDesc description = antPlugin.scanExercise(project, name).get();
        assertEquals(name, description.name);
        assertEquals(1, description.tests.size());
        TestCase testCase = antPlugin.runTests(project);
        assertEquals(TestCase.Status.PASSED, testCase.status);
        assertEquals(1, testCase.testResults.size());
    }

    @Test
    public void testShouldNotDieWhenStudentCodeUsesSystemExitZero() {
        String name = "SystemExit0";
        Path project = TestUtils.getPath(getClass(), "exit_zero");
        ExerciseDesc description = antPlugin.scanExercise(project, name).get();
        assertEquals(name, description.name);
        assertEquals(1, description.tests.size());

        TestCase result = antPlugin.runTests(project);
        assertEquals(TestCase.Status.TESTRUN_INTERRUPTED, result.status);
    }

    private void assertPathContains(ClassPath cp, String file) {
        boolean found = false;
        for (Path path : cp.getPaths()) {
            if (path.endsWith(Paths.get(file))) {
                found = true;
                break;
            }
        }
        assertTrue(
                "ClassPath should have contained \"" + file + "\". It contained " + cp.getPaths(),
                found);
    }
}
