
package fi.helsinki.cs.tmc.langs.r;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class RPluginTest {
    
    private RPlugin plugin;

    @Before
    public void setUp() {
        plugin = new RPlugin();
    }

    @Test
    public void testGetAvailablePointsCommand(){
        
    }

    @Test
    public void testGetPluginName() {
        assertEquals("r", plugin.getLanguageName());
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
    public void excerciseIsCorrectTypeIfItContainsRhistory() {
        Path testCasesRoot = TestUtils.getPath(getClass(), "recognition_test_cases");
        Path project = testCasesRoot.resolve("rhistory");

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
