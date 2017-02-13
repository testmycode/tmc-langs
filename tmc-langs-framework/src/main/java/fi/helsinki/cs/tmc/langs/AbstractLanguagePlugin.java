package fi.helsinki.cs.tmc.langs;

import fi.helsinki.cs.tmc.langs.domain.Configuration;
import fi.helsinki.cs.tmc.langs.domain.ExerciseBuilder;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.ExercisePackagingConfiguration;
import fi.helsinki.cs.tmc.langs.domain.TestDesc;
import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.io.sandbox.SubmissionProcessor;
import fi.helsinki.cs.tmc.langs.io.zip.Unzipper;
import fi.helsinki.cs.tmc.langs.io.zip.Zipper;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public Optional<ImmutableList<String>> availablePoints(Path path) {
        Optional<ExerciseDesc> scanned = scanExercise(path, "lol");
        if (!scanned.isPresent()) {
            return Optional.absent();
        }
        List<String> res = new ArrayList<>();
        ImmutableList<TestDesc> tests = scanned.get().tests;
        for (TestDesc desc : tests) {
            for (String point : desc.points) {
                res.add(point);
            }
        }
        return Optional.of(ImmutableList.copyOf(res));
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
    public ExercisePackagingConfiguration getExercisePackagingConfiguration() {
        return new ExercisePackagingConfiguration(
                ImmutableList.of("src"), ImmutableList.of("test"));
    }

    @Override
    public void maybeCopySharedStuff(Path destPath) {
        // Ignore by default.
    }

    protected Optional<ImmutableList<String>> findAvailablePoints(final Path rootPath,
            Pattern pattern, Pattern commentPattern, String suffix) {
        Set<String> points = new HashSet<>();
        try {
            final List<Path> potentialPointFiles = getPotentialPointFiles(rootPath, suffix);
            for (Path p : potentialPointFiles) {

                String contents = new String(Files.readAllBytes(p), "UTF-8");
                String withoutComments = commentPattern.matcher(contents).replaceAll("");
                Matcher matcher = pattern.matcher(withoutComments);
                while (matcher.find()) {
                    MatchResult matchResult = matcher.toMatchResult();
                    String group = matchResult.group(1).trim().replaceAll("\\\"", "\"");
                    points.add(group);
                }
            }
            return Optional.of(ImmutableList.copyOf(points));
        } catch (IOException e) {
            // We could scan the exercise but that could be a security risk
            return Optional.absent();
        }
    }

    private List<Path> getPotentialPointFiles(final Path rootPath, final String suffix)
            throws IOException {
        final StudentFilePolicy studentFilePolicy = getStudentFilePolicy(rootPath);
        final List<Path> potentialPointFiles = new ArrayList<>();

        Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs)
                throws IOException {
                if (studentFilePolicy.isStudentFile(path, rootPath)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                if (!Files.isDirectory(path) && path.toString().endsWith(suffix)) {
                    potentialPointFiles.add(path);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return  potentialPointFiles;
    }
}
