package fi.helsinki.cs.tmc.langs.ant;

import com.google.common.base.Throwables;
import fi.helsinki.cs.tmc.langs.LanguagePlugin;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;


public class AntPluginTest {

    private LanguagePlugin antPlugin;

    public AntPluginTest() {
        antPlugin = new AntPlugin();
    }

    @Test
    public void testGetLanguageName() {
        assertEquals("apache-ant", antPlugin.getLanguageName());
    }

    @Test
    public void returnsTrueForExercisesWithBuildFile() {
        assertTrue(antPlugin.isExerciseTypeCorrect(getPath("ant_project")));
    }

    @Test
    public void returnsFalseForExercisesWithoutBuildFile() {
        assertFalse(antPlugin.isExerciseTypeCorrect(getPath("non_ant_project")));
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
