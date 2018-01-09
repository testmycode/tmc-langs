package fi.helsinki.cs.tmc.langs.io.zip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import fi.helsinki.cs.tmc.langs.io.EverythingIsStudentFileStudentFilePolicy;
import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    public void zipperThrowsExceptionWhenZippingNonExistentFile() throws IOException {
        zipper.zip(TEST_ASSETS_DIR.resolve("noSuchDir"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void zipperThrowsExceptionWhenZippingRoot() throws IOException {
        // platform-specific root
        zipper.zip(Paths.get("/").toAbsolutePath());
    }

    @Test(expected = IllegalStateException.class)
    public void zipperThrowsExceptionWhenZippingWithoutSettingPolicy() throws IOException {
        Path existingPath = TestUtils.getPath(StudentFileAwareUnzipperTest.class,
                "tmcnosubmit_test_case");
        new StudentFileAwareZipper().zip(existingPath);
    }

    @Test
    public void zipperCorrectlyZipsSingleFile() throws IOException {

        byte[] bytes = zipper.zip(TEST_FILE);
        Path actualPath = Files.createTempFile("testZip", ".zip");
        Files.write(actualPath, bytes);

        ZipFile expected = new ZipFile(TEST_FILE_ZIP.toFile());
        ZipFile actual = new ZipFile(actualPath.toFile());

        assertZipsEqualDecompressed(expected, actual);

        expected.close();
        actual.close();
    }

    @Test
    public void zipperCorrectlyZipsFolderWithFilesAndSubFolders() throws IOException {
        // Create empty dir that is not in git
        Path emptyDir = TEST_DIR.resolve("dir");
        if (Files.notExists(emptyDir)) {
            Files.createDirectory(emptyDir);
        }

        byte[] bytes = zipper.zip(TEST_DIR);
        Path actualPath = Files.createTempFile("testZip", ".zip");
        Files.write(actualPath, bytes);

        ZipFile expected = new ZipFile(TEST_DIR_ZIP.toFile());
        ZipFile actual = new ZipFile(actualPath.toFile());

        assertZipsEqualDecompressed(expected, actual);

        expected.close();
        actual.close();
    }

    @Test
    public void zipperDetectectsAndObeysTmcnosubmitFiles() throws IOException {
        Path uncompressed = TestUtils.getPath(StudentFileAwareUnzipperTest.class,
                "tmcnosubmit_test_case");
        byte[] zip = zipper.zip(uncompressed);

        Path compressed = Files.createTempFile("testZip", ".zip");
        Files.write(compressed, zip);

        Path reference = TEST_ASSETS_DIR.resolve("tmcnosubmit_test_case.zip");

        ZipFile expected = new ZipFile(reference.toFile());
        ZipFile actual = new ZipFile(compressed.toFile());

        assertZipsEqualDecompressed(expected, actual);

        expected.close();
        actual.close();
        Files.deleteIfExists(compressed);
    }

    @Test
    public void zipperFollowsStudentPolicy() throws IOException {
        Path uncompressed = TestUtils.getPath(StudentFileAwareUnzipperTest.class,
                "zip_studentpolicy_test_case");

        // Policy: zip every directory and file whose name starts with "include"
        zipper.setStudentFilePolicy(new StudentFilePolicy() {
            @Override
            public boolean isStudentFile(Path path, Path projectRootPath) {
                if (path.equals(projectRootPath)) {
                    return true;
                }
                return path.getFileName().toString().startsWith("include");
            }

            @Override
            public boolean mayDelete(Path file, Path projectRoot) {
                return true;
            }

            @Override
            public boolean isUpdatingForced(Path path, Path projectRootPath) {
                return false;
            }
        });

        byte[] zip = zipper.zip(uncompressed);
        Path compressed = Files.createTempFile("testZip", ".zip");
        Files.write(compressed, zip);

        Path referenceZip = TEST_ASSETS_DIR.resolve("zip_studentpolicy_test_case.zip");

        ZipFile expected = new ZipFile(referenceZip.toFile());
        ZipFile actual = new ZipFile(compressed.toFile());

        assertZipsEqualDecompressed(expected, actual);

        expected.close();
        actual.close();
        Files.deleteIfExists(compressed);
    }

    @Test
    public void extraStudentFilesGetZipped() throws IOException {
        Path projectFolder = TestUtils.getPath(StudentFileAwareUnzipperTest.class,
                "with_extra_student_files");

        zipper.setStudentFilePolicy(new StudentFilePolicy() {
            @Override
            public boolean isStudentFile(Path path, Path projectRootPath) {
                if (path.equals(projectRootPath)) {
                    return true;
                }
                return path.getFileName().toString().endsWith("TehtavienhallintaTest.java");
            }

            @Override
            public boolean mayDelete(Path file, Path projectRoot) {
                return true;
            }

            @Override
            public boolean isUpdatingForced(Path path, Path projectRootPath) {
                return false;
            }
        });

        byte[] zip = zipper.zip(projectFolder);
        Path compressed = Files.createTempFile("testZip", ".zip");
        Files.write(compressed, zip);

        System.out.println("debug");

        Path referenceZip = TEST_ASSETS_DIR.resolve("with_extra_student_files_target.zip");

        ZipFile expected = new ZipFile(referenceZip.toFile());
        ZipFile actual = new ZipFile(compressed.toFile());

        assertZipsEqualDecompressed(expected, actual);

        expected.close();
        actual.close();
        Files.deleteIfExists(compressed);
    }

    private void assertZipsEqualDecompressed(ZipFile expected, ZipFile actual)
            throws IOException {
        Map<String, ZipArchiveEntry> expectedEntries = new HashMap<>();
        Enumeration<ZipArchiveEntry> entries = expected.getEntries();
        while (entries.hasMoreElements()) {
            ZipArchiveEntry entry = entries.nextElement();
            expectedEntries.put(entry.getName(), entry);
        }

        Map<String, ZipArchiveEntry> actualEntries = new HashMap<>();
        entries = actual.getEntries();
        while (entries.hasMoreElements()) {
            ZipArchiveEntry entry = entries.nextElement();
            actualEntries.put(entry.getName(), entry);
        }

        assertEquals("Expected actual and expected zips to have same number of entries",
                expectedEntries.size(),
                actualEntries.size());

        for (Map.Entry<String, ZipArchiveEntry> mapEntry : expectedEntries.entrySet()) {
            String expectedName = mapEntry.getKey();
            assertTrue("Expected actual zip to contain entry " + expectedName,
                    actualEntries.containsKey(expectedName));
        }
    }
}
