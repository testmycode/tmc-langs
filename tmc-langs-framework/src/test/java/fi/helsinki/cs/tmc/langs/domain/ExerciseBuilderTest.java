package fi.helsinki.cs.tmc.langs.domain;

import static org.junit.Assert.assertEquals;

import fi.helsinki.cs.tmc.langs.LanguagePlugin;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.io.FileUtils;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExerciseBuilderTest {

    private ExerciseBuilder exerciseBuilder;

    @Mock LanguagePlugin languagePlugin;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        exerciseBuilder = new ExerciseBuilder();
    }

    @Test
    public void testPrepareStubs() throws IOException, InterruptedException {
        final Path originProject = Paths.get("src", "test", "resources", "arith_funcs");
        final Path tempDir = Files.createTempDirectory("tmc-langs");
        tempDir.toFile().deleteOnExit();

        tempDir.resolve("clone.c").toFile().mkdirs();
        tempDir.resolve("stub.c").toFile().mkdirs();

        final Path cloneDir = tempDir.resolve(Paths.get("clone.c"));
        final Path stubDir = tempDir.resolve(Paths.get("stub.c"));

        createTemporaryCopyOf(originProject, cloneDir.resolve("arith_funcs"));

        final Map<Path, LanguagePlugin> exerciseMap =
                ImmutableMap.of(cloneDir.resolve("arith_funcs"), languagePlugin);

        exerciseBuilder.prepareStubs(exerciseMap, cloneDir, stubDir);

        Path expected = Paths.get("src", "test", "resources", "arith_funcs_stub");
        assertFileLines(expected, stubDir.resolve("arith_funcs"));
    }

    @Test
    public void testPrepareSolutions() throws IOException, InterruptedException {
        final Path originProject = Paths.get("src", "test", "resources", "arith_funcs");
        final Path tempDir = Files.createTempDirectory("tmc-langs");
        tempDir.toFile().deleteOnExit();

        tempDir.resolve("clone.c").toFile().mkdirs();
        tempDir.resolve("solution.c").toFile().mkdirs();

        final Path cloneDir = tempDir.resolve(Paths.get("clone.c"));

        final Path solutionDir = tempDir.resolve(Paths.get("solution.c"));

        createTemporaryCopyOf(originProject, cloneDir.resolve("arith_funcs"));

        final Map<Path, LanguagePlugin> exerciseMap =
                ImmutableMap.of(cloneDir.resolve("arith_funcs"), languagePlugin);

        exerciseBuilder.prepareSolutions(exerciseMap, cloneDir, solutionDir);

        Path expected = Paths.get("src", "test", "resources", "arith_funcs_solution", "src");
        assertFileLines(expected, solutionDir.resolve("arith_funcs").resolve("src"));
    }

    private void createTemporaryCopyOf(Path from, Path to) throws IOException {
        FileUtils.copyDirectory(from.toFile(), to.toFile());
    }

    private void assertFileLines(Path expectedFolder, Path outputFolder) throws IOException {
        Map<String, Path> expectedFiles = getFileMap(expectedFolder);
        Map<String, Path> actualFiles = getFileMap(outputFolder);
        for (String fileName : expectedFiles.keySet()) {
            Path expected = expectedFiles.get(fileName);
            Path actual = actualFiles.get(fileName);
            List<String> expectedLines = FileUtils.readLines(expected.toFile());
            List<String> actualLines = FileUtils.readLines(actual.toFile());
            for (int i = 0; i < expectedLines.size(); ++i) {
                String expectedLine = expectedLines.get(i);
                String actualLine = actualLines.get(i);
                assertEquals(
                        "Line in file " + fileName + " did not match ", expectedLine, actualLine);
            }
        }
    }

    private Map<String, Path> getFileMap(Path folder) throws IOException {
        if (!folder.toFile().isDirectory()) {
            throw new IOException("Supplied path was not a directory: " + folder);
        }
        Map<String, Path> result = new HashMap<>();
        for (File file : folder.toFile().listFiles()) {
            if (file.isDirectory()) {
                result.putAll(getFileMap(file.toPath()));
                continue;
            }
            result.put(file.toPath().getFileName().toString(), file.toPath());
        }
        return result;
    }
}
