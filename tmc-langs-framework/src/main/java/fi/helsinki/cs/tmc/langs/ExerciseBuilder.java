package fi.helsinki.cs.tmc.langs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;

public class ExerciseBuilder {

    private String beginSolution = "// BEGIN SOLUTION";
    private String endSolution = "// END SOLUTION";
    private String stubMarker = "// STUB:";
    private String sourceFolderName = "src";

    /**
     * Constuctor
     *
     * <p>
     * TODO: Needs to be expanded with LanguagePlugin, which determines the
     * comment style used with the language
     *
     */
    public ExerciseBuilder() {

    }

    /**
     * Prepares a stub exercise from the original.
     *
     * <p>
     * The stub is a copy of the original where the model solution and special
     * comments have been stripped and stubs like ('return 0') have been added.
     *
     * @param path A path to a directory where the original exericse has been
     * copied. This method should modify the contents of this directory.
     */
    public void prepareStub(Path path) {
        File projectRoot = path.resolve(sourceFolderName).toFile();
        List<File> projectFiles = getFileList(projectRoot);

        for (File projectFile : projectFiles) {
            prepareStubFile(projectFile);
        }
    }

    private void prepareStubFile(File file) {
        try {
            List<String> lines = FileUtils.readLines(file);
            List<String> parsedLines = new ArrayList<>();
            boolean skipLine = false;
            for (String line : lines) {
                if (line.contains(beginSolution)) {
                    skipLine = true;
                } else if (skipLine && line.contains(endSolution)) {
                    skipLine = false;
                } else if (!skipLine && !line.contains(stubMarker)) {
                    parsedLines.add(line);
                } else if (line.contains(stubMarker)) {
                    String start = line.substring(0, line.indexOf(stubMarker) - 1);
                    String end = line.substring(line.indexOf(stubMarker) + stubMarker.length());
                    parsedLines.add(start + end);
                }
            }
            FileUtils.writeLines(file, parsedLines);
        } catch (IOException ex) {
            System.out.println("Unexpected IOException, preparation of file {" + file.getAbsolutePath() + "} interrupted");
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
     * The solution usually has stubs and special comments stripped.
     *
     * @param path A path to a directory where the original exericse has been
     * copied. This method should modify the contents of this directory.
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
            List<String> lines = FileUtils.readLines(file);
            List<String> parsedLines = new ArrayList<>();
            for (String line : lines) {
                if (line.contains(beginSolution) || line.contains(endSolution) || line.contains(stubMarker)) {
                    continue;
                } 
                parsedLines.add(line);
            }
            FileUtils.writeLines(file, parsedLines);
        } catch (IOException ex) {
            System.out.println("Unexpected IOException, preparation of file {" + file.getAbsolutePath() + "} interrupted");
        }
    }

}
