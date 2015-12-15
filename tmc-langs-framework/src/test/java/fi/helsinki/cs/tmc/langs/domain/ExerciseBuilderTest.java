package fi.helsinki.cs.tmc.langs.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.LanguagePlugin;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.io.FileUtils;

import org.junit.After;
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

    ExerciseBuilder exerciseBuilder;
    Map<Path, LanguagePlugin> exerciseMap;

    final String testFolderName = "filer_tests_in";
    final Path originPath
            = Paths.get("src", "test", "resources", testFolderName);
    final Path expectedStubs
            = Paths.get("src", "test", "resources", "filer_tests_out_stub", "src");
    final Path expectedSolutions
            = Paths.get("src", "test", "resources", "filer_tests_out_solution", "src");

    Path tempDir; // root directory for these tests
    Path clones; // input will be cloned here
    Path actualStubs; // output for stub
    Path actualSolutions; // output for solution

    @Mock
    LanguagePlugin languagePlugin;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        exerciseBuilder = new ExerciseBuilder();
        initializeTempFolder();
        exerciseMap = ImmutableMap.of(clones.resolve(testFolderName), languagePlugin);
    }

    @After
    public void tearDown() {
        tempDir.toFile().delete();
    }

    @Test
    public void testPrepareStubs() throws IOException, InterruptedException {
        exerciseBuilder.prepareStubs(exerciseMap, clones, actualStubs);
        assertFoldersMatch(expectedStubs, actualStubs);
    }

    @Test
    public void testPrepareSolutions() throws IOException, InterruptedException {
        exerciseBuilder.prepareSolutions(exerciseMap, clones, actualSolutions);
        assertFoldersMatch(expectedSolutions, actualSolutions);
    }

    private void assertFoldersMatch(Path expected, Path actual) throws IOException {
        assertDifferentFolders(expected, actual);
        assertFolderHasFiles(expected);
        assertFileNamesMatch(expected, actual);
        assertFileNamesMatch(actual, expected);
        assertFileContentEqual(expected, actual);
    }

    private void assertDifferentFolders(Path expected, Path actual) {
        assertFalse("Tests are broken, expected and actual folders are the same",
                expected.toFile().equals(actual.toFile()));
    }

    private void assertFileNamesMatch(Path set1, Path set2) throws IOException {
        for (String fileName : getFileMap(set1).keySet()) {
            assertTrue("File " + fileName + " not found in " + set2,
                    getFileMap(set2).containsKey(fileName));
        }
    }

    private void assertFolderHasFiles(Path folder) throws IOException {
        assertFalse("Unable to find files in " + folder,
                getFileMap(folder).isEmpty());
    }

    private void assertFileContentEqual(Path expectedFolder, Path actualFolder) throws IOException {
        Map<String, Path> expectedFiles = getFileMap(expectedFolder);
        Map<String, Path> actualFiles = getFileMap(actualFolder);
        for (String fileName : expectedFiles.keySet()) {
            File expectedFile = expectedFiles.get(fileName).toFile();
            File actualFile = actualFiles.get(fileName).toFile();
            List<String> expectedLines = FileUtils.readLines(expectedFile);
            List<String> actualLines = FileUtils.readLines(actualFile);
            for (int i = 0; i < expectedLines.size(); ++i) {
                String expectedLine = expectedLines.get(i);
                assertTrue("Did not find expected line " + expectedLine
                        + "\n in file " + fileName
                        + "\n Total lines =" + actualLines.size()
                        + "\n Trying to read index i=" + i,
                        actualLines.size() > i);
                String actualLine = actualLines.get(i);
                assertEquals(
                        "Line in file " + fileName + " did not match."
                        + "\n Expected= " + expectedLine
                        + "\n Actual= " + actualLine, expectedLine, actualLine);
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

    public void initializeTempFolder() throws IOException {
        tempDir = Files.createTempDirectory("tmc-langs");
        tempDir.toFile().deleteOnExit(); // precaution, deleted in tearDown

        tempDir.resolve("clone").toFile().mkdirs();
        tempDir.resolve("stub").toFile().mkdirs();
        tempDir.resolve("solution").toFile().mkdirs();

        clones = tempDir.resolve(Paths.get("clone"));
        actualStubs = tempDir.resolve(Paths.get("stub"));
        actualSolutions = tempDir.resolve(Paths.get("solution"));

        File from = originPath.toFile();
        File to = clones.resolve(testFolderName).toFile();
        FileUtils.copyDirectory(from, to);
    }
}
