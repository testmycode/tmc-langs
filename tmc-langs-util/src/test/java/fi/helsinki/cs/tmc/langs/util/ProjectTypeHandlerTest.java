package fi.helsinki.cs.tmc.langs.util;

import com.google.common.base.Optional;
import fi.helsinki.cs.tmc.langs.ant.AntPlugin;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class ProjectTypeHandlerTest {
    private ProjectTypeHandler handler;

    @Before
    public void setUp() throws Exception {
        handler = new ProjectTypeHandler();
    }

    @Test
    public void testProjectTypeOnJavaAntExercise() {
        Optional<ProjectType> projectType = handler.getProjectType(TestUtils.getPath(getClass(), "arith_funcs"));
        if (projectType.isPresent()) {
            assertEquals(ProjectType.JAVA_ANT, projectType.get());
        } else {
            fail("Couldn't identify arith_funcs project type, expected JAVA_ANT");
        }
    }

    @Test
    public void testLanguagePluginOnJavaAntExercise() {
        Optional<ProjectType> projectType = handler.getProjectType(TestUtils.getPath(getClass(), "arith_funcs"));
        if (projectType.isPresent()) {
            assertEquals(AntPlugin.class, projectType.get().getLanguagePlugin().getClass());
        } else {
            fail("Couldn't identify arith_funcs exercise language, expected Ant");
        }
    }

    @Test
    public void testDummyProject() {
        assertEquals(Optional.absent(), handler.getLanguagePlugin(TestUtils.getPath(getClass(), "dummy_project")));
    }
}