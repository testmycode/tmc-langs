package fi.helsinki.cs.tmc.langs.rust;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.SpecialLogs;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
}
