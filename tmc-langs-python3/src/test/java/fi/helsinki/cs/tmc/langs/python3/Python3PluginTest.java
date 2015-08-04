package fi.helsinki.cs.tmc.langs.python3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.TestResult;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import org.junit.Test;

import java.nio.file.Path;

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
    public void testRunTestsRunsTests() {
        Path path = TestUtils.getPath(getClass(), "passing");
        RunResult runResult = python3Plugin.runTests(path);
        assertEquals(RunResult.Status.PASSED, runResult.status);
        TestResult testResult = runResult.testResults.get(0);
        assertTrue(testResult.passed);
        assertEquals("test_new.TestCase.test_new", testResult.name);
        assertEquals(2, testResult.points.size());
        assertTrue(testResult.points.contains("1.2"));
        assertEquals("", testResult.errorMessage);
        assertEquals(0, testResult.backtrace.size());
    }

    @Test
    public void testFailingProjectIsCorrect() {
        Path path = TestUtils.getPath(getClass(), "failing");
        RunResult runResult = python3Plugin.runTests(path);

        assertEquals(RunResult.Status.TESTS_FAILED, runResult.status);
        TestResult testResult = runResult.testResults.get(0);
        assertFalse(testResult.passed);
        assertFalse(testResult.errorMessage.isEmpty());
        assertEquals(6, testResult.backtrace.size());
    }

    @Test
    public void testScanExercise() {
        Path path = TestUtils.getPath(getClass(), "failing");
        ExerciseDesc testDesc = python3Plugin.scanExercise(path, "testname").get();
        assertEquals("testname", testDesc.name);
        assertEquals("test_failing.FailingTest.test_new", testDesc.tests.get(0).name);
        assertEquals(1, testDesc.tests.get(0).points.size());
    }

}
