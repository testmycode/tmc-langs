package fi.helsinki.cs.tmc.langs;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationError;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class AbstractLanguagePluginTest {

    private PluginImplLanguagePlugin pluginImpl;

    public AbstractLanguagePluginTest() {
        pluginImpl = new PluginImplLanguagePlugin();
    }

    class PluginImplLanguagePlugin extends AbstractLanguagePlugin {

        @Override
        protected boolean isExerciseTypeCorrect(Path path) {
            return new File(path.toString() + File.separatorChar + "build.xml").exists();
        }

        @Override
        public String getLanguageName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public RunResult runTests(Path path) {
            throw new UnsupportedOperationException();
        }
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
