package fi.helsinki.cs.tmc.langs.util;

import fi.helsinki.cs.tmc.langs.ant.AntPlugin;
import org.junit.Test;

import static org.junit.Assert.*;

public class ProjectTypeTest {

    @Test
    public void testProjectTypeJAVA_ANT() {
        ProjectType test = ProjectType.JAVA_ANT;
        assertTrue(test.getLanguagePlugin().getLanguageName().equals(new AntPlugin().getLanguageName()));
    }
}