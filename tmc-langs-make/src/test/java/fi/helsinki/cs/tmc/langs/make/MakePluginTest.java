package fi.helsinki.cs.tmc.langs.make;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import com.google.common.base.Optional;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MakePluginTest {

    private MakePlugin makePlugin;

    public MakePluginTest() {
        TestUtils.skipIfNotAvailable("valgrind");
        makePlugin = new MakePlugin();
    }

    @Test
    public void testGetLanguageName() {
        assertEquals("make", makePlugin.getLanguageName());
    }

    @Test
    public void testRunTestsWhenBuildFailing() {
        RunResult runResult = makePlugin.runTests(TestUtils.getPath(getClass(), "build-failing"));
        assertEquals(RunResult.Status.COMPILE_FAILED, runResult.status);
    }

    @Test
    public void testMakeProjectWithFailingTestsCompilesAndFailsTests() {
        Path path = TestUtils.getPath(getClass(), "failing");
        RunResult result = makePlugin.runTests(path);

        assertEquals(RunResult.Status.TESTS_FAILED, result.status);
    }

    @Test
    public void testFailingMakeProjectHasOneFailedTest() {
        Path path = TestUtils.getPath(getClass(), "failing");
        RunResult result = makePlugin.runTests(path);

        assertEquals(1, result.testResults.size());
        assertEquals(false, result.testResults.get(0).isSuccessful());
    }

    @Test
    public void testFailingMakeProjectHasCorrectError() {
        Path path = TestUtils.getPath(getClass(), "failing");
        RunResult result = makePlugin.runTests(path);

        assertEquals(
                "[Task 1.1] one returned 2. Should have returned: 1",
                result.testResults.get(0).getMessage());
    }

    @Test
    public void testMakeProjectWithPassingTestsCompilesAndPassesTests() {
        Path path = TestUtils.getPath(getClass(), "passing");
        RunResult result = makePlugin.runTests(path);

        assertEquals(RunResult.Status.PASSED, result.status);
    }

    @Test
    public void testPassingMakeProjectHasOnePassingTest() {
        Path path = TestUtils.getPath(getClass(), "passing");
        RunResult result = makePlugin.runTests(path);
        assertEquals(1, result.testResults.size());
        assertEquals(true, result.testResults.get(0).isSuccessful());
    }

    @Test
    public void testPassingMakeProjectHasNoError() {
        Path path = TestUtils.getPath(getClass(), "passing");
        RunResult result = makePlugin.runTests(path);

        assertEquals("", result.testResults.get(0).getMessage());
    }

    @Test
    public void testPassingMakeProjectHasCorrectPoints() {
        Path path = TestUtils.getPath(getClass(), "passing");
        RunResult result = makePlugin.runTests(path);

        assertEquals("1.1", result.testResults.get(0).points.get(0));
        assertEquals(1, result.testResults.get(0).points.size());
    }

    @Test
    public void testFailingMakeProjectHasCorrectPoints() {
        Path path = TestUtils.getPath(getClass(), "failing");
        RunResult result = makePlugin.runTests(path);

        assertEquals("1.1", result.testResults.get(0).points.get(0));
        assertEquals(1, result.testResults.get(0).points.size());
    }

    @Test
    public void testPassingMakeProjectWillFailWithValgrindFailure() {
        Path path = TestUtils.getPath(getClass(), "valgrind-failing");
        RunResult result = makePlugin.runTests(path);

        assertTrue(result.testResults.get(0).isSuccessful());
        assertFalse(result.testResults.get(1).isSuccessful());
        assertTrue(result.testResults.get(0).getException().size() == 0);
        assertTrue(result.testResults.get(1).getException().size() > 0);
    }

    @Test
    public void testSamePointFromMultipleTestsAllSuccessful() {
        Path path = TestUtils.getPath(getClass(), "passing-same-point");
        RunResult result = makePlugin.runTests(path);

        assertEquals(RunResult.Status.PASSED, result.status);
        assertEquals(2, result.testResults.size());
        assertEquals("1.3", result.testResults.get(0).points.get(1));
        assertEquals("1.3", result.testResults.get(1).points.get(1));
    }

    @Test
    public void testSamePointFromMultipleTestsSomeFailed() {
        Path path = TestUtils.getPath(getClass(), "failing-same-point");
        RunResult result = makePlugin.runTests(path);

        assertEquals(RunResult.Status.TESTS_FAILED, result.status);
        assertEquals(2, result.testResults.size());
        assertEquals("1.3", result.testResults.get(0).points.get(1));
        assertEquals("1.3", result.testResults.get(1).points.get(1));
    }

    @Test
    public void testSuitePointsWithPassingSuite() {
        Path path = TestUtils.getPath(getClass(), "passing-suite");
        RunResult result = makePlugin.runTests(path);

        assertEquals(RunResult.Status.PASSED, result.status);
        assertEquals(3, result.testResults.size());
        assertEquals("1.3", result.testResults.get(2).points.get(0));
        assertEquals("suite.Test-Passing-Suite", result.testResults.get(2).getName());
    }

    @Test
    public void testSuitePointsWithFailingSuite() {
        Path path = TestUtils.getPath(getClass(), "failing-suite");
        RunResult result = makePlugin.runTests(path);

        assertEquals(RunResult.Status.TESTS_FAILED, result.status);
        assertEquals(3, result.testResults.size());
        assertEquals("1.3", result.testResults.get(2).points.get(0));
        assertEquals("suite.Test-Failing-Suite", result.testResults.get(2).getName());
        assertEquals(false, result.testResults.get(2).isSuccessful());
    }

    @Test
    public void testIsExerciseTypeCorrect() {
        Path path = TestUtils.getPath(getClass(), "passing");
        assertTrue(makePlugin.isExerciseTypeCorrect(path));

        path = TestUtils.getPath(getClass(), "failing");
        assertTrue(makePlugin.isExerciseTypeCorrect(path));

        path = TestUtils.getPath(getClass(), "build-failing");
        assertTrue(makePlugin.isExerciseTypeCorrect(path));

        path = TestUtils.getPath(getClass(), "valgrind-failing");
        assertTrue(makePlugin.isExerciseTypeCorrect(path));

        path = TestUtils.getPath(getClass(), "passing-suite");
        assertTrue(makePlugin.isExerciseTypeCorrect(path));

        path = TestUtils.getPath(getClass(), "failing-suite");
        assertTrue(makePlugin.isExerciseTypeCorrect(path));

        path = TestUtils.getPath(getClass(), "");
        assertFalse(makePlugin.isExerciseTypeCorrect(path));
    }

    @Test
    public void testScanExerciseWithPassing() {
        Path path = TestUtils.getPath(getClass(), "passing");
        Optional<ExerciseDesc> optional = makePlugin.scanExercise(path, "passing exercise");
        assertTrue(optional.isPresent());

        ExerciseDesc desc = optional.get();

        assertEquals("passing exercise", desc.name);

        assertEquals(1, desc.tests.size());
        assertEquals("test.test_one", desc.tests.get(0).name);

        assertEquals(1, desc.tests.get(0).points.size());
        assertEquals("1.1", desc.tests.get(0).points.get(0));
    }

    @Test
    public void testScanExerciseWithNonexistentProject() {
        Path path = TestUtils.getPath(getClass(), "");
        Optional<ExerciseDesc> optional = makePlugin.scanExercise(path, "");

        assertFalse(optional.isPresent());
    }

    @Test
    public void testGetStudentFilePolicy() {
        Path path = TestUtils.getPath(getClass(), "passing");
        StudentFilePolicy studentFilePolicy = makePlugin.getStudentFilePolicy(path);

        assertTrue(studentFilePolicy.getClass().equals(new MakeStudentFilePolicy(path).getClass()));
    }

    @Test
    public void testBackwardCompatibilityWithPassingMakeProject() {
        Path path = TestUtils.getPath(getClass(), "passing-old");
        RunResult result = makePlugin.runTests(path);
        assertEquals(1, result.testResults.size());
        assertEquals(true, result.testResults.get(0).isSuccessful());
    }

    @Test
    public void testBackwardCompatibilityWithFailingMakeProject() {
        Path path = TestUtils.getPath(getClass(), "failing-old");
        RunResult result = makePlugin.runTests(path);
        assertEquals(1, result.testResults.size());
        assertEquals(false, result.testResults.get(0).isSuccessful());
    }

    @Test
    public void isExerciseCorrectTypeDoesIsNotFooledByDirectoryNamedMakefile() throws IOException {
        Path parent = Files.createTempDirectory("tmc-make-test");
        Path make = parent.resolve("Makefile");
        Files.createDirectory(make);

        assertFalse(makePlugin.isExerciseTypeCorrect(parent));

        Files.delete(make);
        Files.delete(parent);
    }

    @Test
    public void testProjectWithUnexecutableArtifacts() {
        Path path = TestUtils.getPath(getClass(), "wrong-permissions");
        RunResult result = makePlugin.runTests(path);
        assertEquals(result.status, RunResult.Status.PASSED);
    }
}
