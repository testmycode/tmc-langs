package fi.helsinki.cs.tmc.langs.python3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.abstraction.Strategy;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.TestResult;
import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class Python3PluginTest {

    private Python3Plugin python3Plugin;

    @Before
    public void setUp() {
        python3Plugin = new Python3Plugin();
    }

    @Test
    public void testGetLanguageName() {
        assertEquals("python3", python3Plugin.getLanguageName());
    }

    @Test
    public void exerciseIsCorrectTypeIfItContainsSetupPy() {
        Path testCasesRoot = TestUtils.getPath(getClass(), "recognition_test_cases");
        Path project = testCasesRoot.resolve("setup_py");

        assertTrue(python3Plugin.isExerciseTypeCorrect(project));
    }

    @Test
    public void exerciseIsCorrectTypeIfItContainsRequirementsTxt() {
        Path testCasesRoot = TestUtils.getPath(getClass(), "recognition_test_cases");
        Path project = testCasesRoot.resolve("requirements_txt");

        assertTrue(python3Plugin.isExerciseTypeCorrect(project));
    }

    @Test
    public void exerciseIsCorrectTypeIfItContainsTestFolderInitPy() {
        Path testCasesRoot = TestUtils.getPath(getClass(), "recognition_test_cases");
        Path project = testCasesRoot.resolve("test_init_py");

        assertTrue(python3Plugin.isExerciseTypeCorrect(project));
    }

    @Test
    public void exerciseIsCorrectTypeIfItContainsTmcLibrary() {
        Path testCasesRoot = TestUtils.getPath(getClass(), "recognition_test_cases");
        Path project = testCasesRoot.resolve("tmc_lib");

        assertTrue(python3Plugin.isExerciseTypeCorrect(project));
    }

    @Test
    public void exerciseIsNotCorrectTypeIfItContainsOnlyPomXml() {
        Path testCasesRoot = TestUtils.getPath(getClass(), "recognition_test_cases");
        Path project = testCasesRoot.resolve("pom_xml");

        assertFalse(python3Plugin.isExerciseTypeCorrect(project));
    }

    @Test
    public void getStudentFilePolicyReturnsPython3StudentFilePolicy() {
        StudentFilePolicy policy = python3Plugin.getStudentFilePolicy(Paths.get(""));

        assertTrue(policy instanceof Python3StudentFilePolicy);
    }

    @Test()
    public void checkCodeStyleReturnsEmptyResult() {
        Path path = TestUtils.getPath(getClass(), "passing");
        ValidationResult result = python3Plugin.checkCodeStyle(path, new Locale("en"));
        assertTrue(result.getStrategy() == Strategy.DISABLED);
    }

    @Test
    public void testRunTestsRunsTests() {
        Path path = TestUtils.getPath(getClass(), "passing");
        RunResult runResult = python3Plugin.runTests(path);
        assertEquals(RunResult.Status.PASSED, runResult.status);
        TestResult testResult = runResult.testResults.get(0);
        assertTrue(testResult.isSuccessful());
        assertNotEquals("test.test_new.TestCase.test_new", testResult.getName());
        assertEquals("TestCase: test_new", testResult.getName());
        assertEquals(2, testResult.points.size());
        assertTrue(testResult.points.contains("1.2"));
        assertEquals("", testResult.getMessage());
        assertEquals(0, testResult.getException().size());
    }

    @Test
    public void testFailingProjectIsCorrect() {
        Path path = TestUtils.getPath(getClass(), "failing");
        RunResult runResult = python3Plugin.runTests(path);

        assertEquals(RunResult.Status.TESTS_FAILED, runResult.status);
        TestResult testResult = runResult.testResults.get(0);
        assertFalse(testResult.isSuccessful());
        assertFalse(testResult.getMessage().isEmpty());
        assertEquals(6, testResult.getException().size());
    }

    @Test
    public void testFailingTestNoBoolIsBool() {
        Path path = TestUtils.getPath(getClass(), "failparsing");
        RunResult runResult = python3Plugin.runTests(path);

        assertEquals(RunResult.Status.TESTS_FAILED, runResult.status);
        TestResult testResult = runResult.testResults.get(0);
        assertEquals(
                "No false is not true at beginning\n   newlines  are:kept:not:split",
                testResult.getMessage());
    }

    @Test
    public void testScanExercise() {
        Path path = TestUtils.getPath(getClass(), "failing");
        ExerciseDesc testDesc = python3Plugin.scanExercise(path, "testname").get();
        assertEquals("testname", testDesc.name);
        assertEquals("test.test_failing.FailingTest.test_new", testDesc.tests.get(0).name);
        assertEquals(1, testDesc.tests.get(0).points.size());
    }

    @Test
    public void testComplexExercise() {
        Path path = TestUtils.getPath(getClass(), "complex");
        RunResult runResult = python3Plugin.runTests(path);

        assertEquals(RunResult.Status.PASSED, runResult.status);
        assertEquals(38, runResult.testResults.size());
    }
}
