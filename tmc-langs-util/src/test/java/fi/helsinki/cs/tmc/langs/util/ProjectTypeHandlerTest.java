package fi.helsinki.cs.tmc.langs.util;

import com.google.common.base.Throwables;
import fi.helsinki.cs.tmc.langs.ant.AntPlugin;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class ProjectTypeHandlerTest {
    private ProjectTypeHandler handler;

    @Before
    public void setUp() throws Exception {
        handler = new ProjectTypeHandler();
    }

    @Test
    public void returnsCorrectTypeOnJavaAntExercise() {
        assertEquals(ProjectType.JAVA_ANT, handler.getProjectType(getPath("arith_funcs")));
    }

    @Test
    public void returnsCorrectLanguagePluginOnJavaAntExercise() {
        assertEquals(AntPlugin.class, handler.getLanguagePlugin(getPath("arith_funcs")).getClass());
    }

    @Test
    public void returnsNullIfNoPluginCanRunTheExercise() {
        assertNull(handler.getLanguagePlugin(getPath("dummy_project")));
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