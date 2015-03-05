/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.helsinki.cs.tmc.langs.maven;

import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;
import java.nio.file.Path;
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
}
