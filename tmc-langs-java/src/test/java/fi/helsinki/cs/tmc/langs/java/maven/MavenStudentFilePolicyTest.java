package fi.helsinki.cs.tmc.langs.java.maven;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.io.zip.StudentFileAwareUnzipper;
import fi.helsinki.cs.tmc.langs.io.zip.Unzipper;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MavenStudentFilePolicyTest {

    private Path projectPath;
    private MavenStudentFilePolicy mavenStudentFilePolicy;

    @Before
    public void setUp() {
        projectPath = TestUtils.getPath(getClass(), "maven_exercise");
        mavenStudentFilePolicy = new MavenStudentFilePolicy(projectPath);
    }

    @Test
    public void testItDoesNotMovePomXml() throws IOException {
        Path pom = Paths.get("pom.xml");
        assertFalse(mavenStudentFilePolicy.isStudentSourceFile(pom, projectPath));
    }

    @Test
    public void testItMovesFilesInSrcMain() throws IOException {
        final List<String> toBeMoved = new ArrayList<>();

        TestUtils.collectPaths(projectPath, toBeMoved, mavenStudentFilePolicy);

        assertEquals(1, toBeMoved.size());
        assertTrue(
                toBeMoved.contains(
                        "src"
                                + File.separatorChar
                                + "main"
                                + File.separatorChar
                                + "java"
                                + File.separatorChar
                                + "fi"
                                + File.separatorChar
                                + "helsinki"
                                + File.separatorChar
                                + "cs"
                                + File.separatorChar
                                + "maventest"
                                + File.separatorChar
                                + "App.java"));
    }

    @Test
    public void testItDoesNotMoveFilesInSrcTest() throws IOException {
        final List<String> toBeMoved = new ArrayList<>();

        TestUtils.collectPaths(projectPath, toBeMoved, mavenStudentFilePolicy);

        assertEquals(1, toBeMoved.size());
        assertFalse(
                toBeMoved.contains(
                        "src"
                                + File.separatorChar
                                + "test"
                                + File.separatorChar
                                + "java"
                                + File.separatorChar
                                + "fi"
                                + File.separatorChar
                                + "helsinki"
                                + File.separatorChar
                                + "cs"
                                + File.separatorChar
                                + "maventest"
                                + File.separatorChar
                                + "AppTest.java"));
    }

    @Test
    public void testItDoesNotMoveFilesInLib() throws IOException {
        final List<String> toBeMoved = new ArrayList<>();

        TestUtils.collectPaths(projectPath, toBeMoved, mavenStudentFilePolicy);

        assertEquals(1, toBeMoved.size());
        assertFalse(
                toBeMoved.contains(
                        "lib"
                                + File.separatorChar
                                + "testrunner"
                                + File.separatorChar
                                + "gson-2.2.4.jar"));
        assertFalse(
                toBeMoved.contains(
                        "lib"
                                + File.separatorChar
                                + "testrunner"
                                + File.separatorChar
                                + "hamcrest-core-1.3.jar"));
        assertFalse(
                toBeMoved.contains(
                        "lib"
                                + File.separatorChar
                                + "testrunner"
                                + File.separatorChar
                                + "junit-4.11.jar"));
        assertFalse(
                toBeMoved.contains(
                        "lib"
                                + File.separatorChar
                                + "testrunner"
                                + File.separatorChar
                                + "tmc-junit-runner.jar"));
    }


    @Test
    public void testThatDeletionDoesNotDeleteStudentFiles() throws Exception {
        Path withFiles = TestUtils.getPath(MavenStudentFilePolicy.class, "zip")
                .resolve("with_files.zip");
        Path template = TestUtils.getPath(MavenStudentFilePolicy.class, "zip")
                .resolve("template.zip");
        Unzipper unzipper = new StudentFileAwareUnzipper(mavenStudentFilePolicy);
        Path tmpDir = Files.createTempDirectory("tmc-tmp");
        unzipper.unzip(withFiles, tmpDir);

        unzipper.unzip(template, tmpDir);

        Path tmpDirCorrect = Files.createTempDirectory("tmc-tmp-correct");
        unzipper.unzip(withFiles, tmpDirCorrect);

        Set<Path> found = listFiles(tmpDir);

        assertThat(found).containsExactlyElementsIn(listFiles(tmpDirCorrect));
        FileUtils.deleteDirectory(tmpDir.toFile());
        FileUtils.deleteDirectory(tmpDirCorrect.toFile());
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
}
