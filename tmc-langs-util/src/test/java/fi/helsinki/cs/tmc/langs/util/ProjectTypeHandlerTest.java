package fi.helsinki.cs.tmc.langs.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import fi.helsinki.cs.tmc.langs.NoLanguagePluginFoundException;
import fi.helsinki.cs.tmc.langs.ant.AntPlugin;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ProjectTypeHandlerTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testProjectTypeOnJavaAntExercise() {
        try {
            ProjectType projectType = ProjectTypeHandler.getProjectType(TestUtils.getPath(getClass(), "arith_funcs"));
            assertEquals(ProjectType.JAVA_ANT, projectType);
        } catch (NoLanguagePluginFoundException e) {
            fail("Couldn't identify arith_funcs project type, expected JAVA_ANT");
        }
    }

    @Test
    public void testLanguagePluginOnJavaAntExercise() {
        try {
            ProjectType projectType = ProjectTypeHandler.getProjectType(TestUtils.getPath(getClass(), "arith_funcs"));
            assertEquals(AntPlugin.class, projectType.getLanguagePlugin().getClass());
        } catch (NoLanguagePluginFoundException e) {
            fail("Couldn't identify arith_funcs exercise language, expected Ant");
        }
    }

    @Test
    public void testDummyProject() throws NoLanguagePluginFoundException {
        exception.expect(NoLanguagePluginFoundException.class);
        exception.expectMessage("No suitable language plugin found.");
        ProjectTypeHandler.getLanguagePlugin(TestUtils.getPath(getClass(), "dummy_project"));
    }
}
