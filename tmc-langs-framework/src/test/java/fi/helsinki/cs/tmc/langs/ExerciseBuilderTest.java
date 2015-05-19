package fi.helsinki.cs.tmc.langs;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import org.junit.Test;

import static org.junit.Assert.*;

public class ExerciseBuilderTest {

    public ExerciseBuilderTest() {
    }

    private File createTemporaryCopyOf(File file) throws IOException {
        File tempFolder = Files.createTempDir();
        FileUtils.copyDirectory(file, tempFolder);
        tempFolder.deleteOnExit();
        return tempFolder;
    }

    @Test
    public void testPrepareStub() throws IOException {
        System.out.println("prepareStub");
        File originProject = new File("src/test/resources/arith_funcs/");

        File targetFolder = createTemporaryCopyOf(originProject);
        ExerciseBuilder instance = new ExerciseBuilder();

        File expectedFolder = new File("src/test/resources/arith_funcs_stub/src");
        File outputFolder = new File(targetFolder.getAbsolutePath() + "/src");

        instance.prepareStub(targetFolder.toPath());

        assertFileLines(expectedFolder, outputFolder);

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
                assertEquals("Line in file " + fileName + " did not match ", expectedLine, actualLine);
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

    @Test
    public void testPrepareSolution() throws IOException {
        File originProject = new File("src/test/resources/arith_funcs/");

        File targetFolder = createTemporaryCopyOf(originProject);
        ExerciseBuilder instance = new ExerciseBuilder();

        instance.prepareSolution(targetFolder.toPath());

        File expectedFolder = new File("src/test/resources/arith_funcs_solution/src");
        File outputFolder = new File(targetFolder.getAbsolutePath() + "/src");

        assertFileLines(expectedFolder, outputFolder);
    }

}
