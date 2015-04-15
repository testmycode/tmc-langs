package fi.helsinki.cs.tmc.langs.testrunner;

import fi.helsinki.cs.tmc.langs.PluginImplLanguagePlugin;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;
import java.io.File;
import java.nio.file.Path;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestRunnerMainTest {

    TestRunnerMain runner;
    private PluginImplLanguagePlugin pluginImpl;

    public TestRunnerMainTest() {
        runner = new TestRunnerMain();
        pluginImpl = new PluginImplLanguagePlugin();
    }

    /**
     * Test of run method, of class TestRunnerMain.
     */
    public void testRun() throws Exception {
    }

}
