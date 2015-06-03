package fi.helsinki.cs.tmc.langs.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;

import fi.helsinki.cs.tmc.langs.utils.TestUtils;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationError;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;

import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class AbstractJavaPluginTest {

    private PluginImplLanguagePlugin pluginImpl;

    public AbstractJavaPluginTest() {
        pluginImpl = new PluginImplLanguagePlugin();
    }

    @Test
    public void findExercisesReturnsAListOfExerciseDirectories() {
        ImmutableList<Path> dirs = pluginImpl.findExercises(TestUtils.getPath(getClass(), "ant_project"));
        Path pathOne = TestUtils.getPath(getClass(), "ant_project");
        Path pathTwo = TestUtils.getPath(getClass(), "ant_project/ant_sub_project");
        assertTrue(dirs.contains(pathOne) && dirs.contains(pathTwo));
    }

    @Test
    public void findExercisesReturnsAnEmptyListWhenInvalidPath() {
        assertTrue(pluginImpl.findExercises(TestUtils.getPath(getClass(), "ant_project/build.xml")).isEmpty());
    }

    @Test
    public void findExercisesReturnsAnEmptyListWhenNoExercisesFound() {
        assertTrue(pluginImpl.findExercises(TestUtils.getPath(getClass(), "dummy_project")).isEmpty());
    }

    @Test
    public void testCheckCodeStyle() {
        ValidationResult result = pluginImpl.checkCodeStyle(TestUtils.getPath(getClass(), "most_errors"));
        Map<File, List<ValidationError>> res = result.getValidationErrors();
        assertEquals("Should be one erroneous file", 1, res.size());
        for (File file : res.keySet()) {
            List<ValidationError> errors = res.get(file);
            assertEquals("Should return the right amount of errors", 23, errors.size());
        }
    }

    @Test
    public void testCheckCodeStyleWithUntestableProject() {
        ValidationResult result = pluginImpl.checkCodeStyle(TestUtils.getPath(getClass(), "dummy_project"));
        assertNull(result);
    }
}
