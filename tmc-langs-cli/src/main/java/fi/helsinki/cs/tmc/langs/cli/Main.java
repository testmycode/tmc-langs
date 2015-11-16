package fi.helsinki.cs.tmc.langs.cli;

import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;
import fi.helsinki.cs.tmc.langs.LanguagePlugin;
import fi.helsinki.cs.tmc.langs.domain.Filer;
import fi.helsinki.cs.tmc.langs.domain.FilterFileTreeVisitor;
import fi.helsinki.cs.tmc.langs.util.ProjectType;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import fi.helsinki.cs.tmc.langs.domain.GeneralDirectorySkipper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Main {

    private static TaskExecutor executor = new TaskExecutorImpl();

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final String EXERCISE_PATH = "exercisePath";
    private static final String OUTPUT_PATH = "outputPath";

    private static final Map<String, Integer> COMMAND_ARGUMENT_COUNTS =
            new ImmutableMap.Builder<String, Integer>()
                    .put("checkstyle", 2)
                    .put("scan-exercise", 2)
                    .put("run-tests", 2)
                    .put("prepare-stubs", 2)
                    .put("prepare-solutions", 2)
                    .put("find-exercises", 2)
                    .build();

    @VisibleForTesting
    static final String HELP_TEXT =
            " Usage: Main <command> [<command-arguments>] \n\n"
                    + " Commands:\n"
                    + " checkstyle <exercise path> <output path>"
                    + "     Run checkstyle or similar plugin to project if applicable.\n"
                    + " help"
                    + "                                         Display help information.\n"
                    + " prepare-solution <exercise path>"
                    + "             Prepare a presentable solution from the original.\n"
                    + " prepare-stub <exercise path>"
                    + "                 Prepare a stub exercise from the original.\n"
                    + " run-tests <exercise path> <output path>"
                    + "      Run the tests for the exercise.\n"
                    + " scan-exercise <exercise path> <output path>"
                    + "  Produce an exercise description of an exercise directory."
                    + " find-exercises <scan path> <output path>"
                    + "  Produce list of found exercises.";

    /**
     * Main entry point for the CLI.
     */
    public static void main(String[] args) {
        System.out.println(Arrays.deepToString(args));
        if (args == null || args.length == 0) {
            printHelpAndExit();
        }
        List<String> argsList = Arrays.asList(args);
        if (argsList.contains("-h") || argsList.contains("--help")) {
            printHelpAndExit();
        }

        run(args);
        System.exit(0); // Make sure to kill non daemon threads.
    }

    public static void setExecutor(TaskExecutor taskExecutor) {
        executor = taskExecutor;
    }

    private static void printHelpAndExit() {
        System.out.println(HELP_TEXT);
        System.exit(0); // Make sure to kill non daemon threads.
    }

    private static void run(String[] args) {
        Preconditions.checkArgument(args.length >= 1);
        String command = args[0];
        Integer pathsCount = COMMAND_ARGUMENT_COUNTS.get(command);

        if (pathsCount == null || "help".equals(command)) {
            printHelpAndExit();
        } else if (!pathsCount.equals(args.length - 1)) {
            System.err.println(
                    "ERROR: wrong argument count for "
                            + command
                            + " expected "
                            + pathsCount
                            + " got "
                            + (args.length - 1));
            logger.error(
                    "Wrong argument count for {}. Expected {}, got {}",
                    command,
                    pathsCount,
                    args.length - 1);
            printHelpAndExit();
        }

        Map<String, Path> paths = parsePaths(args, pathsCount);

        switch (command) {
            case "help":
                printHelpAndExit();
                break;
            case "checkstyle":
                runCheckCodeStyle(paths);
                break;
            case "scan-exercise":
                runScanExercise(paths);
                break;
            case "find-exercises":
                runFindExercises(paths);
                break;
            case "run-tests":
                runTests(paths);
                break;
            case "prepare-stubs":
                runPrepareStubs(paths);
                break;
            case "prepare-solutions":
                runPrepareSolutions(paths);
                break;
            default:
                printHelpAndExit();
                break;
        }
    }

    private static void runCheckCodeStyle(Map<String, Path> paths) {
        ValidationResult validationResult = null;
        try {
            validationResult = executor.runCheckCodeStyle(paths.get(EXERCISE_PATH));
        } catch (NoLanguagePluginFoundException e) {
            logger.error(
                    "Could not find a language plugin for the project at {}",
                    paths.get(EXERCISE_PATH),
                    e);
            printErrAndExit(
                    "ERROR: Could not find suitable language plugin for the given exercise "
                            + "path.");
        }

        try {
            JsonWriter.writeObjectIntoJsonFormat(validationResult, paths.get(OUTPUT_PATH));
            System.out.println("Codestyle report can be found at " + paths.get(OUTPUT_PATH));
        } catch (IOException e) {
            logger.error("Could not write result into {}", paths.get(OUTPUT_PATH), e);
            printErrAndExit("ERROR: Could not write the results to the given file.");
        }
    }

    private static void runScanExercise(Map<String, Path> paths) {
        System.out.println(paths);
        String exerciseName = paths.get(EXERCISE_PATH).toFile().getName();
        Optional<ExerciseDesc> exerciseDesc = Optional.absent();
        try {
            exerciseDesc = executor.scanExercise(paths.get(EXERCISE_PATH), exerciseName);

            if (!exerciseDesc.isPresent()) {
                logger.error("Absent exercise description after running scanExercise");
                printErrAndExit("ERROR: Could not scan the exercises.");
            }
        } catch (NoLanguagePluginFoundException e) {
            logger.error(
                    "No suitable language plugin for project at {}", paths.get(EXERCISE_PATH), e);
            printErrAndExit(
                    "ERROR: Could not find suitable language plugin for the given "
                            + "exercise path.");
        }

        try {
            JsonWriter.writeObjectIntoJsonFormat(exerciseDesc.get(), paths.get(OUTPUT_PATH));
            System.out.println(
                    "Exercises scanned successfully, results can be found in "
                            + paths.get(OUTPUT_PATH).toString());
        } catch (IOException e) {
            logger.error("Could not write output to {}", paths.get(OUTPUT_PATH), e);
            printErrAndExit("ERROR: Could not write the results to the given file.");
        }
    }

    private static void runFindExercises(Map<String, Path> paths) {
        Path clonePath = paths.get(EXERCISE_PATH);
        final Set<String> exercises = new HashSet<>();
        Filer exerciseMatchingFiler =
                new Filer() {

                    @Override
                    public FileVisitResult decideOnDirectory(Path directory) {
                        if (executor.isExerciseRootDirectory(directory)) {
                            exercises.add(directory.toString());
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public void maybeCopyAndFilterFile(Path file, Path fromPath) {
                        // Just skip
                    }
                };
        new FilterFileTreeVisitor()
                .addSkipper(new GeneralDirectorySkipper())
                .setClonePath(paths.get(EXERCISE_PATH))
                .setFiler(exerciseMatchingFiler)
                .traverse();

        try {
            JsonWriter.writeObjectIntoJsonFormat(exercises, paths.get(OUTPUT_PATH));
            System.out.println("Results can be found in " + paths.get(OUTPUT_PATH));
        } catch (IOException e) {
            logger.error("Could not write output to {}", paths.get(OUTPUT_PATH), e);
            printErrAndExit("ERROR: Could not write the results to the given file.");
        }
    }

    private static void runTests(Map<String, Path> paths) {
        RunResult runResult = null;
        try {
            runResult = executor.runTests(paths.get(EXERCISE_PATH));
        } catch (NoLanguagePluginFoundException e) {
            logger.error(
                    "No suitable language plugin for project at {}", paths.get(EXERCISE_PATH), e);
            printErrAndExit(
                    "ERROR: Could not find suitable language plugin for the given "
                            + "exercise path.");
        }

        try {
            JsonWriter.writeObjectIntoJsonFormat(runResult, paths.get(OUTPUT_PATH));
            System.out.println("Test results can be found in " + paths.get(OUTPUT_PATH));
        } catch (IOException e) {
            logger.error("Could not write output to {}", paths.get(OUTPUT_PATH), e);
            printErrAndExit("ERROR: Could not write the results to the given file.");
        }
    }

    private static void runPrepareStubs(Map<String, Path> paths) {
        try {
            executor.prepareStubs(
                    findExerciseDirectoriesAndGetLanguagePlugins(paths), paths.get(OUTPUT_PATH));
        } catch (NoLanguagePluginFoundException e) {
            logger.error(
                    "No suitable language plugin for project at {}", paths.get(EXERCISE_PATH), e);
            printErrAndExit(
                    "ERROR: Could not find suitable language plugin for the given "
                            + "exercise path.");
        }
    }

    private static Map<Path, LanguagePlugin> findExerciseDirectoriesAndGetLanguagePlugins(
            Map<String, Path> paths) {
        final Map<Path, LanguagePlugin> map = new HashMap<>();
        Filer exerciseMatchingFiler =
                new Filer() {

                    @Override
                    public FileVisitResult decideOnDirectory(Path directory) {
                        if (executor.isExerciseRootDirectory(directory)) {
                            try {
                                map.put(
                                        directory,
                                        ProjectType.getProjectType(directory).getLanguagePlugin());
                            } catch (NoLanguagePluginFoundException ex) {
                                throw new IllegalStateException(ex);
                            }
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public void maybeCopyAndFilterFile(Path file, Path fromPath) {
                        // Just skip
                    }
                };
        new FilterFileTreeVisitor()
                .addSkipper(new GeneralDirectorySkipper())
                .setClonePath(paths.get(EXERCISE_PATH))
                .setFiler(exerciseMatchingFiler)
                .traverse();
        System.out.println(map);
        return map;
    }

    private static void runPrepareSolutions(Map<String, Path> paths) {
        try {

            executor.prepareSolutions(
                    findExerciseDirectoriesAndGetLanguagePlugins(paths), paths.get(OUTPUT_PATH));
        } catch (NoLanguagePluginFoundException e) {
            logger.error(
                    "No suitable language plugin for project at {}", paths.get(EXERCISE_PATH), e);
            printErrAndExit(
                    "ERROR: Could not find suitable language plugin for the given "
                            + "exercise path.");
        }
    }

    private static void checkTestPath(Path exercisePath) {
        if (!exercisePath.toFile().isDirectory()) {
            logger.error("checkTestPath was given a non directory path {}", exercisePath);
            System.err.println("ERROR: Given test path is not a directory.");
            printHelpAndExit();
        }
    }

    private static Map<String, Path> parsePaths(String[] args, int pathsCount) {
        Map<String, Path> argsMap = new HashMap<>();

        if (pathsCount == 0) {
            return argsMap;
        }
        argsMap.put(EXERCISE_PATH, Paths.get(args[1]));

        if (pathsCount == 2) {
            argsMap.put(OUTPUT_PATH, Paths.get(args[2]));
        }

        checkTestPath(argsMap.get(EXERCISE_PATH));

        return argsMap;
    }

    private static void printErrAndExit(String errorText) {
        logger.error("Exiting");
        System.err.println(errorText);
        System.exit(1);
    }
}
