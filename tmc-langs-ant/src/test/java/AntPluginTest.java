/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import fi.helsinki.cs.tmc.langs.LanguagePlugin;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author alpa
 */
public class AntPluginTest {

    private LanguagePlugin antPlugin;

    public AntPluginTest() {
        antPlugin = new AntPlugin();
    }

    @Test
    public void testGetLanguageName() {
        assertEquals("apache-ant", antPlugin.getLanguageName());
    }

}
