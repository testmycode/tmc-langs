package fi.helsinki.cs.tmc.langs.io.zip;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


import fi.helsinki.cs.tmc.langs.io.EverythingIsStudentFileStudentFilePolicy;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.FileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class StudentFileAwareZipperTest {

    private static final Path TEST_ASSETS_DIR
            = TestUtils.getPath(StudentFileAwareUnzipperTest.class, "zipTestResources");
    private static final Path TEST_DIR_ZIP = TEST_ASSETS_DIR.resolve("testDirectory.zip");
    private static final Path TEST_DIR = TEST_ASSETS_DIR.resolve("testDirectory");
    private static final Path TEST_FILE_ZIP = TEST_ASSETS_DIR.resolve("testZip.zip");
    private static final Path TEST_FILE = TEST_ASSETS_DIR.resolve("testFile.txt");

    private Path tmpDir;
    private Zipper zipper;

    @Before
    public void setUp() throws IOException {
        tmpDir = Files.createTempDirectory("tmc-tmp");
        zipper = new StudentFileAwareZipper(new EverythingIsStudentFileStudentFilePolicy());
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
    public void zipperThrowsExceptionWhenUnzippingNonExistentFile() throws IOException {
        zipper.zip(TEST_ASSETS_DIR.resolve("noSuchDir"));
    }

    @Test
    public void zipperCorrectlyZipsSingleFile() throws IOException {

        byte[] bytes = zipper.zip(TEST_FILE);
        Path actualPath = Files.createTempFile("testZip", ".zip");
        Files.write(actualPath, bytes);

        ZipFile expected = new ZipFile(TEST_FILE_ZIP.toFile());
        ZipFile actual = new ZipFile(actualPath.toFile());

        assertZipsEqualDecompressed(expected, actual);
    }

    @Test
    public void zipperCorrectlyZipsFolderWithFilesAndSubFolders() throws IOException {
        // Create empty dir that is not in git
        Path emptyDir = (TEST_DIR.resolve("dir"));
        if (Files.notExists(emptyDir)) {
            Files.createDirectory(emptyDir);
        }

        byte[] bytes = zipper.zip(TEST_DIR);
        Path actualPath = Files.createTempFile("testZip", ".zip");
        Files.write(actualPath, bytes);

        ZipFile expected = new ZipFile(TEST_DIR_ZIP.toFile());
        ZipFile actual = new ZipFile(actualPath.toFile());

        assertZipsEqualDecompressed(expected, actual);
    }

    private void assertZipsEqualDecompressed(ZipFile expected, ZipFile actual) throws IOException {

        Map<String, ZipArchiveEntry> expectedEntries = new HashMap<>();
        Enumeration<ZipArchiveEntry> entries = expected.getEntries();
        while (entries.hasMoreElements()) {
            ZipArchiveEntry entry = entries.nextElement();
            System.out.println(entry.getName());
            expectedEntries.put(entry.getName(), entry);
        }

        Map<String, ZipArchiveEntry> actualEntries = new HashMap<>();
        entries = expected.getEntries();
        while (entries.hasMoreElements()) {
            ZipArchiveEntry entry = entries.nextElement();
            actualEntries.put(entry.getName(), entry);
        }

        for (ZipArchiveEntry excepted : expectedEntries.values()) {
            assertTrue(actualEntries.containsValue(excepted));
        }
    }
}
