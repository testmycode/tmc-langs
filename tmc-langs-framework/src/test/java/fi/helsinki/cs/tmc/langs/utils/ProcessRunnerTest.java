package fi.helsinki.cs.tmc.langs.utils;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProcessRunnerTest extends TestCase {

    private Path folder;

    @Override
    protected void setUp() throws Exception {
        this.folder = Files.createTempDirectory("process-runner");
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        FileUtils.deleteDirectory(this.folder.toFile());
        super.tearDown();
    }

    @Test
    public void testFailingProcessHasCorrectStatus() throws Exception {
        ProcessRunner runner = new ProcessRunner(new String[]{"false"}, this.folder);
        assertTrue(0 != runner.call().statusCode);
    }

    @Test
    public void testProcessHasCorrectStatus() throws Exception {
        ProcessRunner runner = new ProcessRunner(new String[]{"java", "-version"}, this.folder);
        assertEquals(0, runner.call().statusCode);
    }

    @Test
    public void testProcessHasCorrectOutput() throws Exception {
        ProcessRunner runner = new ProcessRunner(
                new String[]{"echo", "This is a test."},
                this.folder
        );
        String output = runner.call().output;
        assertTrue(output.contains("This is a test."));
    }

    @Test
    public void testProcessHasCorrectErrorOutput() throws Exception {
        ProcessRunner runner = new ProcessRunner(new String[]{"java"}, this.folder);
        String error = runner.call().errorOutput;
        assertTrue(error.contains("java"));
    }

    @Test
    public void testProcessIsExecutedInRightFolder() throws Exception {
        ProcessRunner runner = new ProcessRunner(
                new String[]{"mkdir", "special-folder"},
                this.folder
        );
        runner.call();
        assertTrue(Paths.get(this.folder.toAbsolutePath().toString(), "special-folder")
                .toFile().isDirectory());
    }
}
