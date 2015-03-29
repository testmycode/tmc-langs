package fi.helsinki.cs.tmc.langs.util;

import fi.helsinki.cs.tmc.langs.ant.AntPlugin;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ProjectTypeHandlerTest {
    private ProjectTypeHandler handler;

    @Before
    public void setUp() throws Exception {
        handler = new ProjectTypeHandler();
    }

    @Test
    public void testProjectTypeOnJavaAntExercise() {
        assertEquals(ProjectType.JAVA_ANT, handler.getProjectType(TestUtils.getPath(getClass(), "arith_funcs")));
    }

    @Test
    public void testLanguagePluginOnJavaAntExercise() {
        assertEquals(AntPlugin.class, handler.getLanguagePlugin(TestUtils.getPath(getClass(), "arith_funcs")).getClass());
    }

    @Test
    public void testDummyProject() {
        assertNull(handler.getLanguagePlugin(TestUtils.getPath(getClass(), "dummy_project")));
    }
}