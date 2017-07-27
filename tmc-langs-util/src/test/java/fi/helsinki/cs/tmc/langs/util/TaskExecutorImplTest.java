package fi.helsinki.cs.tmc.langs.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;
import fi.helsinki.cs.tmc.langs.java.ant.AntPlugin;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;


public class TaskExecutorImplTest {

    public TaskExecutorImplTest() {
        TestUtils.skipIfNotAvailable("tar");
    }
    
    @Test
    public void prepareSandboxTaskTest() throws IOException, NoLanguagePluginFoundException,
            ArchiveException {
        TaskExecutor executor = new TaskExecutorImpl();

        Path exercisePath = TestUtils.getPath(getClass(), "arith_funcs");
        Path submissionPath = TestUtils.getPath(getClass(), "arith_funcs_solution");
        Path outputDirectory = Files.createTempDirectory("output-directory");
        Path outputPath = outputDirectory.resolve("output.tar");
        Path tmcRunPath = Files.createTempFile("fake-tmc-run", ".sh");
        Path tmcLangsPath = Files.createTempFile("fake-tmc-langs", ".jar");

        executor.prepareSandboxTask(exercisePath, submissionPath, outputPath, tmcRunPath, tmcLangsPath);
        Path testDirectory = Files.createTempDirectory("test-directory");

        Process untarringProcess = new ProcessBuilder("tar", "-C", testDirectory.toString(), "-xf",
                outputPath.toString()).start();

        assertTrue(Files.exists(outputPath));
        assertTrue(Files.exists(testDirectory.resolve("tmc-langs.jar")));
        assertTrue(Files.exists(testDirectory.resolve("tmc-run.sh")));

        FileUtils.forceDelete(outputDirectory.toFile());
        FileUtils.forceDelete(tmcRunPath.toFile());
        FileUtils.forceDelete(tmcLangsPath.toFile());
        FileUtils.forceDelete(testDirectory.toFile());
    }
}