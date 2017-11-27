package fi.helsinki.cs.tmc.langs.qmake;

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

public class QmakeStudentFilePolicyTest {

    private Path singleLib;
    private QmakeStudentFilePolicy qmakeStudentFilePolicy;

    private final String srcDir = "src" + File.separatorChar;
    private final String testDir = "test_case_test_runner" + File.separatorChar;
    private final String libDir = "test_case_lib" + File.separatorChar;

    @Before
    public void setUp() {
        singleLib = TestUtils.getPath(getClass(), "passing_single_lib");
        qmakeStudentFilePolicy = new QmakeStudentFilePolicy(singleLib);
    }

    @Test
    public void testMovesFilesInSrc() throws IOException {
        final List<String> toBeMoved = new ArrayList<>();

        TestUtils.collectPaths(singleLib, toBeMoved, qmakeStudentFilePolicy);
        assertEquals(2, toBeMoved.size());

        assertTrue(toBeMoved.contains(srcDir + "src.pro"));
        assertTrue(toBeMoved.contains(srcDir + "main.cpp"));
    }

    @Test
    public void testDoesNotMoveFilesInTest() throws IOException {
        final List<String> toBeMoved = new ArrayList<>();

        TestUtils.collectPaths(singleLib, toBeMoved, qmakeStudentFilePolicy);

        assertEquals(2, toBeMoved.size());

        assertFalse(toBeMoved.contains(testDir + "test_case_test_runner.pro"));
        assertFalse(toBeMoved.contains(testDir + "test_case_test_runner.cpp"));
        assertFalse(toBeMoved.contains(testDir + "test_case_test_runner.h"));
        assertFalse(toBeMoved.contains(testDir + "main.cpp"));
    }

    @Test
    public void testDoesNotMoveFilesInLibrary() throws IOException {
        final List<String> toBeMoved = new ArrayList<>();

        TestUtils.collectPaths(singleLib, toBeMoved, qmakeStudentFilePolicy);

        assertEquals(2, toBeMoved.size());
        assertFalse(toBeMoved.contains(libDir + "test_case_lib.cpp"));
        assertFalse(toBeMoved.contains(libDir + "test_case_lib.h"));
        assertFalse(toBeMoved.contains(libDir + "test_case_lib.pro"));
    }
}
