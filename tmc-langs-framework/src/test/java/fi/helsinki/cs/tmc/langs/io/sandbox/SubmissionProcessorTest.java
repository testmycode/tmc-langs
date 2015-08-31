package fi.helsinki.cs.tmc.langs.io.sandbox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.io.FileUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SubmissionProcessorTest {

    private Path rootPath;
    private Path sourceDir;
    private Path subSourceDir;
    private Path targetDir;
    private Path sourceFile;
    private Path subSourceFile;

    private StudentFileAwareSubmissionProcessor processor;

    @Before
    public void setUp() throws IOException {
        processor = new StudentFileAwareSubmissionProcessor();

        rootPath = Files.createTempDirectory("tmc-test-submissionprocessortest");
        sourceDir = Files.createTempDirectory(rootPath, "source");
        subSourceDir = Files.createTempDirectory(sourceDir, "subdir");
        targetDir = Files.createTempDirectory(rootPath, "target");
        sourceFile = Files.createTempFile(sourceDir, "file", ".tmp");
        subSourceFile = Files.createTempFile(subSourceDir, "subfile", ".tmp");
    }

    /**
     * Delete files after tests are run.
     */
    @After
    public void tearDown() {
        rootPath.toFile().delete();
        sourceDir.toFile().delete();
        subSourceDir.toFile().delete();
        targetDir.toFile().delete();
        sourceFile.toFile().delete();
        subSourceFile.toFile().delete();
    }

    @Test
    public void moveFileMovesFiles() throws IOException {
        processor.moveFile(sourceDir, sourceFile, targetDir);

        Path targetFile = targetDir.resolve(sourceFile.getFileName());
        assertTrue(Files.exists(targetFile));
    }

    @Test
    public void moveFileMovesFilesInSubfolders() throws IOException {
        processor.moveFile(sourceDir, subSourceFile, targetDir);

        Path targetFile = targetDir.resolve(sourceDir.relativize(subSourceFile));
        assertTrue(Files.exists(targetFile));
    }

    @Test
    public void moveFileReplacesExistingFiles() throws IOException {
        Path path = targetDir.resolve("temp");
        Files.createFile(path);
        FileUtils.write(path.toFile(), "Initial content");

        assertTrue(Files.exists(path));
        assertEquals("Initial content", FileUtils.readFileToString(path.toFile()));

        Path newFile = sourceDir.resolve("temp");
        Files.createFile(newFile);
        FileUtils.write(newFile.toFile(), "New content");

        processor.moveFile(sourceDir, newFile, targetDir);

        assertTrue(Files.exists(path));
        assertEquals("New content", FileUtils.readFileToString(path.toFile()));

        path.toFile().delete();
        newFile.toFile().delete();
    }

    @Test
    public void moveFilesMovesAllFilesInDirectory() {
        processor.moveFiles(sourceDir, targetDir);

        Path targetFile = targetDir.resolve(sourceFile.getFileName());
        Path targetSubFile = targetDir.resolve(sourceDir.relativize(subSourceFile));

        assertTrue(Files.exists(targetFile));
        assertTrue(Files.exists(targetSubFile));
    }
}
