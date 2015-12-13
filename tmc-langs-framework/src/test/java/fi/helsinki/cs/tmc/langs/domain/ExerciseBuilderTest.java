package fi.helsinki.cs.tmc.langs.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
import org.junit.After;

public class ExerciseBuilderTest {
    
    final private String testFolderName = "filer_tests_in";
    final private String expectedFolderStub = "filer_tests_out_stub";
    final private String expectedFolderSolution = "filer_tests_out_solution";
    private Path originPath = Paths.get("src", "test", "resources", testFolderName);
    private Path tempDir; // root directory for these tests
    private Path cloneDir; // input will be cloned here
    private Path stubDir; // output for stub
    private Path solutionDir; // output for solution

    private ExerciseBuilder exerciseBuilder;
    private Map<Path, LanguagePlugin> exerciseMap;

    @Mock LanguagePlugin languagePlugin;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        exerciseBuilder = new ExerciseBuilder();
        tempDir = Files.createTempDirectory("tmc-langs");
        tempDir.toFile().deleteOnExit(); // precaution, deleted in tearDown
        
        tempDir.resolve("clone").toFile().mkdirs();
        tempDir.resolve("stub").toFile().mkdirs();
        tempDir.resolve("solution").toFile().mkdirs();
        cloneDir = tempDir.resolve(Paths.get("clone"));
        stubDir = tempDir.resolve(Paths.get("stub"));
        solutionDir = tempDir.resolve(Paths.get("solution"));
        
        createTemporaryCopyOf(originPath, cloneDir.resolve(testFolderName));
        exerciseMap = ImmutableMap.of(cloneDir.resolve(testFolderName), languagePlugin);
    }
    
    @After
    public void tearDown() {
        tempDir.toFile().delete();
    }

    @Test
    public void testPrepareStubs() throws IOException, InterruptedException {
        exerciseBuilder.prepareStubs(exerciseMap, cloneDir, stubDir);
        Path expected = Paths.get("src", "test", "resources", expectedFolderStub, "src");
        assertFileLines(expected, stubDir.resolve(testFolderName));
    }

    @Test
    public void testPrepareSolutions() throws IOException, InterruptedException {
        exerciseBuilder.prepareSolutions(exerciseMap, cloneDir, solutionDir);
        Path expected = Paths.get("src", "test", "resources", expectedFolderSolution, "src");
        assertFileLines(expected, solutionDir.resolve(testFolderName).resolve("src"));
    }

    private void createTemporaryCopyOf(Path from, Path to) throws IOException {
        FileUtils.copyDirectory(from.toFile(), to.toFile());
    }

    /** Asserts that files in expected and actual folders match **/
    private void assertFileLines(Path expectedFolder, Path actualFolder) throws IOException {
        Map<String, Path> expectedFiles = getFileMap(expectedFolder);
        Map<String, Path> actualFiles = getFileMap(actualFolder);
        for (String fileName : expectedFiles.keySet()) {
            assertTrue("File " + fileName + " not found in map for " + actualFolder,
                    actualFiles.containsKey(fileName));
            Path expected = expectedFiles.get(fileName);
            Path actual = actualFiles.get(fileName);
            assertTrue("File found on map but not on disk: " + expected,
                    Files.exists(expected));
            assertTrue("File found on map but not on disk: " + actual,
                    Files.exists(actual));
            List<String> expectedLines = FileUtils.readLines(expected.toFile());
            List<String> actualLines = FileUtils.readLines(actual.toFile());
            for (int i = 0; i < expectedLines.size(); ++i) {
                String expectedLine = expectedLines.get(i);
                String actualLine = actualLines.get(i);
                assertEquals(
                        "Line in file " + fileName + " did not match ", expectedLine, actualLine);
            }
        }
        for (String fileName : actualFiles.keySet()) {
            assertTrue("Did not expect to find file " + fileName,
                    expectedFiles.containsKey(fileName));
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
