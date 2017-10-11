package fi.helsinki.cs.tmc.langs.r;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.SpecialLogs;
import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RPluginTest {

    private RPlugin plugin;

    private Path simpleAllTestsPassProject;
    private Path simpleSomeTestsFailProject;
    private Path simpleSourceCodeErrorProject;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        plugin = new RPlugin();

        simpleAllTestsPassProject = TestUtils.getPath(getClass(),
                "simple_all_tests_pass");
        simpleSomeTestsFailProject = TestUtils.getPath(getClass(),
                "simple_some_tests_fail");
        simpleSourceCodeErrorProject = TestUtils.getPath(getClass(),
                "simple_source_code_error");
    }

    @After
    public void tearDown() throws IOException {
        removeAvailablePointsJson(simpleAllTestsPassProject);
        removeResultsJson(simpleAllTestsPassProject);
        removeResultsJson(simpleSomeTestsFailProject);
        removeResultsJson(simpleSourceCodeErrorProject);
    }

    private void removeResultsJson(Path projectPath) throws IOException {
        Files.deleteIfExists(projectPath.resolve(".results.json"));
    }

    private void removeAvailablePointsJson(Path projectPath) throws IOException {
        Files.deleteIfExists(projectPath.resolve(".available_points.json"));
    }

    @Test
    public void testScanExercise() {
        plugin.scanExercise(simpleAllTestsPassProject, "main.R");
        assertTrue(Files.exists(simpleAllTestsPassProject.resolve(".available_points.json")));
    }

    @Test
    public void testScanExerciseInTheWrongPlace() throws IOException {
        plugin.scanExercise(simpleAllTestsPassProject, "ar.R");
        Path availablePointsJson = simpleAllTestsPassProject.resolve(".available_points.json");
        
        exception.expect(IOException.class);
        new RExerciseDescParser(availablePointsJson).parse();
    }

    @Test
    public void runTestsRunResultWithCorrectStatusForSimpleAllPass() {
        RunResult result = plugin.runTests(simpleAllTestsPassProject);

        assertEquals(RunResult.Status.PASSED, result.status);
    }

    @Test
    public void runTestsRunResultWithCorrectStatusForSimpleSomeFail() {
        RunResult result = plugin.runTests(simpleSomeTestsFailProject);

        assertEquals(RunResult.Status.TESTS_FAILED, result.status);
    }

    @Test
    public void runTestsCreatesRunResultWithCorrectStatusWhenSourceCodeHasError() {
        RunResult res = plugin.runTests(simpleSourceCodeErrorProject);

        assertEquals(RunResult.Status.COMPILE_FAILED, res.status);
    }

    @Test
    public void runTestsReturnsGenericErrorWhenPathDoesNotExist() {
        Path doesNotExist = TestUtils.getPath(getClass(),
                "aijoigad0");
        RunResult res = plugin.runTests(doesNotExist);

        assertEquals(RunResult.Status.GENERIC_ERROR, res.status);
    }

    @Test
    public void runTestsReturnsStackTraceWhenPathDoesNotExist() {
        Path doesNotExist = TestUtils.getPath(getClass(),
                "aijoigad0");
        RunResult res = plugin.runTests(doesNotExist);
        
        String stackTrace = new String(res.logs.get(SpecialLogs.GENERIC_ERROR_MESSAGE));
        assertEquals("java.lang.NullPointerException", stackTrace.split("\n")[0]);
        assertTrue(stackTrace.split("\n").length > 1);
    }
    
    @Test
    public void exerciseIsCorrectTypeIfItContainsRFolder() {
        Path testCasesRoot = TestUtils.getPath(getClass(), "recognition_test_cases");
        Path project = testCasesRoot.resolve("R_folder");

        assertTrue(plugin.isExerciseTypeCorrect(project));
    }

    @Test
    public void exerciseIsCorrectTypeIfItContainsTestthatFolder() {
        Path testCasesRoot = TestUtils.getPath(getClass(), "recognition_test_cases");
        Path project = testCasesRoot.resolve("testthat_folder");

        assertTrue(plugin.isExerciseTypeCorrect(project));
    }

    @Test
    public void exerciseIsCorrectTypeIfItContainsTestthatFile() {
        Path testCasesRoot = TestUtils.getPath(getClass(), "recognition_test_cases");
        Path project = testCasesRoot.resolve("testthat_folder")
                                    .resolve("tests");

        assertTrue(Files.exists(project.resolve("testthat.R")));
    }

    @Test
    public void getStudentFilePolicyReturnsRStudentFilePolicy() {
        StudentFilePolicy policy = plugin.getStudentFilePolicy(Paths.get(""));

        assertTrue(policy instanceof RStudentFilePolicy);
    }
}
