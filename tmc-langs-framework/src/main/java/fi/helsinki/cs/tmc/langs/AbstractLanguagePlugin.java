package fi.helsinki.cs.tmc.langs;

import com.google.common.collect.ImmutableList;

import fi.helsinki.cs.tmc.stylerunner.CheckstyleRunner;
import fi.helsinki.cs.tmc.stylerunner.exception.TMCCheckstyleException;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;

import java.io.File;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractLanguagePlugin implements LanguagePlugin {

    /**
     * Exercisebuilder uses an instance because it is somewhat likely that it
     * will need some language specific configuration
     */
    private ExerciseBuilder exerciseBuilder = new ExerciseBuilder();

    private static final Logger log = Logger.getLogger(AbstractLanguagePlugin.class.getName());

    @Override
    public void prepareSubmission(Path submissionPath, Path destPath) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void prepareStub(Path path) {
        exerciseBuilder.prepareStub(path);
    }

    @Override
    public void prepareSolution(Path path) {
        exerciseBuilder.prepareSolution(path);
    }

    /**
     * Check if the exercise's project type corresponds with the language plugin
     * type.
     *
     * @param path The path to the exercise directory.
     * @return True if given path is valid directory for this language plugin
     */
    protected abstract boolean isExerciseTypeCorrect(Path path);

    /**
     * @param basePath The file path to search in.
     * @return A list of directories that contain a build file in this language.
     */
    @Override
    public ImmutableList<Path> findExercises(Path basePath) {
        File searchPath = new File(basePath.toString());
        ImmutableList.Builder<Path> listBuilder = new ImmutableList.Builder<>();
        if (searchPath.exists() && searchPath.isDirectory()) {
            return searchForExercises(searchPath, listBuilder);
        } else {
            return listBuilder.build();
        }
    }

    @Override
    public ValidationResult checkCodeStyle(Path path) {
        try {
            System.out.println(path.toString());
            CheckstyleRunner runner = new CheckstyleRunner(path.toFile(), new Locale("fi"));

            return runner.run();
        } catch (TMCCheckstyleException ex) {
            log.log(Level.SEVERE, "Error running checkstyle:", ex);
            return null;
        }
    }

    /**
     * Search a directory and its subdirectories for build files. If a directory
     * contains a build file, the directory is added to the list.
     *
     * @param file The current file path to search in
     * @param listBuilder
     * @return a list of all directories that contain build files for this
     * language.
     */
    private ImmutableList<Path> searchForExercises(File file, ImmutableList.Builder<Path> listBuilder) {
        Stack<File> stack = new Stack();
        // Push the initial directory onto the stack.
        stack.push(file);
        // Walk the directories that get added onto the stack.
        while (!stack.isEmpty()) {
            File current = stack.pop();
            if (current.isDirectory()) {
                // See if current directory contains a build file.
                if (isExerciseTypeCorrect(current.toPath())) {
                    listBuilder.add(current.toPath());
                }
                for (File temp : current.listFiles()) {
                    if (temp.isDirectory()) {
                        stack.push(temp);
                    }
                }
            }
        }
        return listBuilder.build();
    }
}
