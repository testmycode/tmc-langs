package fi.helsinki.cs.tmc.langs;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;
import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;
import static org.junit.Assert.*;

public class AbstractLanguagePluginTest {

    PluginFindImplLanguagePlugin pluginFindImpl;

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
        ImmutableList<Path> dirs = pluginFindImpl.findExercises(getPath("ant_project"));
        Path pathOne = getPath("ant_project");
        Path pathTwo = getPath("ant_project/ant_sub_project");
        assertTrue(dirs.contains(pathOne) && dirs.contains(pathTwo));
    }

    @Test
    public void findExercisesReturnsAnEmptyListWhenInvalidPath() {
        assertTrue(pluginFindImpl.findExercises(getPath("ant_project/build.xml")).isEmpty());
    }

    @Test
    public void findExercisesReturnsAnEmptyListWhenNoExercisesFound() {
        assertTrue(pluginFindImpl.findExercises(getPath("dummy_project")).isEmpty());
    }

    private Path getPath(String location) {
        Path path;
        try {
            path = Paths.get(getClass().getResource(File.separatorChar + location).toURI());
        } catch (URISyntaxException e) {
            throw Throwables.propagate(e);
        }
        return path;
    }

}
