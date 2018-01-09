package fi.helsinki.cs.tmc.langs.io.zip;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.io.ConfigurableStudentFilePolicy;
import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class StudentFileAwareUnzipperTest {

    private Path tmpDir;
    private Unzipper unzipper;
    public static final Path MODULE_TRIVIAL =
            TestUtils.getPath(StudentFileAwareUnzipperTest.class, "zip")
                    .resolve("module-trivial.zip");
    public static final Path COURSE_MODULE_TRIVIAL =
            TestUtils.getPath(StudentFileAwareUnzipperTest.class, "zip")
                    .resolve("course-module-trivial.zip");

    @Before
    public void setUp() throws IOException {
        tmpDir = Files.createTempDirectory("tmc-tmp");
        unzipper = new StudentFileAwareUnzipper(getNothingIsStudentFilePolicy());
    }

    @After
    public void teardown() throws Exception {
        FileUtils.deleteDirectory(tmpDir.toFile());
    }

    @Test(expected = FileNotFoundException.class)
    public void attemptingToUnzipNonExistentZipThrowsException() throws IOException {
        unzipper.unzip(Paths.get("NoSuch"), Paths.get(""));
    }

    @Test
    public void detectsProjectrDirectoryProperlyWhenItsTheOnlySubfolder() throws Exception {
        Path moduleTrivial =
                TestUtils.getPath(StudentFileAwareUnzipperTest.class, "zip")
                        .resolve("module-trivial");
        unzipper.unzip(MODULE_TRIVIAL, tmpDir);

        Set<Path> expected = listFiles(moduleTrivial);

        Set<Path> found = listFiles(tmpDir);

        assertThat(found).containsExactlyElementsIn(expected);
    }

    private Set<Path> listFiles(Path path) {
        return relativize(new HashSet<File>(FileUtils.listFiles(path.toFile(), null, true)), path);
    }

    private Set<Path> relativize(Set<File> files, Path from) {
        Set<Path> results = new HashSet<>(files.size());
        for (File file : files) {

            results.add(from.relativize(file.toPath()));
        }
        return results;
    }

    @Test
    public void detectsProjectrDirectoryProperlyWhenItsDeeperInSubfolder() throws Exception {
        Path moduleTrivial =
                TestUtils.getPath(StudentFileAwareUnzipperTest.class, "zip")
                        .resolve("module-trivial");
        unzipper.unzip(COURSE_MODULE_TRIVIAL, tmpDir);

        Set<Path> expected = listFiles(moduleTrivial);

        Set<Path> found = listFiles(tmpDir);

        assertThat(found).containsExactlyElementsIn(expected);
    }

    @Test
    public void unzipperOverwritesFilesThatAreNotStudentFiles() throws Exception {
        Path srcFile = tmpDir.resolve(Paths.get("src", "Trivial.java"));
        Files.createDirectories(srcFile.getParent());
        Files.createFile(srcFile);
        long originalSize = Files.size(srcFile);

        unzipper.unzip(COURSE_MODULE_TRIVIAL, tmpDir);

        assertTrue(originalSize != Files.size(srcFile));
    }

    @Test
    public void unzipperDoesNotOverwriteStudentFiles() throws IOException {
        unzipper = new StudentFileAwareUnzipper(getEverythingIsStudentFilePolicy());
        Path srcFile = tmpDir.resolve(Paths.get("src", "Trivial.java"));
        Files.createDirectories(srcFile.getParent());
        Files.createFile(srcFile);
        long originalSize = Files.size(srcFile);

        unzipper.unzip(COURSE_MODULE_TRIVIAL, tmpDir);

        assertEquals(originalSize, Files.size(srcFile));
    }

    @Test
    public void canChangePolicy() throws Exception {
        Path srcFile = tmpDir.resolve(Paths.get("src", "Trivial.java"));
        Files.createDirectories(srcFile.getParent());
        Files.createFile(srcFile);

        unzipper = new StudentFileAwareUnzipper(getEverythingIsStudentFilePolicy());
        unzipper.setStudentFilePolicy(getNothingIsStudentFilePolicy());

        long originalSize = Files.size(srcFile);
        unzipper.unzip(COURSE_MODULE_TRIVIAL, tmpDir);

        assertTrue(originalSize != Files.size(srcFile));
    }

    @Test
    public void testDeletion() throws Exception {
        Path bad = TestUtils.getPath(StudentFileAwareUnzipperTest.class, "zip")
                .resolve("bad-test-class-name.zip");
        Path fixed = TestUtils.getPath(StudentFileAwareUnzipperTest.class, "zip")
                .resolve("fixed-test-class-name.zip");

        unzipper.unzip(bad, tmpDir);

        unzipper.unzip(fixed, tmpDir);

        Path tmpDirCorrect = Files.createTempDirectory("tmc-tmp-correct");
        unzipper.unzip(fixed, tmpDirCorrect);

        Set<Path> found = listFiles(tmpDir);

        assertThat(found).containsExactlyElementsIn(listFiles(tmpDirCorrect));
        FileUtils.deleteDirectory(tmpDirCorrect.toFile());
    }

    @Test
    public void testForceUpdate() throws IOException {
        StudentFilePolicy policy = new ConfigurableStudentFilePolicy(tmpDir) {
            @Override
            public boolean isStudentSourceFile(Path path, Path projectRootPath) {
                return true;
            }
        };
        unzipper = new StudentFileAwareUnzipper(policy);

        Path faulty = TestUtils.getPath(StudentFileAwareUnzipperTest.class, "zipTestResources")
                .resolve("faulty.zip");
        Path correction = TestUtils.getPath(StudentFileAwareUnzipperTest.class, "zipTestResources")
                .resolve("correction.zip");

        unzipper.unzip(faulty, tmpDir);

        unzipper.unzip(correction, tmpDir);

        Path testFile = tmpDir.resolve(Paths.get("src", "test_file"));
        String contents = new String(Files.readAllBytes(testFile));
        assertEquals("correct", contents.trim());
    }

    private StudentFilePolicy getNothingIsStudentFilePolicy() {
        return new StudentFilePolicy() {
            @Override
            public boolean isStudentFile(Path path, Path projectRootPath) {
                return false;
            }

            @Override
            public boolean mayDelete(Path file, Path projectRoot) {
                return true;
            }

            @Override
            public boolean isUpdatingForced(Path path, Path projectRootPath) {
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

            @Override
            public boolean mayDelete(Path file, Path projectRoot) {
                return false;
            }

            @Override
            public boolean isUpdatingForced(Path path, Path projectRootPath) {
                return false;
            }
        };
    }
}
