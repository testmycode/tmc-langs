package fi.helsinki.cs.tmc.langs.java.testscanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.TestDesc;
import fi.helsinki.cs.tmc.langs.java.ClassPath;
import fi.helsinki.cs.tmc.langs.utils.SourceFiles;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class TestScannerTest {

    private TestScanner scanner;
    private Optional<ExerciseDesc> opDesc;

    @Before
    public void setup() {
        scanner = new TestScanner();
        opDesc = createExerciseDesc();
    }

    @Test
    public void testTestScannerReturnsAbsentWithoutSourceFiles() {
        Path path = TestUtils.getPath(getClass(), "ant_arith_funcs").toAbsolutePath();
        SourceFiles sourceFiles = new SourceFiles();

        Path testClassesPath = Paths.get(path + "/build/test/classes");
        Path normalClassesPath = Paths.get(path + "/build/classes");
        ClassPath classPath = new ClassPath(testClassesPath, normalClassesPath);
        classPath.addDirAndContents(Paths.get(path + "/lib"));
        Optional<ExerciseDesc> absDesc = scanner.findTests(classPath, sourceFiles, "exercise");
        assertEquals(absDesc, Optional.absent());
    }

    @Test
    public void testTestScannerReturnsExerciseDesc() {
        assertTrue("No ExerciseDesc found!", opDesc.isPresent());
        ExerciseDesc desc = opDesc.get();
        assertTrue("ExerciseDesc has wrong exerciseName!", desc.name.equals("exercise"));
    }

    @Test
    public void testTestScannerFindsAllTests() {
        HashMap<String, ImmutableList<String>> testMethods = new HashMap<>();
        for (TestDesc test : opDesc.get().tests) {
            testMethods.put(test.name, test.points);
            assertEquals("[arith-funcs]", test.points.toString());
        }
        assertNotNull("Test method testAdd() not found.", testMethods.get("ArithTest testAdd"));
        assertNotNull("Test method testSub() not found.", testMethods.get("ArithTest testSub"));
        assertNotNull("Test method testMul() not found.", testMethods.get("ArithTest testMul"));
        assertNotNull("Test method testDiv() not found.", testMethods.get("ArithTest testDiv"));
    }

    private Optional<ExerciseDesc> createExerciseDesc() {
        Path path = TestUtils.getPath(getClass(), "ant_arith_funcs").toAbsolutePath();
        SourceFiles sourceFiles = new SourceFiles();
        sourceFiles.addSource(Paths.get(path + "/test").toFile());

        Path testClassesPath = Paths.get(path + "/build/test/classes");
        Path normalClassesPath = Paths.get(path + "/build/classes");
        ClassPath classPath = new ClassPath(testClassesPath, normalClassesPath);
        classPath.addDirAndContents(Paths.get(path + "/lib"));
        return scanner.findTests(classPath, sourceFiles, "exercise");
    }
}
