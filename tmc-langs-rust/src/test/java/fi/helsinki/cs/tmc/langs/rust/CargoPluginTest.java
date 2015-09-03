package fi.helsinki.cs.tmc.langs.rust;

import com.google.common.base.Optional;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class CargoPluginTest {

    private CargoPlugin cargoPlugin;

    public CargoPluginTest() {
    }

    @Before
    public void setUp() {
        cargoPlugin = new CargoPlugin();
    }

    @Test
    public void isExerciseCorrectTypeDoesntBreakByDirectoryNamedCargoToml() throws IOException {
        Path parent = Files.createTempDirectory("tmc-cargo-test");
        Path cargoToml = parent.resolve("Cargo.toml");
        Files.createDirectory(cargoToml);

        assertFalse(cargoPlugin.isExerciseTypeCorrect(parent));

        Files.delete(cargoToml);
        Files.delete(parent);
    }

    @Test
    public void testMakeProjectWithPassingTestsCompilesAndPassesTests() {
        Path path = TestUtils.getPath(getClass(), "passing");
        RunResult result = cargoPlugin.runTests(path);

        assertEquals(RunResult.Status.PASSED, result.status);
    }

}