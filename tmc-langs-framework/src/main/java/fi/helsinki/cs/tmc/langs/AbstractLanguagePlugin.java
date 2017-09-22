package fi.helsinki.cs.tmc.langs;

import fi.helsinki.cs.tmc.langs.domain.Configuration;
import fi.helsinki.cs.tmc.langs.domain.ExerciseBuilder;
import fi.helsinki.cs.tmc.langs.domain.ExercisePackagingConfiguration;
import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.io.sandbox.SubmissionProcessor;
import fi.helsinki.cs.tmc.langs.io.zip.Unzipper;
import fi.helsinki.cs.tmc.langs.io.zip.Zipper;

import com.google.common.collect.ImmutableList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public abstract class AbstractLanguagePlugin implements LanguagePlugin {

    private final ExerciseBuilder exerciseBuilder;
    private final SubmissionProcessor submissionProcessor;
    private final Zipper zipper;
    private final Unzipper unzipper;

    /**
     * Instantiates a new AbstractLanguagePlugin.
     */
    public AbstractLanguagePlugin(
            ExerciseBuilder exerciseBuilder,
            SubmissionProcessor submissionProcessor,
            Zipper zipper,
            Unzipper unzipper) {
        this.exerciseBuilder = exerciseBuilder;
        this.submissionProcessor = submissionProcessor;
        this.zipper = zipper;
        this.unzipper = unzipper;
    }

    /**
     * Check if the exercise's project type corresponds with the language plugin
     * type.
     *
     * @param path The path to the exercise directory.
     * @return True if given path is valid directory for this language plugin
     */
    public abstract boolean isExerciseTypeCorrect(Path path);

    /**
     * Gets a language specific {@link StudentFilePolicy}.
     *
     * <p>The project root path must be specified for the {@link StudentFilePolicy} to read
     * any configuration files such as <tt>.tmcproject.yml</tt>.
     *
     * @param projectPath The project's root path
     */
    protected abstract StudentFilePolicy getStudentFilePolicy(Path projectPath);

    @Override
    public String getLanguageName() {
        return getPluginName();
    }

    @Override
    public void prepareSubmission(Path submissionPath, Path destPath) {
        submissionProcessor.setStudentFilePolicy(getStudentFilePolicy(destPath));
        submissionProcessor.moveFiles(submissionPath, destPath);
    }

    @Override
    public void extractProject(Path compressedProject, Path targetLocation) throws IOException {
        unzipper.setStudentFilePolicy(getStudentFilePolicy(targetLocation));
        unzipper.unzip(compressedProject, targetLocation);
    }

    @Override
    public byte[] compressProject(Path project) throws IOException {
        zipper.setStudentFilePolicy(getStudentFilePolicy(project));
        return zipper.zip(project);
    }

    @Override
    public void prepareStubs(Map<Path, LanguagePlugin> exerciseMap, Path repoPath, Path destPath) {
        exerciseBuilder.prepareStubs(exerciseMap, repoPath, destPath);
    }

    @Override
    public void prepareSolutions(
            Map<Path, LanguagePlugin> exerciseMap, Path repoPath, Path destPath) {
        exerciseBuilder.prepareSolutions(exerciseMap, repoPath, destPath);
    }

    /**
     * TODO: rewrite using the exercise finder used by find exercises of the tmc-langs-cli.
     *
     * @param basePath The file path to search in.
     * @return A list of directories that contain a build file in this language.
     */
    @Override
    public ImmutableList<Path> findExercises(Path basePath) {
        File searchPath = basePath.toFile();
        ImmutableList.Builder<Path> listBuilder = new ImmutableList.Builder<>();
        if (searchPath.exists() && searchPath.isDirectory()) {
            return searchForExercises(searchPath, listBuilder);
        } else {
            return listBuilder.build();
        }
    }

    /**
     * Reads and parses the configuration file of the project.
     * @return The configuration as an object.
     */
    protected Configuration getConfiguration(Path projectRoot) {
        return new Configuration(projectRoot);
    }

    /**
     * Search a directory and its subdirectories for build files. If a directory
     * contains a build file, the directory is added to the list.
     *
     * @param file The current file path to search in
     * @param listBuilder a listBuilder the found exercises should be appended to
     * @return a list of all directories that contain build files for this language.
     */
    private ImmutableList<Path> searchForExercises(
            File file, ImmutableList.Builder<Path> listBuilder) {
        Stack<File> stack = new Stack<>();
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

    @Override
    public ExercisePackagingConfiguration getExercisePackagingConfiguration(Path path) {
        Configuration configuration = getConfiguration(path);
        List<Path> extraStudentFiles = configuration.getExtraStudentFiles();
        List<String> extraStudentStrings = new ArrayList<>();
        for (Path p : extraStudentFiles) {
            extraStudentStrings.add(p.toString());
        }
        ImmutableList<String> studentFiles =
                ImmutableList.<String>builder().add("src").addAll(extraStudentStrings).build();
        ImmutableList<String> src = ImmutableList.of("src");
        return new ExercisePackagingConfiguration(studentFiles, ImmutableList.of("test"));
    }

    @Override
    public void maybeCopySharedStuff(Path destPath) {
        // Ignore by default.
    }
}
