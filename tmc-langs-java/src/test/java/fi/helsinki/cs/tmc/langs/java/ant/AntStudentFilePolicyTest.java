package fi.helsinki.cs.tmc.langs.java.ant;

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

public class AntStudentFilePolicyTest {

    private AntStudentFilePolicy antFileMovingPolicy;
    private Path projectPath;

    @Before
    public void setUp() {
        projectPath = TestUtils.getPath(getClass(), "ant_arith_funcs");
        Path tmcprojectYml = projectPath.resolve(".tmcproject.yml");
        antFileMovingPolicy = new AntStudentFilePolicy(tmcprojectYml);
    }

    @Test
    public void testItDoesNotMoveBuildXml() throws IOException {
        Path build = Paths.get("build.xml");
        assertFalse(antFileMovingPolicy.isStudentSourceFile(build, projectPath));
    }

    @Test
    public void testItMovesFilesInSrc() throws IOException {
        final List<String> toBeMoved = new ArrayList<>();

        TestUtils.collectPaths(projectPath, toBeMoved, antFileMovingPolicy);

        assertEquals(1, toBeMoved.size());
        assertTrue(toBeMoved.contains("src" + File.separatorChar + "Arith.java"));
    }

    @Test
    public void testItDoesNotMoveFilesInTest() throws IOException {
        final List<String> toBeMoved = new ArrayList<>();

        TestUtils.collectPaths(projectPath, toBeMoved, antFileMovingPolicy);

        assertEquals(1, toBeMoved.size());
        assertFalse(toBeMoved.contains("test" + File.separatorChar + "ArithTest.java"));
    }

    @Test
    public void testItDoesNotMoveFilesInLib() throws IOException {
        final List<String> toBeMoved = new ArrayList<>();

        TestUtils.collectPaths(projectPath, toBeMoved, antFileMovingPolicy);

        assertEquals(1, toBeMoved.size());
        assertFalse(toBeMoved.contains("lib" + File.separatorChar + "edu-test-utils-0.4.1.jar"));
        assertFalse(toBeMoved.contains("lib" + File.separatorChar + "junit-4.10.jar"));
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
