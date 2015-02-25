package fi.helsinki.cs.tmc.langs.ant;

import com.google.common.base.Throwables;
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
    public void scanExerciseReturnExerciseDesc() {
        String name ="Ant Test";
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
