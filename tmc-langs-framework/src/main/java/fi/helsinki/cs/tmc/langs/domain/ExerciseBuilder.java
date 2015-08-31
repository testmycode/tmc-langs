package fi.helsinki.cs.tmc.langs.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ExerciseBuilder {

    private static final String BEGIN_SOLUTION_TAG = "// BEGIN SOLUTION";
    private static final String END_SOLUTION_TAG = "// END SOLUTION";
    private static final String STUB_TAG = "// STUB:";
    private static final String SOLUTION_FILE_TAG = "// SOLUTION FILE";
    private static final String SOURCE_FOLDER_NAME = "src";
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final Logger logger = LoggerFactory.getLogger(ExerciseBuilder.class);

    /**
     * Prepares a stub exercise from the original.
     *
     * <p>Implements LanguagePlugin.prepareStub
     */
    public void prepareStub(Path path) {
        Path projectRoot = path.resolve(SOURCE_FOLDER_NAME);
        List<Path> projectFiles = getFileList(projectRoot);

        for (Path projectFile : projectFiles) {
            prepareStubFile(projectFile);
        }
    }

    private void prepareStubFile(Path file) {
        try {
            List<String> lines = Files.readAllLines(file, CHARSET);
            List<String> filteredLines = new ArrayList<>();
            boolean skipLine = false;
            for (String line : lines) {
                if (line.contains(SOLUTION_FILE_TAG)) {
                    Files.deleteIfExists(file);
                    return;
                }
                if (line.contains(BEGIN_SOLUTION_TAG)) {
                    skipLine = true;
                } else if (skipLine && line.contains(END_SOLUTION_TAG)) {
                    skipLine = false;
                } else if (line.contains(STUB_TAG)) {
                    String start = line.substring(0, line.indexOf(STUB_TAG) - 1);
                    String end = line.substring(line.indexOf(STUB_TAG) + STUB_TAG.length());
                    filteredLines.add(start + end);
                } else if (!skipLine) {
                    filteredLines.add(line);
                }
            }
            Files.write(file, filteredLines, CHARSET);
        } catch (IOException ex) {
            logger.error("Unexpected IOException, preparation of file {} was interrupted",
                    file.toAbsolutePath().toString(),
                    ex);
        }
    }

    private List<Path> getFileList(Path folder) {
        if (!folder.toFile().isDirectory()) {
            return new ArrayList<>();
        }
        ArrayList<Path> result = new ArrayList<>();
        try {
            for (Path file : Files.newDirectoryStream(folder)) {
                if (file.toFile().isDirectory()) {
                    result.addAll(getFileList(file));
                    continue;
                }
                result.add(file);
            }
        } catch (IOException e) {
            logger.error("Unexpected IOException, getting file list of {} was interrupted",
                    folder.toAbsolutePath().toString(),
                    e);
        }
        return result;
    }

    /**
     * Prepares a presentable solution from the original.
     *
     * <p>Implements LanguagePlugin.prepareSolution
     */
    public void prepareSolution(Path path) {
        Path projectRoot = path.resolve(SOURCE_FOLDER_NAME);
        List<Path> projectFiles = getFileList(projectRoot);

        for (Path projectFile : projectFiles) {
            prepareSolutionFile(projectFile);
        }
    }

    private void prepareSolutionFile(Path file) {
        try {
            List<String> lines = Files.readAllLines(file, CHARSET);
            List<String> filteredLines = new ArrayList<>();
            for (String line : lines) {
                if (line.contains(BEGIN_SOLUTION_TAG)
                        || line.contains(END_SOLUTION_TAG)
                        || line.contains(STUB_TAG)
                        || line.contains(SOLUTION_FILE_TAG)) {
                    continue;
                }
                filteredLines.add(line);
            }
            Files.write(file, filteredLines, CHARSET);
        } catch (IOException ex) {
            logger.error("Unexpected IOException, preparation of file {} was interrupted",
                    file.toAbsolutePath().toString(),
                    ex);
        }
    }
}