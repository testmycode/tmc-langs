
import fi.helsinki.cs.tmc.langs.LanguagePlugin;
import static org.junit.Assert.*;

import fi.helsinki.cs.tmc.langs.ant.AntPlugin;
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
