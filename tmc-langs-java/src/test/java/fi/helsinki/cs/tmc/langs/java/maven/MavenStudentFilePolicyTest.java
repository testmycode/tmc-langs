package fi.helsinki.cs.tmc.langs.java.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
}
