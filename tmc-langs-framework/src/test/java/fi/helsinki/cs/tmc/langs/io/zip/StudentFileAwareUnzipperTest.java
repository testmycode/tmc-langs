package fi.helsinki.cs.tmc.langs.io.zip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import com.google.common.truth.Truth;
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
        Path moduleTrivial = TestUtils.getPath(StudentFileAwareUnzipperTest.class, "zip").resolve("module-trivial");
        Path zip = TestUtils.getPath(StudentFileAwareUnzipperTest.class, "zip").resolve("module-trivial.zip");
        unzipper.unzip(zip, tmpDir);

        Set<Path> expected = listFiles(moduleTrivial);

        Set<Path> found = listFiles(tmpDir);

        Truth.assertThat(found).containsExactlyElementsIn(expected);
    }

    private Set<Path> listFiles(Path path) {
        return relativize(new HashSet<File>(FileUtils.listFiles(path.toFile(), null, true)), path);
    }

    private Set<Path> relativize(Set<File> files, Path from) {
        Set<Path> results = new HashSet<>(files.size());
        for (File file : files){

            results.add(from.relativize(file.toPath()));
        }
        return results;
    }

    @Test
    public void detectsProjectrDirectoryProperlyWhenItsDeeperInSubfolder() throws Exception {
        Path moduleTrivial = TestUtils.getPath(StudentFileAwareUnzipperTest.class, "zip").resolve("module-trivial");
        Path zip = TestUtils.getPath(StudentFileAwareUnzipperTest.class, "zip").resolve("course-module-trivial.zip");
        unzipper.unzip(zip, tmpDir);

        Set<Path> expected = listFiles(moduleTrivial);

        Set<Path> found = listFiles(tmpDir);

        Truth.assertThat(found).containsExactlyElementsIn(expected);
    }

    @Test
    public void unzipperOverwritesFilesThatAreNotStudentFiles() throws Exception {
        Path zip = TestUtils.getPath(StudentFileAwareUnzipperTest.class, "zip").resolve("course-module-trivial.zip");
        Path srcFile = tmpDir.resolve(Paths.get("src","Trivial.java"));
        Files.createDirectories(srcFile.getParent());
        Files.createFile(srcFile);
        long originalSize = Files.size(srcFile);

        unzipper.unzip(zip, tmpDir);

        assertTrue(originalSize != Files.size(srcFile));
    }

    @Test
    public void unzipperDoesNotOverwriteStudentFiles() throws IOException {
        unzipper = new StudentFileAwareUnzipper(getEverythingIsStudentFilePolicy());
        Path zip = TestUtils.getPath(StudentFileAwareUnzipperTest.class, "zip").resolve("course-module-trivial.zip");
        Path srcFile = tmpDir.resolve(Paths.get("src","Trivial.java"));
        Files.createDirectories(srcFile.getParent());
        Files.createFile(srcFile);
        long originalSize = Files.size(srcFile);

        unzipper.unzip(zip, tmpDir);

        assertEquals(originalSize, Files.size(srcFile));
    }

    @Test
    public void canChangePolicy() throws Exception {
        Path zip = TestUtils.getPath(StudentFileAwareUnzipperTest.class, "zip").resolve("course-module-trivial.zip");
        Path srcFile = tmpDir.resolve(Paths.get("src","Trivial.java"));
        Files.createDirectories(srcFile.getParent());
        Files.createFile(srcFile);

        unzipper = new StudentFileAwareUnzipper(getEverythingIsStudentFilePolicy());
        unzipper.setStudentFilePolicy(getNothingIsStudentFilePolicy());

        long originalSize = Files.size(srcFile);
        unzipper.unzip(zip, tmpDir);

        assertTrue(originalSize != Files.size(srcFile));
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
