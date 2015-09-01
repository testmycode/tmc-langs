package fi.helsinki.cs.tmc.langs.python3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Python3StudentFilePolicyTest {

    private Path projectPath;
    private Python3StudentFilePolicy python3StudentFilePolicy;

    @Before
    public void setUp() {
        projectPath = TestUtils.getPath(getClass(), "passing");
        python3StudentFilePolicy = new Python3StudentFilePolicy(projectPath);
    }

    @Test
    public void testFilesInSrcDirectoryAreStudentFiles() throws IOException {
        final List<String> studentFiles = new ArrayList<>();

        TestUtils.collectPaths(projectPath, studentFiles, python3StudentFilePolicy);

        assertEquals(1, studentFiles.size());
        assertTrue(studentFiles.contains("src" + File.separator + "newpythonproject.py"));
    }

    @Test
    public void testFilesInTestDirectoryAreNotStudentFiles() throws IOException {
        final List<String> studentFiles = new ArrayList<>();

        TestUtils.collectPaths(projectPath, studentFiles, python3StudentFilePolicy);

        assertEquals(1, studentFiles.size());
        assertFalse(studentFiles.contains("test" + File.separatorChar + "test_two.py"));
    }

    @Test
    public void testFilesInTestSubDirectoryAreNotStudentFiles() throws IOException {
        final List<String> studentFiles = new ArrayList<>();

        TestUtils.collectPaths(projectPath, studentFiles, python3StudentFilePolicy);

        assertEquals(1, studentFiles.size());
        assertFalse(
                studentFiles.contains(
                        "test" + File.separatorChar + "tmc" + File.separator + "runner.py"));
    }
}
