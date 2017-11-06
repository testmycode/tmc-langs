package fi.helsinki.cs.tmc.langs.r;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RStudentFilePolicyTest {

    private Path projectPath;
    private RStudentFilePolicy studentFilePolicy;

    @Before
    public void setUp() {
        projectPath = TestUtils.getPath(getClass(), "simple_all_tests_pass");
        studentFilePolicy = new RStudentFilePolicy(projectPath);
    }

    @Test
    public void testFilesInRDirectoryAreStudentFiles() throws IOException {
        List<String> studentFiles = new ArrayList<>();

        TestUtils.collectPaths(projectPath, studentFiles, studentFilePolicy);

        assertEquals(2, studentFiles.size());
        assertTrue(studentFiles.contains("R" + File.separator + "main.R"));
        assertTrue(studentFiles.contains("R" + File.separator + "second.R"));
    }

    @Test
    public void testFilesInTestthatDirectoryAreNotStudentFiles() throws IOException {
        List<String> studentFiles = new ArrayList<>();

        TestUtils.collectPaths(projectPath, studentFiles, studentFilePolicy);

        assertEquals(2, studentFiles.size());
        assertTrue(Files.exists(
                projectPath.resolve("tests").resolve("testthat").resolve("testMain.R")));
        assertTrue(Files.exists(
                projectPath.resolve("tests").resolve("testthat").resolve("testSecond.R")));
        assertFalse(studentFiles.contains(
                "test" + File.separatorChar + "testthat"
                        + File.separatorChar + "testSecond.R"));
        assertFalse(studentFiles.contains(
                "test" + File.separatorChar + "testthat"
                        + File.separatorChar + "testMainR"));
    }

    @Test
    public void testFilesInADirectoryWhoseNameBeginsWithRAreNotStudentFiles() throws IOException {
        List<String> studentFiles = new ArrayList<>();

        TestUtils.collectPaths(projectPath, studentFiles, studentFilePolicy);

        assertEquals(2, studentFiles.size());
        assertTrue(Files.exists(projectPath.resolve("R2").resolve("third.R")));
        assertFalse(studentFiles.contains("R2" + File.separator + "third.R"));
    }
}
