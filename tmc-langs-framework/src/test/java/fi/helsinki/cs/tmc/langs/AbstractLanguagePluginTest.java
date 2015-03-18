package fi.helsinki.cs.tmc.langs;

import com.google.common.collect.ImmutableList;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

public class AbstractLanguagePluginTest {

    private PluginFindImplLanguagePlugin pluginFindImpl;

    public AbstractLanguagePluginTest() {
        pluginFindImpl = new PluginFindImplLanguagePlugin();
    }

    class PluginFindImplLanguagePlugin extends AbstractLanguagePlugin {

        @Override
        protected boolean isExerciseTypeCorrect(Path path) {
            return new File(path.toString() + File.separatorChar + "build.xml").exists();
        }

        @Override
        public String getLanguageName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ExerciseDesc scanExercise(Path path, String exerciseName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public RunResult runTests(Path path) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ValidationResult checkCodeStyle(Path path) {
            throw new UnsupportedOperationException();
        }

    }

    @Test
    public void findExercisesReturnsAListOfExerciseDirectories() {
        ImmutableList<Path> dirs = pluginFindImpl.findExercises(TestUtils.getPath(getClass(), "ant_project"));
        Path pathOne = TestUtils.getPath(getClass(), "ant_project");
        Path pathTwo = TestUtils.getPath(getClass(), "ant_project/ant_sub_project");
        assertTrue(dirs.contains(pathOne) && dirs.contains(pathTwo));
    }

    @Test
    public void findExercisesReturnsAnEmptyListWhenInvalidPath() {
        assertTrue(pluginFindImpl.findExercises(TestUtils.getPath(getClass(), "ant_project/build.xml")).isEmpty());
    }

    @Test
    public void findExercisesReturnsAnEmptyListWhenNoExercisesFound() {
        assertTrue(pluginFindImpl.findExercises(TestUtils.getPath(getClass(), "dummy_project")).isEmpty());
    }
}
