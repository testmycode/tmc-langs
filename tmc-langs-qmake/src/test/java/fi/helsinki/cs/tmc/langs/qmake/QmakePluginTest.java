package fi.helsinki.cs.tmc.langs.qmake;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import com.google.common.base.Optional;

import org.junit.Test;

import java.nio.file.Path;


public class QmakePluginTest {

    private final QmakePlugin qmakePlugin;

    public QmakePluginTest() {
        qmakePlugin = new QmakePlugin();
    }

    @Test
    public void testGetLanguageName() {
        assertEquals("qmake", qmakePlugin.getLanguageName());
    }

    @Test
    public void testQtBuildFailingWithNoLib() {
        assertEquals(RunResult.Status.PASSED, runTests("passing_nolib"));
    }

    @Test
    public void testQtBuildFailingWithSingleLib() {
        assertEquals("Failing compile with single lib",
                RunResult.Status.COMPILE_FAILED,
                runTests("failing_compile_single_lib_compiling"));
    }

    @Test
    public void testQtLibBuildFailing() {
        assertEquals(RunResult.Status.COMPILE_FAILED, runTests("failing_single_lib_not_compiling"));
    }

    @Test
    public void testQTestsFailingNoLib() {
        assertEquals(RunResult.Status.TESTS_FAILED, runTests("failing_nolib"));
    }

    @Test
    public void testQtTestsPassingSingleLib() {
        assertEquals(RunResult.Status.PASSED, runTests("passing_single_lib"));
    }

    @Test
    public void testQtTestsFailingNoLibSamePoints() {
        assertEquals("Failing no library",
                RunResult.Status.TESTS_FAILED,
                runTests("failing_nolib_same_point"));
    }

    @Test
    public void testScanExerciseWithFailingSamePoints() {
        Optional<ExerciseDesc> optional = scanExercise("failing_single_lib_same_point");
        assertTrue(optional.isPresent());

        ExerciseDesc desc = optional.get();

        assertEquals("failing_single_lib_same_point", desc.name);

        assertEquals(3, desc.tests.size());
        assertEquals("test_function_two_here", desc.tests.get(0).name);

        assertEquals(1, desc.tests.get(0).points.size());
        assertEquals("2", desc.tests.get(0).points.get(0));
    }

    @Test
    public void testIsExerciseTypeCorrect() {
        assertTrue(isExerciseTypeCorrect("passing_nolib"));
        assertTrue(isExerciseTypeCorrect("passing_nolib_same_point"));
        assertTrue(isExerciseTypeCorrect("passing_single_lib"));
        assertTrue(isExerciseTypeCorrect("passing_single_lib_same_point"));

        assertTrue(isExerciseTypeCorrect("passing_multiple_lib"));
        assertTrue(isExerciseTypeCorrect("passing_multiple_lib_same_point"));

        assertTrue(isExerciseTypeCorrect("failing_single_lib_not_compiling"));
        assertTrue(isExerciseTypeCorrect("failing_nolib"));
        assertTrue(isExerciseTypeCorrect("failing_nolib_same_point"));
        assertTrue(isExerciseTypeCorrect("failing_single_lib"));
        assertTrue(isExerciseTypeCorrect("failing_single_lib_not_compiling"));
        assertTrue(isExerciseTypeCorrect("failing_single_lib_same_point"));
        assertFalse(isExerciseTypeCorrect(""));
    }

    @Test
    public void testScanExerciseWithNonexistentProject() {
        Optional<ExerciseDesc> optional = scanExercise("");
        assertFalse(optional.isPresent());
    }

    private RunResult.Status runTests(String testExercise) {
        return qmakePlugin.runTests(TestUtils.getPath(getClass(), testExercise)).status;
    }

    private Optional<ExerciseDesc> scanExercise(String testExercise) {
        return qmakePlugin.scanExercise(TestUtils.getPath(getClass(), testExercise), testExercise);
    }

    private boolean isExerciseTypeCorrect(String exercisePath) {
        Path path = TestUtils.getPath(getClass(), exercisePath);
        return qmakePlugin.isExerciseTypeCorrect(path);
    }
}
