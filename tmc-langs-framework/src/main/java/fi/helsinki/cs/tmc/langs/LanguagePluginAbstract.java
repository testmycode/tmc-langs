/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.helsinki.cs.tmc.langs;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public abstract class LanguagePluginAbstract implements LanguagePlugin {
    
    /**
      * Exercisebuilder uses an instance because it is somewhat likely
      * that it will need some language specific configuration
     */
    
    private ExerciseBuilder exerciseBuilder = new ExerciseBuilder();

    @Override
    public void prepareSubmission(Path submissionPath, Path destPath) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
     *
     * @param basePath The file path to search in.
     * @return A list of directories that contain a build file in this language.
     */
    @Override
    public ImmutableList<Path> findExercises(Path basePath) {
        File searchPath = new File(basePath.toString());
        ImmutableList.Builder<Path> listBuilder = new ImmutableList.Builder<>();
        if (searchPath.exists() && searchPath.isDirectory()) {
            return search(searchPath, listBuilder);
        } else {
            return listBuilder.build();
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
    private ImmutableList<Path> search(File file, ImmutableList.Builder<Path> listBuilder) {
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

    /**
     * Start a process using ProcessBuilder.
     *
     * @param args Arguments for starting the process.
     * @return Possible output of the process.
     */
    protected List<String> startProcess(List<String> args) {
        try {
            Process process = new ProcessBuilder(args).start();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            List<String> results = new ArrayList<>();

            while ((line = br.readLine()) != null && !line.equals("")) {
                results.add(line);
            }

            return results;
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
