package fi.helsinki.cs.tmc.langs.io.zip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.FileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

public class StudentFileAwareUnzipperTest {

    private static final Path TEST_DIR_ZIP =
            TestUtils.getPath(StudentFileAwareUnzipperTest.class, "zipTestResources")
                    .resolve("testDirectory.zip");
    private static final Path TEST_ZIP =
            TestUtils.getPath(StudentFileAwareUnzipperTest.class, "zipTestResources")
                    .resolve("testZip.zip");

    private Path tmpDir;
    private Unzipper unzipper;

    @Before
    public void setUp() throws IOException {
        tmpDir = Files.createTempDirectory("tmc-tmp");
        unzipper = new StudentFileAwareUnzipper(getNothingIsStudentFilePolicy());
    }

    @After
    public void tearDown() throws IOException {
        Files.walkFileTree(tmpDir, new FileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path path,
                                                     BasicFileAttributes basicFileAttributes)
                    throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes)
                    throws IOException {
                Files.delete(path);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path path, IOException ex) throws IOException {
                fail("Unable to clean temporary files!");
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path path, IOException ex)
                    throws IOException {
                Files.delete(path);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Test(expected = FileNotFoundException.class)
    public void attemptingToUnzipNonExistentZipThrowsException() throws IOException {
        unzipper.unzip(Paths.get("NoSuch"), Paths.get(""));
    }

    @Test
    public void attemptingToUnzipToNonExistentTargetCreatesNecessaryDirectories()
            throws IOException {
        Path tmpDir = Files.createTempDirectory("tmc-tmp");
        Path subDir = tmpDir.resolve("subDir");
        Path subSubDir = subDir.resolve("subSubDir");
        unzipper.unzip(TEST_ZIP, subSubDir);

        assertTrue(Files.exists(subSubDir));
    }

    @Test
    public void unzippingCorrectlyUnzipsZipWithSingleFile() throws IOException {
        unzipper.unzip(TEST_ZIP, tmpDir);

        assertTrue(Files.exists(tmpDir.resolve("testFile.txt")));
    }

    @Test
    public void unzippingCorrectlyUnzipsZipWithSubDirsAndFiles() throws IOException {
        unzipper.unzip(TEST_DIR_ZIP, tmpDir);

        assertTrue(Files.exists(tmpDir.resolve("testDirectory")));
        assertTrue(Files.exists(tmpDir.resolve("testDirectory").resolve("testFile.txt")));
        assertTrue(Files.exists(tmpDir.resolve("testDirectory").resolve("dir")));
    }

    @Test
    public void unzipperUnzipsNonExistentStudentFiles() throws IOException {
        unzipper = new StudentFileAwareUnzipper(getEverythingIsStudentFilePolicy());

        unzipper.unzip(TEST_DIR_ZIP, tmpDir);

        assertTrue(Files.exists(tmpDir.resolve("testDirectory")));
        assertTrue(Files.exists(tmpDir.resolve("testDirectory").resolve("testFile.txt")));
        assertTrue(Files.exists(tmpDir.resolve("testDirectory").resolve("dir")));
    }

    @Test
    public void unzipperOverwritesFilesThatAreNotStudentFiles() throws IOException {
        Path testFile = tmpDir.resolve("testDirectory").resolve("testFile.txt");
        Files.createDirectories(testFile.getParent());
        Files.createFile(testFile);
        long originalSize = Files.size(testFile);

        unzipper.unzip(TEST_DIR_ZIP, tmpDir);

        assertTrue(originalSize != Files.size(testFile));
    }

    @Test
    public void unzipperDoesNotOverwriteStudentFiles() throws IOException {
        unzipper = new StudentFileAwareUnzipper(getEverythingIsStudentFilePolicy());
        Path testFile = tmpDir.resolve("testDirectory").resolve("testFile.txt");
        Files.createDirectories(testFile.getParent());
        Files.createFile(testFile);
        long originalSize = Files.size(testFile);

        unzipper.unzip(TEST_DIR_ZIP, tmpDir);

        assertEquals(originalSize, Files.size(testFile));
    }

    @Test
    public void canChangePolicy() throws IOException {
        Path testFile = tmpDir.resolve("testDirectory").resolve("testFile.txt");
        Files.createDirectories(testFile.getParent());
        Files.createFile(testFile);
        long originalSize = Files.size(testFile);

        unzipper = new StudentFileAwareUnzipper(getEverythingIsStudentFilePolicy());
        unzipper.setStudentFilePolicy(getNothingIsStudentFilePolicy());
        unzipper.unzip(TEST_DIR_ZIP, tmpDir);

        assertTrue(originalSize != Files.size(testFile));
    }

    private StudentFilePolicy getNothingIsStudentFilePolicy() {
        return new StudentFilePolicy() {
            @Override
            public boolean isStudentFile(Path path, Path projectRootPath) {
                return false;
            }
        };
    }

    private StudentFilePolicy getEverythingIsStudentFilePolicy() {
        return new StudentFilePolicy() {
            @Override
            public boolean isStudentFile(Path path, Path projectRootPath) {
                return true;
            }
        };
    }
}
