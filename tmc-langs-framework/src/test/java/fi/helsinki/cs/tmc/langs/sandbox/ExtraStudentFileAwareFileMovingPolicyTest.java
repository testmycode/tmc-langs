package fi.helsinki.cs.tmc.langs.sandbox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExtraStudentFileAwareFileMovingPolicyTest {

    private Path rootPath;
    private Path sourceDir;
    private Path targetDir;

    private SubmissionProcessor processor;
    private FileMovingPolicy fileMovingPolicy;

    @Before
    public void setUp() throws IOException {

        fileMovingPolicy = new ExtraStudentFileAwareFileMovingPolicy() {
            @Override
            public boolean shouldMoveFile(Path path) {
                return false;
            }
        };
        processor = new SubmissionProcessor(fileMovingPolicy);

        rootPath = Files.createTempDirectory("tmc-test-submissionprocessortest");
        sourceDir = Files.createTempDirectory(rootPath, "source");
        targetDir = Files.createTempDirectory(rootPath, "target");
    }

    /**
     * Delete files after tests are run.
     */
    @After
    public void tearDown() {
        rootPath.toFile().delete();
        sourceDir.toFile().delete();
        targetDir.toFile().delete();
    }

    @Test
    public void movesSingleExtraStudentFiles() throws IOException {
        Path path = targetDir.resolve(".tmcproject.yml");
        Files.createFile(path);
        FileUtils.write(path.toFile(), "extra_student_files: temp");

        Path newFile = sourceDir.resolve("temp");
        Files.createFile(newFile);
        FileUtils.write(newFile.toFile(), "Temporary");

        assertTrue(fileMovingPolicy.shouldMove(newFile, sourceDir, targetDir));

        processor.moveFiles(sourceDir, targetDir);

        Path targetFile = targetDir.resolve(sourceDir.relativize(newFile));

        assertTrue(Files.exists(targetFile));
        assertEquals("Temporary", FileUtils.readFileToString(targetFile.toFile()));

        path.toFile().delete();
        newFile.toFile().delete();
        targetFile.toFile().delete();
    }

    @Test
    public void movesMultipleExtraStudentFiles() throws IOException {
        Path path = targetDir.resolve(".tmcproject.yml");
        Files.createFile(path);
        FileUtils.write(path.toFile(), "extra_student_files:\n  - temp\n  - temporary.txt");

        Path temp = sourceDir.resolve("temp");
        Files.createFile(temp);
        FileUtils.write(temp.toFile(), "Temporary 1");
        Path temporary = sourceDir.resolve("temporary.txt");
        Files.createFile(temporary);
        FileUtils.write(temporary.toFile(), "Temporary 2");

        assertTrue(fileMovingPolicy.shouldMove(temp, sourceDir, targetDir));
        assertTrue(fileMovingPolicy.shouldMove(temporary, sourceDir, targetDir));

        processor.moveFiles(sourceDir, targetDir);

        Path targetTemp = targetDir.resolve(sourceDir.relativize(temp));
        Path targetTemporary = targetDir.resolve(sourceDir.relativize(temporary));

        assertTrue(Files.exists(targetTemp));
        assertTrue(Files.exists(targetTemporary));
        assertEquals("Temporary 1", FileUtils.readFileToString(targetTemp.toFile()));
        assertEquals("Temporary 2", FileUtils.readFileToString(targetTemporary.toFile()));

        path.toFile().delete();
        temp.toFile().delete();
        temporary.toFile().delete();
        targetTemp.toFile().delete();
        targetTemporary.toFile().delete();
    }

    @Test
    public void movesExtraStudentFilesInSubfolders() throws IOException {
        Path path = targetDir.resolve(".tmcproject.yml");
        Files.createFile(path);
        FileUtils.write(path.toFile(), "extra_student_files:\n  - subdir/temp");

        Path dir = sourceDir.resolve("subdir");
        Files.createDirectory(dir);
        Path temp = dir.resolve("temp");
        Files.createFile(temp);
        FileUtils.write(temp.toFile(), "Temporary");

        assertTrue(fileMovingPolicy.shouldMove(temp, sourceDir, targetDir));

        processor.moveFiles(sourceDir, targetDir);

        Path targetTemp = targetDir.resolve(sourceDir.relativize(temp));

        assertTrue(Files.exists(targetTemp));
        assertEquals("Temporary", FileUtils.readFileToString(targetTemp.toFile()));

        path.toFile().delete();
        temp.toFile().delete();
        dir.toFile().delete();
        targetTemp.toFile().delete();
    }

    @Test
    public void doesNotMoveTMCProjectYML() throws IOException {
        Path path = targetDir.resolve(".tmcproject.yml");
        Files.createFile(path);
        FileUtils.write(path.toFile(), "extra_student_files:\n  - .tmcproject.yml");

        Path temp = sourceDir.resolve(".tmcproject.yml");
        Files.createFile(temp);
        FileUtils.write(temp.toFile(), "extra_student_files:\n  - temp");

        assertFalse(fileMovingPolicy.shouldMove(temp, sourceDir, targetDir));

        processor.moveFiles(sourceDir, targetDir);

        Path targetTemp = targetDir.resolve(sourceDir.relativize(temp));

        assertTrue(Files.exists(targetTemp));
        assertEquals("extra_student_files:\n  - .tmcproject.yml", FileUtils.readFileToString(targetTemp.toFile()));

        path.toFile().delete();
        temp.toFile().delete();
        targetTemp.toFile().delete();
    }

    @Test
    public void doesNotMoveDirectories() throws IOException {
        Path dir = sourceDir.resolve("subdir");
        Files.createDirectory(dir);

        assertFalse(fileMovingPolicy.shouldMove(dir, sourceDir, targetDir));

        processor.moveFiles(sourceDir, targetDir);

        Path targetSubdir = targetDir.resolve(sourceDir.relativize(dir));

        assertFalse(Files.exists(targetSubdir));

        dir.toFile().delete();
        targetSubdir.toFile().delete();
    }

    @Test
    public void doesNotMoveNonexistentFiles() {
        Path file = sourceDir.resolve("nonexistent");

        assertFalse(fileMovingPolicy.shouldMove(file, sourceDir, targetDir));

        processor.moveFiles(sourceDir, targetDir);

        Path targetFile = targetDir.resolve(sourceDir.relativize(file));

        assertFalse(Files.exists(targetFile));

        file.toFile().delete();
        targetFile.toFile().delete();
    }

    @Test
    public void doesNotMoveFilesThatAreNotExtraStudentFiles() throws IOException {
        Path path = targetDir.resolve(".tmcproject.yml");
        Files.createFile(path);
        FileUtils.write(path.toFile(), "extra_student_files: nope");

        Path newFile = sourceDir.resolve("temp");
        Files.createFile(newFile);
        FileUtils.write(newFile.toFile(), "Temporary");

        assertFalse(fileMovingPolicy.shouldMove(newFile, sourceDir, targetDir));

        processor.moveFiles(sourceDir, targetDir);

        Path targetFile = targetDir.resolve(sourceDir.relativize(newFile));

        assertFalse(Files.exists(targetFile));

        path.toFile().delete();
        newFile.toFile().delete();
        targetFile.toFile().delete();
    }
}
