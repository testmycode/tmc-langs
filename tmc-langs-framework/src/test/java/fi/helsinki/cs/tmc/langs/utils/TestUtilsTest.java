package fi.helsinki.cs.tmc.langs.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.sandbox.ExtraStudentFileAwareFileMovingPolicy;

import org.apache.commons.io.FileUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TestUtilsTest {

    private Path dir;
    private Path subDir;
    private Path subSubDir;
    private Path dirContent;
    private Path subDirContent;
    private Path subSubDirContent;

    @Before
    public void setUp() throws IOException {
        dir = Files.createTempDirectory("tmc-test-testutils");
        subDir = Files.createTempDirectory(dir, "subDir");
        subSubDir = Files.createTempDirectory(subDir, "subSubDir");
        dirContent = Files.createTempFile(dir, "testFile", ".tmp");
        subDirContent = Files.createTempFile(subDir, "subTestFile", ".tmp");
        subSubDirContent = Files.createTempFile(subSubDir, "subSubTestFile", ".tmp");
    }

    /**
     * Delete files after tests are run.
     */
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

    @Test
    public void collectPathsCanCollectPaths() throws IOException {
        List<String> toBeMoved = new ArrayList<>();
        ExtraStudentFileAwareFileMovingPolicy fileMovingPolicy =
                new ExtraStudentFileAwareFileMovingPolicy() {
            @Override
            public boolean shouldMoveFile(Path path) {
                return true;
            }
        };

        TestUtils.collectPaths(dir, toBeMoved, fileMovingPolicy);

        assertEquals(3, toBeMoved.size());
        assertTrue(toBeMoved.get(0).endsWith(".tmp"));
        assertTrue(toBeMoved.get(1).endsWith(".tmp"));
        assertTrue(toBeMoved.get(2).endsWith(".tmp"));
    }

    @Test
    public void collectPathsDoesNotCollectWrongPaths() throws IOException {
        List<String> toBeMoved = new ArrayList<>();
        ExtraStudentFileAwareFileMovingPolicy fileMovingPolicy =
                new ExtraStudentFileAwareFileMovingPolicy() {
            @Override
            public boolean shouldMoveFile(Path path) {
                return false;
            }
        };

        TestUtils.collectPaths(dir, toBeMoved, fileMovingPolicy);

        assertEquals(0, toBeMoved.size());
    }

    @Test
    public void initTempFileWithContent() throws IOException {
        File file = TestUtils.initTempFileWithContent("temp", "tmp", "Temporary");

        assertTrue(file.exists());
        assertTrue(file.toString().startsWith("/tmp/temp"));
        assertTrue(file.toString().endsWith(".tmp"));
        assertEquals("Temporary\n", FileUtils.readFileToString(file));

        file.delete();
    }

    @Test
    public void initTempFileWithContentInDirectory() throws IOException {
        Path dirPath = Files.createTempDirectory("tempdir");
        File dir = dirPath.toFile();
        File file = TestUtils.initTempFileWithContent("temp", "tmp", dir, "Temporary");

        assertTrue(file.exists());
        assertTrue(file.toString().startsWith("/tmp/temp"));
        assertTrue(file.toString().endsWith(".tmp"));
        assertEquals(file, dir.listFiles()[0]);
        assertEquals("Temporary\n", FileUtils.readFileToString(file));

        file.delete();
        dir.delete();
    }
}
