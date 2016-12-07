package fi.helsinki.cs.tmc.langs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import java.nio.file.Path;

public class NoTestsPluginTest {

    NoTestsPlugin plugin;

    public NoTestsPluginTest() {
        this.plugin = new NoTestsPlugin();
    }

    @Test
    public void readsConfigsProperly() {
        Path project = TestUtils.getPath(getClass(), "notests-points");
        assertTrue(plugin.isExerciseTypeCorrect(project));
        ExerciseDesc desc = plugin.scanExercise(project, "No Tests Exercise").get();
        assertEquals(1, desc.tests.size());
        assertEquals(
                ImmutableList.<String>of("1", "notests").toString(),
                desc.tests.get(0).points.toString());

        RunResult runResult = plugin.runTests(project);
        assertEquals(RunResult.Status.PASSED, runResult.status);
    }

    @Test
    public void worksWithoutPoints() {
        Path project = TestUtils.getPath(getClass(), "notests");
        assertTrue(plugin.isExerciseTypeCorrect(project));
        ExerciseDesc desc = plugin.scanExercise(project, "No Tests Exercise").get();
        assertEquals(1, desc.tests.size());
        assertEquals(0, desc.tests.get(0).points.size());
        RunResult runResult = plugin.runTests(project);
        assertEquals(RunResult.Status.PASSED, runResult.status);
    }
}
