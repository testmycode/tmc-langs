/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.helsinki.cs.tmc.langs.maven;

import com.google.common.base.Throwables;
import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationError;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;
import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author alpa
 */
public class MavenPluginTest {

    MavenPlugin mavenPlugin;

    public MavenPluginTest() {
        mavenPlugin = new MavenPlugin();
    }

    @Test
    public void testGetLanguageName() {
        assertEquals("apache-maven", mavenPlugin.getLanguageName());
    }

    @Test
    public void testCheckCodeStyle() {
        ValidationResult result = mavenPlugin.checkCodeStyle(getPath("most_errors"));
        Map<File, List<ValidationError>> res = result.getValidationErrors();
        assertEquals("Should be one erroneous file", 1, res.size());
        for (File file : res.keySet()) {
            List<ValidationError> errors = res.get(file);
            assertEquals("Should return the right amount of errors", 23, errors.size());
        }
    }

    @Test
    public void testCheckCodeStyleWithUntestableProject() {
        File projectToTest = new File("src/test/resources/arith_funcs/");
        ValidationResult result = mavenPlugin.checkCodeStyle(projectToTest.toPath());
        assertNull(result);
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
