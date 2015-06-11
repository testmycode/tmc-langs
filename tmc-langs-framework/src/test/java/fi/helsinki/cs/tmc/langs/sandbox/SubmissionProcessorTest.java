package fi.helsinki.cs.tmc.langs.sandbox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class SubmissionProcessorTest {

    private Path rootPath;
    private Path sourceDir;
    private Path subSourceDir;
    private Path targetDir;
    private Path sourceFile;
    private Path subSourceFile;

    private SubmissionProcessor processor;

    @Before
    public void setUp() throws IOException {
        processor = new SubmissionProcessor();

        rootPath = Files.createTempDirectory("tmc-test-submissionprocessortest");
        sourceDir = Files.createTempDirectory(rootPath, "source");
        subSourceDir = Files.createTempDirectory(sourceDir, "subdir");
        targetDir = Files.createTempDirectory(rootPath, "target");
        sourceFile = Files.createTempFile(sourceDir, "file", ".tmp");
        subSourceFile = Files.createTempFile(subSourceDir, "subfile", ".tmp");
    }

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
    public void getAbsoluteTargetPathSolvesCorrectTargetPathFoorDirectChildOfRoot() {
        Path result = processor.getAbsoluteTargetPath(sourceDir, targetDir, sourceFile);
        Path correct = targetDir.resolve(sourceFile.getFileName());

        assertEquals(correct.toAbsolutePath(), result.toAbsolutePath());
    }

    @Test
    public void getAbsoluteTargetPathSolvesCorrectTargetPathForSubPath() {
        Path result = processor.getAbsoluteTargetPath(sourceDir, targetDir, subSourceFile);
        Path correct = targetDir.resolve(subSourceDir.getFileName())
                                .resolve(subSourceFile.getFileName());

        assertEquals(correct.toAbsolutePath(), result.toAbsolutePath());
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
}
