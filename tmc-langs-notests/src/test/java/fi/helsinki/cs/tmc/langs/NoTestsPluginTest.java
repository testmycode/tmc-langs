package fi.helsinki.cs.tmc.langs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.TestCase;
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
        Path project = TestUtils.getPath(getClass(), "notests");
        System.out.println(project.toAbsolutePath().toString());
        assertTrue(plugin.isExerciseTypeCorrect(project));
        ExerciseDesc desc = plugin.scanExercise(project, "No Tests Exercise").get();
        assertEquals(1, desc.tests.size());
        assertEquals(
                ImmutableList.<String>of("1", "notests").toString(),
                desc.tests.get(0).points.toString());

        TestCase testCase = plugin.runTests(project);
        assertEquals(TestCase.Status.PASSED, testCase.status);
    }
}
