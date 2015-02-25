package fi.helsinki.cs.tmc.langs.ant;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.LanguagePlugin;
import fi.helsinki.cs.tmc.langs.RunResult.Status;
import fi.helsinki.cs.tmc.langs.TestDesc;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class AntPluginTest {

    private AntPlugin antPlugin;

    public AntPluginTest() {
        antPlugin = new AntPlugin();
    }

    @Test
    public void testGetLanguageName() {
        assertEquals("apache-ant", antPlugin.getLanguageName());
    }

    @Test
    public void findExercisesReturnsAListOfExerciseDirectories() {
        ImmutableList<Path> dirs = antPlugin.findExercises(getPath("ant_project"));
        Path pathOne = getPath("ant_project");
        Path pathTwo = getPath("ant_project/ant_sub_project");
        assertTrue(dirs.contains(pathOne) && dirs.contains(pathTwo));
    }

    @Test
    public void findExercisesReturnsAListOfRightSize() {
        assertEquals(2, antPlugin.findExercises(getPath("ant_project")).size());
    }

    @Test
    public void findExercisesReturnsAnEmptyListWhenInvalidPath() {
        assertTrue(antPlugin.findExercises(getPath("ant_project/build.xml")).isEmpty());
    }

    @Test
    public void findExercisesReturnsAnEmptyListWhenNoExercisesFound() {
        assertTrue(antPlugin.findExercises(getPath("not_an_ant_project")).isEmpty());
    }

    @Test
    public void scanExerciseReturnExerciseDesc() {
        String name = "Ant Test";
        ExerciseDesc description = antPlugin.scanExercise(getPath("ant_project"), name);
        assertEquals(name, description.name);
        assertEquals(3, description.tests.size());
    }

    @Test
    public void scanExerciseReturnsCorrectTests() {
        ExerciseDesc description = antPlugin.scanExercise(getPath("ant_project"), "AntTestSubject");
        assertEquals(3, description.tests.size());

        TestDesc test = description.tests.get(0);
        assertEquals("oneExTestMethod", test.name);
        assertEquals("one", test.points.get(0));

        test = description.tests.get(2);
        assertEquals("twoExTestMethod", test.name);
        assertEquals("one", test.points.get(0));
        assertEquals("two", test.points.get(1));
    }

    @Test
    public void scanExerciseReturnsNullWhenWrongProjectType() {
        assertNull(antPlugin.scanExercise(getPath("non_ant_project"), "Dummy"));
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
