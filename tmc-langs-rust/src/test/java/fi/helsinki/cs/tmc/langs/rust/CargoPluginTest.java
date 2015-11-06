package fi.helsinki.cs.tmc.langs.rust;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.abstraction.ValidationError;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.SpecialLogs;
import fi.helsinki.cs.tmc.langs.domain.TestDesc;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import com.google.common.base.Optional;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public class CargoPluginTest {

    private CargoPlugin cargoPlugin;

    @Before
    public void setUp() {
        cargoPlugin = new CargoPlugin();
    }

    @Test
    public void testIsExerciseCorrectTypeDoesntBreakByDirectoryNamedCargoToml() throws IOException {
        Path parent = Files.createTempDirectory("tmc-cargo-test");
        Path cargoToml = parent.resolve("Cargo.toml");
        Files.createDirectory(cargoToml);

        assertFalse(cargoPlugin.isExerciseTypeCorrect(parent));

        Files.delete(cargoToml);
        Files.delete(parent);
    }

    @Test
    public void testProjectWithPassingTestCompilesAndPassesTests() {
        Path path = TestUtils.getPath(getClass(), "passing");
        RunResult result = cargoPlugin.runTests(path);

        assertEquals(RunResult.Status.PASSED, result.status);
        assertEquals(1, result.testResults.size());
        assertTrue(result.testResults.get(0).passed);
    }

    @Test
    public void testProjectWithMultiplePassingTestCompilesAndPassesTests() {
        Path path = TestUtils.getPath(getClass(), "multiPassing");
        RunResult result = cargoPlugin.runTests(path);

        assertEquals(RunResult.Status.PASSED, result.status);
        assertEquals(2, result.testResults.size());
        assertTrue(result.testResults.get(0).passed);
        assertTrue(result.testResults.get(1).passed);
    }

    @Test
    public void testProjectWithFailingTestCompilesAndFailsTest() {
        Path path = TestUtils.getPath(getClass(), "failing");
        RunResult result = cargoPlugin.runTests(path);

        assertEquals(RunResult.Status.TESTS_FAILED, result.status);
        assertEquals(1, result.testResults.size());
        assertFalse(result.testResults.get(0).passed);
    }

    @Test
    public void testProjectPartiallyFailingTestCompilesAndFailsTest() {
        Path path = TestUtils.getPath(getClass(), "semiFailing");
        RunResult result = cargoPlugin.runTests(path);

        assertEquals(RunResult.Status.TESTS_FAILED, result.status);
        assertEquals(2, result.testResults.size());
        boolean first = result.testResults.get(0).passed;
        if (first) {
            assertFalse(result.testResults.get(1).passed);
        } else {
            assertTrue(result.testResults.get(1).passed);
        }
    }

    @Test
    public void testTryingToCheatByAddingTestFails() {
        Path path = TestUtils.getPath(getClass(), "testCheat");
        RunResult result = cargoPlugin.runTests(path);

        assertEquals(RunResult.Status.TESTS_FAILED, result.status);
        assertEquals(1, result.testResults.size());
        assertFalse(result.testResults.get(0).passed);
    }

    @Test
    public void compilationFailurePreserversCompilationOutput() {
        Path path = TestUtils.getPath(getClass(), "compileFail");
        RunResult result = cargoPlugin.runTests(path);

        assertEquals(RunResult.Status.COMPILE_FAILED, result.status);
        assertTrue(result.logs.containsKey(SpecialLogs.COMPILER_OUTPUT));
        assertTrue(new String(result.logs.get(SpecialLogs.COMPILER_OUTPUT))
                .contains("aborting due to previous error"));
        assertEquals(0, result.testResults.size());
    }

    @Test
    public void lintingWorksWithOneError() {
        Path path = TestUtils.getPath(getClass(), "warning");
        ValidationResult result = cargoPlugin.checkCodeStyle(path, new Locale("en"));
        Map<File, List<ValidationError>> errors = result.getValidationErrors();
        assertEquals(1, errors.size());
        assertEquals(Paths.get("src", "lib.rs"), errors.keySet().iterator().next().toPath());
        assertEquals(1, errors.values().iterator().next().size());
    }

    @Test
    public void lintingHasRightErrorWithOneError() {
        Path path = TestUtils.getPath(getClass(), "warning");
        ValidationResult result = cargoPlugin.checkCodeStyle(path, new Locale("en"));
        ValidationError validation = result
                .getValidationErrors().values()
                .iterator().next().get(0);
        assertEquals(7, validation.getLine());
        assertEquals(1, validation.getColumn());
        assertTrue(validation.getMessage().contains("snake case"));
        assertTrue(validation.getSourceName().contains("lib.rs"));
    }

    @Test
    public void lintingWorksWithTwoErrors() {
        Path path = TestUtils.getPath(getClass(), "warnings");
        ValidationResult result = cargoPlugin.checkCodeStyle(path, new Locale("en"));
        Map<File, List<ValidationError>> errors = result.getValidationErrors();
        assertEquals(1, errors.size());
        assertEquals(Paths.get("src", "lib.rs"), errors.keySet().iterator().next().toPath());
        assertEquals(2, errors.values().iterator().next().size());
    }

    @Test
    public void lintingHasRightErrorsWithTwoErrors() {
        Path path = TestUtils.getPath(getClass(), "warnings");
        ValidationResult result = cargoPlugin.checkCodeStyle(path, new Locale("en"));
        List<ValidationError> errors = result.getValidationErrors().values()
                .iterator().next();
        ValidationError validation1 = errors.get(0);
        ValidationError validation2 = errors.get(1);
        if (validation2.getMessage().contains("snake case")) {
            ValidationError tmp = validation1;
            validation1 = validation2;
            validation2 = tmp;
        }
        assertEquals(7, validation1.getLine());
        assertEquals(1, validation1.getColumn());
        assertTrue(validation1.getMessage().contains("snake case"));
        assertTrue(validation1.getSourceName().contains("lib.rs"));

        assertEquals(8, validation2.getLine());
        assertEquals(9, validation2.getColumn());
        assertTrue(validation2.getMessage().contains("unused"));
        assertTrue(validation2.getSourceName().contains("lib.rs"));
    }

    @Test
    public void lintingWorksWithTwoFiles() {
        Path path = TestUtils.getPath(getClass(), "warningFiles");
        ValidationResult result = cargoPlugin.checkCodeStyle(path, new Locale("en"));
        Map<File, List<ValidationError>> errors = result.getValidationErrors();
        assertEquals(2, errors.size());
        Iterator<Entry<File, List<ValidationError>>> errorIt = errors.entrySet().iterator();
        Entry<File, List<ValidationError>> error1 = errorIt.next();
        Entry<File, List<ValidationError>> error2 = errorIt.next();
        if (error2.getKey().getPath().contains("lib.rs")) {
            Entry<File, List<ValidationError>> tmp = error1;
            error1 = error2;
            error2 = tmp;
        }
        assertEquals(Paths.get("src", "lib.rs"), error1.getKey().toPath());
        assertEquals(Paths.get("src", "xor_adder.rs"), error2.getKey().toPath());
        assertEquals(1, error1.getValue().size());
        assertEquals(1, error2.getValue().size());
    }

    @Test
    public void lintingHasRightErrorsWithTwoFiles() {
        Path path = TestUtils.getPath(getClass(), "warningFiles");
        ValidationResult result = cargoPlugin.checkCodeStyle(path, new Locale("en"));
        Map<File, List<ValidationError>> errors = result.getValidationErrors();
        Iterator<Entry<File, List<ValidationError>>> errorIt = errors.entrySet().iterator();
        Entry<File, List<ValidationError>> error1 = errorIt.next();
        Entry<File, List<ValidationError>> error2 = errorIt.next();
        if (error2.getKey().getPath().contains("lib.rs")) {
            Entry<File, List<ValidationError>> tmp = error1;
            error1 = error2;
            error2 = tmp;
        }
        assertEquals(4, error1.getValue().get(0).getLine());
        assertEquals(9, error1.getValue().get(0).getColumn());
        assertTrue(error1.getValue().get(0).getMessage().contains("unused"));
        assertTrue(error1.getValue().get(0).getSourceName().contains("lib.rs"));

        assertEquals(1, error2.getValue().get(0).getLine());
        assertEquals(1, error2.getValue().get(0).getColumn());
        assertTrue(error2.getValue().get(0).getMessage().contains("snake case"));
        assertTrue(error2.getValue().get(0).getSourceName().contains("xor_adder.rs"));
    }

    @Test
    public void scanningExerciseWorks() {
        Path path = TestUtils.getPath(getClass(), "points");
        Optional<ExerciseDesc> desc = cargoPlugin.scanExercise(path, "test");
        assertTrue(desc.isPresent());
        assertEquals("test", desc.get().name);
        assertEquals(1, desc.get().tests.size());
        assertEquals("it_shall_work", desc.get().tests.get(0).name);
        assertEquals(1, desc.get().tests.get(0).points.size());
        assertEquals("10", desc.get().tests.get(0).points.get(0));
    }

    @Test
    public void scanningWithMultipleExerciseWorks() {
        Path path = TestUtils.getPath(getClass(), "multiplePoints");
        Optional<ExerciseDesc> desc = cargoPlugin.scanExercise(path, "test");
        assertTrue(desc.isPresent());
        assertEquals("test", desc.get().name);
        assertEquals(2, desc.get().tests.size());
        TestDesc test1 = desc.get().tests.get(0);
        TestDesc test2 = desc.get().tests.get(1);
        if (test2.name.equals("it_shall_work")) {
            TestDesc tmp = test1;
            test1 = test2;
            test2 = tmp;
        }
        assertEquals("it_shall_work", test1.name);
        assertEquals(1, test1.points.size());
        assertEquals("4", test1.points.get(0));

        assertEquals("it_shall_work2", test2.name);
        assertEquals(1, test2.points.size());
        assertEquals("7", test2.points.get(0));
    }

    @Test
    public void scanningWithSuiteWorks() {
        Path path = TestUtils.getPath(getClass(), "multiplePointsSuite");
        Optional<ExerciseDesc> desc = cargoPlugin.scanExercise(path, "test");
        assertTrue(desc.isPresent());
        assertEquals("test", desc.get().name);
        assertEquals(3, desc.get().tests.size());
        TestDesc test1 = desc.get().tests.get(0);
        TestDesc test2 = desc.get().tests.get(1);
        TestDesc test3 = desc.get().tests.get(2);
        if (test2.name.equals("exercise1")) {
            TestDesc tmp = test1;
            test1 = test2;
            test2 = tmp;
        } else if (test3.name.equals("exercise1")) {
            TestDesc tmp = test1;
            test1 = test3;
            test3 = tmp;
        }
        if (test2.name.contains("2")) {
            TestDesc tmp = test2;
            test2 = test3;
            test3 = tmp;
        }
        assertEquals("exercise1", test1.name);
        assertEquals("exercise1.it_shall_work", test2.name);
        assertEquals("exercise1.it_shall_work2", test3.name);
        assertEquals(1, test1.points.size());
        assertEquals("4", test1.points.get(0));
        assertEquals(2, test2.points.size());
        String point1 = test2.points.get(0);
        String point2 = test2.points.get(1);
        if (point2.equals("4")) {
            String tmp = point1;
            point1 = point2;
            point2 = tmp;
        }
        assertEquals("4", point1);
        assertEquals("6", point2);
        assertEquals(2, test3.points.size());
        point1 = test3.points.get(0);
        point2 = test3.points.get(1);
        if (point2.equals("4")) {
            String tmp = point1;
            point1 = point2;
            point2 = tmp;
        }
        assertEquals("4", point1);
        assertEquals("7", point2);
    }

    @Test
    public void scanningWithMultiplePointsPerTestsWorks() {
        Path path = TestUtils.getPath(getClass(), "multiplePointsPerTest");
        Optional<ExerciseDesc> desc = cargoPlugin.scanExercise(path, "test");
        assertTrue(desc.isPresent());
        assertEquals("test", desc.get().name);
        assertEquals(1, desc.get().tests.size());
        assertEquals("it_shall_work", desc.get().tests.get(0).name);
        assertEquals(2, desc.get().tests.get(0).points.size());
        String test1 = desc.get().tests.get(0).points.get(0);
        String test2 = desc.get().tests.get(0).points.get(1);
        if (test2.equals("10")) {
            String tmp = test1;
            test1 = test2;
            test2 = tmp;
        }
        assertEquals("10", test1);
        assertEquals("9", test2);
    }

    @Test
    public void scanningWithMultiplePointPerSuiteWorks() {
        Path path = TestUtils.getPath(getClass(), "multiplePointsPerSuite");
        Optional<ExerciseDesc> desc = cargoPlugin.scanExercise(path, "test");
        assertTrue(desc.isPresent());
        assertEquals("test", desc.get().name);
        assertEquals(3, desc.get().tests.size());
        TestDesc test1 = desc.get().tests.get(0);
        TestDesc test2 = desc.get().tests.get(1);
        TestDesc test3 = desc.get().tests.get(2);
        if (test2.name.equals("exercise1")) {
            TestDesc tmp = test1;
            test1 = test2;
            test2 = tmp;
        } else if (test3.name.equals("exercise1")) {
            TestDesc tmp = test1;
            test1 = test3;
            test3 = tmp;
        }
        if (test2.name.contains("2")) {
            TestDesc tmp = test2;
            test2 = test3;
            test3 = tmp;
        }
        assertEquals("exercise1", test1.name);
        assertEquals("exercise1.it_shall_work", test2.name);
        assertEquals("exercise1.it_shall_work2", test3.name);
        assertEquals(2, test1.points.size());
        String point1 = test1.points.get(0);
        String point2 = test1.points.get(1);
        if (point2.equals("4")) {
            String tmp = point1;
            point1 = point2;
            point2 = tmp;
        }
        assertEquals("4", point1);
        assertEquals("3", point2);
        assertEquals(3, test2.points.size());
        point1 = test2.points.get(0);
        point2 = test2.points.get(1);
        String point3 = test2.points.get(2);
        if (point2.equals("4")) {
            String tmp = point1;
            point1 = point2;
            point2 = tmp;
        } else if (point3.equals("4")) {
            String tmp = point1;
            point1 = point3;
            point3 = tmp;
        }
        if (point3.equals("3")) {
            String tmp = point2;
            point2 = point3;
            point3 = tmp;
        }
        assertEquals("4", point1);
        assertEquals("3", point2);
        assertEquals("6", point3);
        assertEquals(3, test3.points.size());
        point1 = test3.points.get(0);
        point2 = test3.points.get(1);
        point3 = test2.points.get(2);
        if (point2.equals("4")) {
            String tmp = point1;
            point1 = point2;
            point2 = tmp;
        } else if (point3.equals("4")) {
            String tmp = point1;
            point1 = point3;
            point3 = tmp;
        }
        if (point3.equals("3")) {
            String tmp = point2;
            point2 = point3;
            point3 = tmp;
        }
        assertEquals("4", point1);
        assertEquals("3", point2);
        assertEquals("7", point3);
    }
}
