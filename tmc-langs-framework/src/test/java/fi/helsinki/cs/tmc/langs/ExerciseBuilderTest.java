package fi.helsinki.cs.tmc.langs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.google.common.io.Files;

import org.apache.commons.io.FileUtils;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExerciseBuilderTest {

    private ExerciseBuilder exerciseBuilder;

    @Before
    public void setUp() {
        exerciseBuilder = new ExerciseBuilder();
    }

    @Test
    public void testPrepareStub() throws IOException {
        File originProject = new File("src"
                + File.separator + "test"
                + File.separator + "resources"
                + File.separator + "arith_funcs" + File.separator);
        File targetFolder = createTemporaryCopyOf(originProject);
        File expectedFolder = new File("src"
                + File.separator + "test"
                + File.separator + "resources"
                + File.separator + "arith_funcs_stub"
                + File.separator + "src");
        File outputFolder = new File(targetFolder.getAbsolutePath() + File.separator + "src");

        exerciseBuilder.prepareStub(targetFolder.toPath());

        assertFileLines(expectedFolder, outputFolder);

    }

    @Test
    public void testPrepareSolution() throws IOException {
        File originProject = new File("src"
                + File.separator + "test"
                + File.separator + "resources"
                + File.separator + "arith_funcs" + File.separator);
        File targetFolder = createTemporaryCopyOf(originProject);
        File expectedFolder = new File("src"
                + File.separator + "test"
                + File.separator + "resources"
                + File.separator + "arith_funcs_solution"
                + File.separator + "src");
        File outputFolder = new File(targetFolder.getAbsolutePath() + File.separator + "src");

        exerciseBuilder.prepareSolution(targetFolder.toPath());

        assertFileLines(expectedFolder, outputFolder);
    }

    @Test
    public void prepareSolutionHandlesNonFolderPath() {
        File originProject = new File("src"
                + File.separator + "test"
                + File.separator + "resources"
                + File.separator + "arith_funcs"
                + File.separator + "build.xml");

        exerciseBuilder.prepareSolution(originProject.toPath());
    }

    @Test
    public void solutionFileLinesAreCorrectlyPrepared() throws IOException {
        Path p = Paths.get("src", "test", "resources", "arith_funcs_solution_file");
        File temp = createTemporaryCopyOf(p.toFile());
        temp.deleteOnExit();
        exerciseBuilder.prepareSolution(temp.toPath());
        Path solutionFile = temp.toPath().resolve(Paths.get("src", "SolutionFile.java"));
        int size = java.nio.file.Files.readAllLines(solutionFile).size();
        assertEquals(2, size);
    }

    @Test
    public void solutionFilesAreIgnoredFromStub() throws IOException {
        Path p = Paths.get("src", "test", "resources", "arith_funcs_solution_file");
        File temp = createTemporaryCopyOf(p.toFile());
        temp.deleteOnExit();
        exerciseBuilder.prepareStub(temp.toPath());
        Path solutionFile = temp.toPath().resolve(Paths.get("src", "SolutionFile.java"));
        assertFalse(solutionFile.toFile().exists());
    }

    private File createTemporaryCopyOf(File file) throws IOException {
        File tempFolder = Files.createTempDir();
        FileUtils.copyDirectory(file, tempFolder);
        tempFolder.deleteOnExit();
        return tempFolder;
    }

    private String getFileName(String parentDir, File file) {
        return file.getAbsolutePath().substring(parentDir.length() + 1);
    }

    private void assertFileLines(File expectedFolder, File outputFolder) throws IOException {

        Map<String, File> expectedFiles = getFileMap(expectedFolder);
        Map<String, File> actualFiles = getFileMap(outputFolder);

        for (String fileName : expectedFiles.keySet()) {
            File expected = expectedFiles.get(fileName);
            File actual = actualFiles.get(fileName);
            List<String> expectedLines = FileUtils.readLines(expected);
            List<String> actualLines = FileUtils.readLines(actual);
            for (int i = 0; i < expectedLines.size(); ++i) {
                String expectedLine = expectedLines.get(i);
                String actualLine = actualLines.get(i);
                assertEquals("Line in file " + fileName + " did not match ",
                        expectedLine,
                        actualLine);
            }

        }
    }

    private Map<String, File> getFileMap(File folder) throws IOException {
        if (!folder.isDirectory()) {
            throw new IOException("Supplied path was not a directory");
        }
        Map<String, File> result = new HashMap<>();
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                result.putAll(getFileMap(file));
                continue;
            }
            result.put(getFileName(folder.getAbsolutePath(), file), file);
        }
        return result;
    }

}
