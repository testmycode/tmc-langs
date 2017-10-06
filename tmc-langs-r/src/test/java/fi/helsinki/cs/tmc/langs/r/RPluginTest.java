
package fi.helsinki.cs.tmc.langs.r;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import org.apache.commons.lang3.SystemUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
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
        Path testDir = TestUtils.getPath(getClass(), "passing");
        File resultsJson = new File(testDir.toAbsolutePath().toString() + "/.results.json");
        resultsJson.delete();
    }

    @Test
    public void testGetTestCommand() {
        String[] command = new String[] {"Rscript"};
        String[] args;

        if (SystemUtils.IS_OS_WINDOWS) {
            args = new String[] {"-e", "\"library('tmcRtestrunner');run_tests()\""};
        } else {
            args = new String[] {"-e", "library(tmcRtestrunner);run_tests()"};
        }
        String[] expectedCommand = ArrayUtils.addAll(command, args);
        Assert.assertArrayEquals(expectedCommand,plugin.getTestCommand());
    }

    @Test
    public void testGetAvailablePointsCommand() {
        String[] command = new String[] {"Rscript"};
        String[] args;
        if (SystemUtils.IS_OS_WINDOWS) {
            args = new String[] {"-e", "\"library('tmcRtestrunner');run_available_points()\""};
        } else {
            args = new String[] {"-e", "library(tmcRtestrunner);run_available_points()"};
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
        Path testDir = TestUtils.getPath(getClass(), "passing");
        plugin.scanExercise(testDir, "arithmetics.R");
    }

    @Test
    public void testRunTests() {
        Path testDir = TestUtils.getPath(getClass(), "passing");
        plugin.runTests(testDir);
        File resultsJson = new File(testDir.toAbsolutePath().toString() + "/.results.json");

        assertTrue(resultsJson.exists());
    }

    @Test
    public void excerciseIsCorrectTypeIfItContainsRFolder() {
        Path testCasesRoot = TestUtils.getPath(getClass(), "recognition_test_cases");
        Path project = testCasesRoot.resolve("R_folder");

        assertTrue(plugin.isExerciseTypeCorrect(project));
    }

    @Test
    public void excerciseIsCorrectTypeIfItContainsTestthatFolder() {
        Path testCasesRoot = TestUtils.getPath(getClass(), "recognition_test_cases");
        Path project = testCasesRoot.resolve("testthat_folder");

        assertTrue(plugin.isExerciseTypeCorrect(project));
    }

    @Test
    public void excerciseIsCorrectTypeIfItContainsDescription() {
        Path testCasesRoot = TestUtils.getPath(getClass(), "recognition_test_cases");
        Path project = testCasesRoot.resolve("description");

        assertTrue(plugin.isExerciseTypeCorrect(project));
    }

    @Test
    public void exeriseIsCorrectTypeIfItContainsTestthatFile() {
        Path testCasesRoot = TestUtils.getPath(getClass(), "recognition_test_cases");
        Path project = testCasesRoot.resolve("testthat_folder").resolve("tests").resolve("testthat.R");

        assertTrue(plugin.isExerciseTypeCorrect(project));
    }

    @Test
    public void excerciseIsCorrectTypeIfItContainsResultR() {
        Path testCasesRoot = TestUtils.getPath(getClass(), "recognition_test_cases");
        Path project = testCasesRoot.resolve("result_r");

        assertTrue(plugin.isExerciseTypeCorrect(project));
    }

    @Test
    public void getStudentFilePolicyReturnsRStudentFilePolicy() {
        StudentFilePolicy policy = plugin.getStudentFilePolicy(Paths.get(""));

        assertTrue(policy instanceof RStudentFilePolicy);
    }
}
