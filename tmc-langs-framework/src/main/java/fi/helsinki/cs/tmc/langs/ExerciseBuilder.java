package fi.helsinki.cs.tmc.langs;

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

    private final String beginSolution = "// BEGIN SOLUTION";
    private final String endSolution = "// END SOLUTION";
    private final String stubMarker = "// STUB:";
    private final String solutionFile = "// SOLUTION FILE";
    private final String sourceFolderName = "src";
    private final Charset charset = StandardCharsets.UTF_8;

    private Logger log = LoggerFactory.getLogger(ExerciseBuilder.class);

    /**
     * Prepares a stub exercise from the original.
     *
     * <p>Implements LanguagePlugin.prepareStub
     */
    public void prepareStub(Path path) {
        Path projectRoot = path.resolve(sourceFolderName);
        List<Path> projectFiles = getFileList(projectRoot);

        for (Path projectFile : projectFiles) {
            prepareStubFile(projectFile);
        }
    }

    private void prepareStubFile(Path file) {
        try {
            List<String> lines = Files.readAllLines(file, charset);
            List<String> filteredLines = new ArrayList<>();
            boolean skipLine = false;
            for (String line : lines) {
                if (line.contains(solutionFile)) {
                    Files.deleteIfExists(file);
                    return;
                }
                if (line.contains(beginSolution)) {
                    skipLine = true;
                } else if (skipLine && line.contains(endSolution)) {
                    skipLine = false;
                } else if (line.contains(stubMarker)) {
                    String start = line.substring(0, line.indexOf(stubMarker) - 1);
                    String end = line.substring(line.indexOf(stubMarker) + stubMarker.length());
                    filteredLines.add(start + end);
                } else if (!skipLine) {
                    filteredLines.add(line);
                }
            }
            Files.write(file, filteredLines, charset);
        } catch (IOException ex) {
            log.error("Unexpected IOException, preparation of file {} was interrupted",
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
            log.error("Unexpected IOException, getting file list of {} was interrupted",
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
        Path projectRoot = path.resolve(sourceFolderName);
        List<Path> projectFiles = getFileList(projectRoot);

        for (Path projectFile : projectFiles) {
            prepareSolutionFile(projectFile);
        }
    }

    private void prepareSolutionFile(Path file) {
        try {
            List<String> lines = Files.readAllLines(file, charset);
            List<String> filteredLines = new ArrayList<>();
            for (String line : lines) {
                if (line.contains(beginSolution)
                        || line.contains(endSolution)
                        || line.contains(stubMarker)
                        || line.contains(solutionFile)) {
                    continue;
                }
                filteredLines.add(line);
            }
            Files.write(file, filteredLines, charset);
        } catch (IOException ex) {
            log.error("Unexpected IOException, preparation of file {} was interrupted",
                    file.toAbsolutePath().toString(),
                    ex);
        }
    }

}
