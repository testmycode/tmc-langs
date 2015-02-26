/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

/**
 *
 * @author alpa
 */
public class LanguagePluginAbstractTest {

    PluginFindImpl PluginFindImpl;

    public LanguagePluginAbstractTest() {
        PluginFindImpl = new PluginFindImpl();
    }

    class PluginFindImpl extends LanguagePluginAbstract {

        @Override
        protected boolean isExerciseTypeCorrect(Path path) {
            return new File(path.toString() + File.separatorChar + "build.xml").exists();
        }

        @Override
        public String getLanguageName() {
            return null;
        }

        @Override
        public ExerciseDesc scanExercise(Path path, String exerciseName) {
            return null;
        }

        @Override
        public RunResult runTests(Path path) {
            return null;
        }

        @Override
        public ValidationResult checkCodeStyle(Path path) {
            return null;
        }

    }

    @Test
    public void findExercisesReturnsAListOfExerciseDirectories() {
        ImmutableList<Path> dirs = PluginFindImpl.findExercises(getPath("ant_project"));
        Path pathOne = getPath("ant_project");
        Path pathTwo = getPath("ant_project/ant_sub_project");
        assertTrue(dirs.contains(pathOne) && dirs.contains(pathTwo));
    }

    @Test
    public void findExercisesReturnsAnEmptyListWhenInvalidPath() {
        assertTrue(PluginFindImpl.findExercises(getPath("ant_project/build.xml")).isEmpty());
    }

    @Test
    public void findExercisesReturnsAnEmptyListWhenNoExercisesFound() {
        assertTrue(PluginFindImpl.findExercises(getPath("dummy_project")).isEmpty());
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
