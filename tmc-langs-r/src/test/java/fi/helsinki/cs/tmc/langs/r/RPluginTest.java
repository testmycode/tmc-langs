package fi.helsinki.cs.tmc.langs.r;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.domain.RunResult;
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
    public void runTestsCreatesAJson() {
        Path testDir = TestUtils.getPath(getClass(), "passing");
        plugin.runTests(testDir);
        File resultsJson = new File(testDir.toAbsolutePath().toString() + "/.results.json");

        assertTrue(resultsJson.exists());
    }

    @Test
    public void runTestsCreatesJsonWithCorrectStatus() {
        Path testDir = TestUtils.getPath(getClass(), "passing");
        RunResult res = plugin.runTests(testDir);

        assertEquals(RunResult.Status.TESTS_FAILED, res.status);
    }

    @Test
    public void runTestsCreatesJsonWithCorrectNumberOfResults() {
        Path testDir = TestUtils.getPath(getClass(), "passing");
        RunResult res = plugin.runTests(testDir);

        assertEquals(19, res.testResults.size());
    }

    @Test
    public void testResultsFromRunTestsHaveCorrectStatuses() {
        Path testDir = TestUtils.getPath(getClass(), "passing");
        RunResult res = plugin.runTests(testDir);

        for (TestResult tr : res.testResults) {
            if (!tr.getName().equals("Dummy test set to fail")) {
                assertTrue(tr.isSuccessful());
            } else {
                assertFalse(tr.isSuccessful());
            }
        }
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
