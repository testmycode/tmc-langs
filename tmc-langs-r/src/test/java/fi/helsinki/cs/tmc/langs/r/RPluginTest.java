package fi.helsinki.cs.tmc.langs.r;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.TestDesc;
import fi.helsinki.cs.tmc.langs.domain.TestResult;
import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RPluginTest {

    private RPlugin plugin;

    @Before
    public void setUp() {
        plugin = new RPlugin();
    }

    @After
    public void tearDown() {
        Path testDir = TestUtils.getPath(getClass(), "project1");
        File resultsJson = new File(testDir.toAbsolutePath().toString() + "/.results.json");
        resultsJson.delete();
        File availablePointsJson = new File(testDir.toAbsolutePath().toString()
                + "/.available_points.json");
        availablePointsJson.delete();

        testDir = TestUtils.getPath(getClass(), "simple_source_code_error");
        resultsJson = new File(testDir.toAbsolutePath().toString() + "/.results.json");
        resultsJson.delete();
        
        testDir = TestUtils.getPath(getClass(), "simple_all_tests_pass");
        resultsJson = new File(testDir.toAbsolutePath().toString() + "/.results.json");
        resultsJson.delete();
    }

    @Test
    public void testGetTestCommand() {
        String[] command = new String[]{"Rscript"};
        String[] args;

        if (SystemUtils.IS_OS_WINDOWS) {
            args = new String[]{"-e", "\"library('tmcRtestrunner');run_tests()\""};
        } else {
            args = new String[]{"-e", "library(tmcRtestrunner);run_tests()"};
        }
        String[] expectedCommand = ArrayUtils.addAll(command, args);
        Assert.assertArrayEquals(expectedCommand, plugin.getTestCommand());
    }

    @Test
    public void testGetAvailablePointsCommand() {
        String[] command = new String[]{"Rscript"};
        String[] args;
        if (SystemUtils.IS_OS_WINDOWS) {
            args = new String[]{"-e", "\"library('tmcRtestrunner');run_available_points()\""};
        } else {
            args = new String[]{"-e", "library(tmcRtestrunner);run_available_points()"};
        }
        String[] expectedCommand = ArrayUtils.addAll(command, args);
        Assert.assertArrayEquals(expectedCommand, plugin.getAvailablePointsCommand());
    }

    @Test
    public void testGetPluginName() {
        assertEquals("r", plugin.getLanguageName());
    }

    @Test
    public void testScanExercise() {
        Path testDir = TestUtils.getPath(getClass(), "project1");
        plugin.scanExercise(testDir, "arithmetics.R");
        File availablePointsJson = new File(testDir.toAbsolutePath().toString()
                + "/.available_points.json");

        assertTrue(availablePointsJson.exists());
    }

    @Test
    public void testScanExerciseInTheWrongPlace() {
        Path testDir = TestUtils.getPath(getClass(), "project1");
        plugin.scanExercise(testDir, "ar.R");
        Path availablePointsJson = testDir.resolve(".available_points.json");
        ImmutableList<TestDesc> re = null;
        try {
            re = new RExerciseDescParser(availablePointsJson).parse();
        } catch (IOException e) {
            System.out.println("Something wrong: " + e.getMessage());
        }
        assertTrue(re == null);
    }

    @Test
    public void testRunTests() {
        Path testDir = TestUtils.getPath(getClass(), "project1");
        RunResult runRes = plugin.runTests(testDir);
        ImmutableList<TestResult> re = runRes.testResults;
        assertEquals(re.size(),22);
        assertEquals(re.get(0).getName(),"Addition works");
        assertTrue(re.get(1).isSuccessful());
        assertEquals(re.get(1).getName(),"Multiplication works");
        assertTrue(re.get(2).isSuccessful());
        assertEquals(re.get(2).getName(),"Subtraction works");
        assertTrue(re.get(3).isSuccessful());
        assertEquals(re.get(3).getName(),"Division works");
        assertTrue(re.get(4).isSuccessful());
        assertEquals(re.get(4).getName(), "Test with no points");
        assertFalse(re.get(5).isSuccessful());
        assertEquals(re.get(5).getName(), "Dummy test set to fail");
        assertTrue(re.get(6).isSuccessful());
        assertEquals(re.get(6).getName(), "Matrix transpose with [[1,2]] works");
        assertTrue(re.get(7).isSuccessful());
        assertEquals(re.get(7).getName(), "Matrix transpose with [[1,2],[3,4]] works");
        assertTrue(re.get(8).isSuccessful());
        assertEquals(re.get(8).getName(), "Constant string works");
        for (int i = 1;i <= 13;i++) {
            assertEquals(re.get(8 + i).getName(), "Exercise " + i + " is correct");
            assertTrue(re.get(8 + i).isSuccessful());

        }

        File resultsJson = new File(testDir.toAbsolutePath().toString() + "/.results.json");

        assertTrue(resultsJson.exists());
    }

    @Test
    public void runTestsCreatesJsonWithCorrectStatus() {
        Path testDir = TestUtils.getPath(getClass(), "project1");
        RunResult res = plugin.runTests(testDir);

        assertEquals(RunResult.Status.TESTS_FAILED, res.status);
    }

    @Test
    public void runTestsWorksWithErronousSourceCode() {
        Path testDir = TestUtils.getPath(getClass(), "simple_source_code_error");
        RunResult res = plugin.runTests(testDir);

        assertEquals(RunResult.Status.COMPILE_FAILED, res.status);
        assertEquals(1, res.testResults.size());
    }

    @Test
    public void runTestsHasCorrectStatusesWhenAllTestsPass() {
        Path testDir = TestUtils.getPath(getClass(), "simple_all_tests_pass");
        RunResult res = plugin.runTests(testDir);

        assertEquals(RunResult.Status.PASSED, res.status);
        for (TestResult tr : res.testResults) {
            assertTrue(tr.isSuccessful());
        }
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
    public void exerciseIsCorrectTypeIfItContainsDescription() {
        Path testCasesRoot = TestUtils.getPath(getClass(), "recognition_test_cases");
        Path project = testCasesRoot.resolve("description");

        assertTrue(plugin.isExerciseTypeCorrect(project));
    }

    @Test
    public void exerciseIsCorrectTypeIfItContainsTestthatFile() {
        Path testCasesRoot = TestUtils.getPath(getClass(), "recognition_test_cases");
        Path project = testCasesRoot.resolve("testthat_folder")
                                    .resolve("tests");

        File testThatR = new File(project.toAbsolutePath().toString() + "/testthat.R");
        assertTrue(testThatR.exists());
    }


    @Test
    public void getStudentFilePolicyReturnsRStudentFilePolicy() {
        StudentFilePolicy policy = plugin.getStudentFilePolicy(Paths.get(""));

        assertTrue(policy instanceof RStudentFilePolicy);
    }
}
