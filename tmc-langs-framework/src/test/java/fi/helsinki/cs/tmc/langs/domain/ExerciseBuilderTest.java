package fi.helsinki.cs.tmc.langs.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.google.common.io.Files;

import org.apache.commons.io.FileUtils;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Ignore
public class ExerciseBuilderTest {

    private ExerciseBuilder exerciseBuilder;

    @Before
    public void setUp() {}

    @Test
    public void test() {}

    private Path createTemporaryCopyOf(Path path) throws IOException {
        File tempFolder = Files.createTempDir();
        FileUtils.copyDirectory(path.toFile(), tempFolder);
        tempFolder.deleteOnExit();
        return tempFolder.toPath();
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
            throw new IOException("Supplied path was not a directory");
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
