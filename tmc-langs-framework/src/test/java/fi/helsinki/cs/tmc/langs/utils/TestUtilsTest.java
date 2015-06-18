package fi.helsinki.cs.tmc.langs.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;

public class TestUtilsTest {

    private Path dir;
    private Path subDir;
    private Path subSubDir;
    private Path dirContent;
    private Path subDirContent;
    private Path subSubDirContent;

    @Before
    public void setUp() throws IOException {
        dir = Files.createTempDirectory("tmc-test-resource");
        subDir = Files.createTempDirectory(dir, "subDir");
        subSubDir = Files.createTempDirectory(subDir, "subSubDir");
        dirContent = Files.createTempFile(dir, "testFile", ".tmp");
        subDirContent = Files.createTempFile(subDir, "subTestFile", ".tmp");
        subSubDirContent = Files.createTempFile(subSubDir, "subSubTestFile", ".tmp");
    }

    @After
    public void tearDown() {
        subSubDirContent.toFile().delete();
        subSubDir.toFile().delete();
        subDirContent.toFile().delete();
        subDir.toFile().delete();
        dirContent.toFile().delete();
        dir.toFile().delete();
    }

    @Test
    public void getPathReturnsNullIfRequestedResourceDoesNotExist() {
        TestUtils.getPath(getClass(), "noSuch");
    }

    @Test
    public void removeDirRecursivelyHandlesNullPath() throws IOException {
        TestUtils.removeDirRecursively(null);
    }

    @Test
    public void removeDirRecursivelyRemovesDirAndSubDirsWhenGivenAPath() throws IOException {
        TestUtils.removeDirRecursively(dir);

        assertFalse(dir.toFile().exists());
        assertFalse(subDir.toFile().exists());
        assertFalse(subSubDir.toFile().exists());
        assertFalse(dirContent.toFile().exists());
        assertFalse(subDirContent.toFile().exists());
        assertFalse(subSubDirContent.toFile().exists());
    }

    @Test
    public void removeDirRecursivelyRemovesDirAndSubDirs() throws IOException {
        TestUtils.removeDirRecursively(dir);

        assertFalse(dir.toFile().exists());
        assertFalse(subDir.toFile().exists());
        assertFalse(subSubDir.toFile().exists());
        assertFalse(dirContent.toFile().exists());
        assertFalse(subDirContent.toFile().exists());
        assertFalse(subSubDirContent.toFile().exists());
    }
}
