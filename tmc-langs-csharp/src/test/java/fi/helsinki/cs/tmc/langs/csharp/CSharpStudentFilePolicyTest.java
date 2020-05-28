package fi.helsinki.cs.tmc.langs.csharp;

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

public class CSharpStudentFilePolicyTest {

    private Path projectPath;
    private CSharpStudentFilePolicy policy;

    @Before
    public void setUp() {
        this.projectPath = TestUtils.getPath(getClass(), "PolicyProject");
        this.policy = new CSharpStudentFilePolicy(projectPath);
    }

    @Test
    public void testFilesInSrcDirectoryAreStudentFiles() throws IOException {
        final List<String> studentFiles = new ArrayList<>();
        TestUtils.collectPaths(projectPath, studentFiles, policy);

        assertEquals(2, studentFiles.size());
        assertTrue(studentFiles.contains("src" + File.separator + "PolicySample"
                + File.separator + "Program.cs"));
        assertTrue(studentFiles.contains("src" + File.separator + "PolicySample"
                + File.separator + "PolicySample.csproj"));
    }

    @Test
    public void testFilesInTestDirectoryAreNotStudentFiles() throws IOException {
        final List<String> studentFiles = new ArrayList<>();
        TestUtils.collectPaths(projectPath, studentFiles, policy);

        assertEquals(2, studentFiles.size());
        assertFalse(studentFiles.contains("test" + File.separator + "PolicySampleTests"
                + File.separator + "PrgramTest.cs"));
        assertFalse(studentFiles.contains("test" + File.separator + "PolicySampleTests"
                + File.separator + "PolicySampleTests.csproj"));

    }
}
