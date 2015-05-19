package fi.helsinki.cs.tmc.langs;

import java.io.File;
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
    private final String sourceFolderName = "src";
    private final Charset charset = StandardCharsets.UTF_8;

    /**
     * Prepares a stub exercise from the original.
     *
     * <p>
     * Implements LanguagePlugin.prepareStub
     */
    public void prepareStub(Path path) {
        File projectRoot = path.resolve(sourceFolderName).toFile();
        List<File> projectFiles = getFileList(projectRoot);

        for (File projectFile : projectFiles) {
            prepareStubFile(projectFile);
        }
    }

    // TODO: exclude files containing '// SOLUTION FILE'
    private void prepareStubFile(File file) {

        try {
            List<String> lines = Files.readAllLines(file.toPath(), charset);
            List<String> filteredLines = new ArrayList<>();
            boolean skipLine = false;
            for (String line : lines) {
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
            Files.write(file.toPath(), filteredLines, charset);
        } catch (IOException ex) {
            throw new RuntimeException("Unexpected IOException, preparation of file {" + file.getAbsolutePath() + "} interrupted", ex);
        }
    }

    private List<File> getFileList(File folder) {
        if (!folder.isDirectory()) {
            System.out.println("No files found");
            return new ArrayList<>();
        }
        ArrayList<File> result = new ArrayList<>();
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                result.addAll(getFileList(file));
                continue;
            }
            result.add(file);
        }
        return result;
    }

    /**
     * Prepares a presentable solution from the original.
     *
     * <p>
     * Implements LanguagePlugin.preparaSolution
     */
    public void prepareSolution(Path path) {
        File projectRoot = path.resolve(sourceFolderName).toFile();
        List<File> projectFiles = getFileList(projectRoot);

        for (File projectFile : projectFiles) {
            prepareSolutionFile(projectFile);
        }
    }

    private void prepareSolutionFile(File file) {
        try {
            List<String> lines = Files.readAllLines(file.toPath(), charset);
            List<String> filteredLines = new ArrayList<>();
            for (String line : lines) {
                if (line.contains(beginSolution) || line.contains(endSolution) || line.contains(stubMarker)) {
                    continue;
                }
                filteredLines.add(line);
            }
            Files.write(file.toPath(), filteredLines, charset);
        } catch (IOException ex) {
            throw new RuntimeException("Unexpected IOException, preparation of file {" + file.getAbsolutePath() + "} interrupted", ex);
        }
    }

}
