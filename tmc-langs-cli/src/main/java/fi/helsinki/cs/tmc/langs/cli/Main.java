package fi.helsinki.cs.tmc.langs.cli;

import fi.helsinki.cs.tmc.langs.LanguagePlugin;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.ExercisePackagingConfiguration;
import fi.helsinki.cs.tmc.langs.domain.Filer;
import fi.helsinki.cs.tmc.langs.domain.FilterFileTreeVisitor;
import fi.helsinki.cs.tmc.langs.domain.GeneralDirectorySkipper;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.util.ProjectType;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/*
 * TODO: unstaticify this class
 */
public final class Main {

    private static TaskExecutor executor = new TaskExecutorImpl();

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final String EXERCISE_PATH = "exercisePath";
    private static final String OUTPUT_PATH = "outputPath";
    private static final String LOCALE = "locale";

    @VisibleForTesting static Map<String, String> argsMap = Maps.newHashMap();

    @VisibleForTesting
    static final String HELP_TEXT =
            " Usage: Main <command> [<command-arguments>] \n\n"
                    + " Commands:\n"
                    + " checkstyle --exercisePath --outputPath --locale"
                    + "     Run checkstyle or similar plugin to project if applicable.\n"
                    + " compress-project --exercisePath --outputPath\n"
                    + " help"
                    + "                                         Display help information.\n"
                    + " prepare-solutions --exercisePath --outputPath"
                    + "             Prepare a presentable solution from the original.\n"
                    + " prepare-stubs --exercisePath -- outputPath"
                    + "                 Prepare a stub exercise from the original.\n"
                    + " prepare-submission  --clonePath --submissionPath --outputPath"
                    + "      Prepares from submission and solution project for which the tests"
                    + " can be run in sandbox\n"
                    + " run-tests --exercisePath --outputPath"
                    + "      Run the tests for the exercise.\n"
                    + " scan-exercise --exercisePath --outputPath"
                    + "  Produce an exercise description of an exercise directory.\n"
                    + " find-exercises --exercisePath --outputPath"
                    + "  Produce list of found exercises.\n"
                    + " get-exercise-packaging-configuration --exercisePath --outputPath"
                    + "  Returns configuration of under which folders student and nonstudent files"
                    + " are located."
                    + " clean --exercisePath"
                    + "  Produce list of found exercises.";

    /**
     * Main entry point for the CLI.
     */
    public static void main(String[] args) {
        if (args == null || args.length <= 1) {
            printHelpAndExit();
        }

        String command = args[0];

        if ("h".equals(command) || "help".equals(command)) {
            printHelpAndExit();
        }

        List<String> argsList = Arrays.asList(args);
        if (argsList.contains("-h") || argsList.contains("--help")) {
            printHelpAndExit();
        }

        parsePaths(args);
        System.out.println(argsMap);
        run(command);
        System.exit(0); // Make sure to kill non daemon threads.
    }

    public static void setExecutor(TaskExecutor taskExecutor) {
        executor = taskExecutor;
    }

    private static void printHelpAndExit() {
        System.out.println(HELP_TEXT);
        System.exit(0); // Make sure to kill non daemon threads.
    }

    private static void run(String command) {

        switch (command) {
            case "help":
                printHelpAndExit();
                break;
            case "checkstyle":
                runCheckCodeStyle();
                break;
            case "compress-project":
                runCompressProject();
                break;
            case "scan-exercise":
                runScanExercise();
                break;
            case "find-exercises":
                runFindExercises();
                break;
            case "run-tests":
                runTests();
                break;
            case "prepare-stubs":
                runPrepareStubs();
                break;
            case "prepare-solutions":
                runPrepareSolutions();
                break;
            case "get-exercise-packaging-configuration":
                runGetExercisePackagingConfiguration();
                break;
            case "clean":
                runClean();
                break;
            default:
                printHelpAndExit();
                break;
        }
    }

    private static Path getExercisePathFromArgs() {
        if (argsMap.containsKey(EXERCISE_PATH)) {
            return Paths.get(argsMap.get(EXERCISE_PATH));
        }
        throw new IllegalStateException("No " + EXERCISE_PATH + " provided");
    }

    private static Locale getLocaleFromArgs() {
        if (argsMap.containsKey(LOCALE)) {
            return new Locale(argsMap.get(LOCALE));
        }
        throw new IllegalStateException("No " + LOCALE + " provided");
    }

    private static Path getOutputPathFromArgs() {
        if (argsMap.containsKey(OUTPUT_PATH)) {
            return Paths.get(argsMap.get(OUTPUT_PATH));
        }
        throw new IllegalStateException("No " + OUTPUT_PATH + " provided");
    }

    private static void runCompressProject() {
        Path exercisePath = getExercisePathFromArgs();
        Path outputPath = getOutputPathFromArgs();
        try {
            byte[] compressed = executor.compressProject(exercisePath);
            Files.write(outputPath, compressed);
        } catch (IOException | NoLanguagePluginFoundException e) {
            e.printStackTrace();
        }
    }

    private static void runCheckCodeStyle() {
        ValidationResult validationResult = null;
        try {
            validationResult =
                    executor.runCheckCodeStyle(getExercisePathFromArgs(), getLocaleFromArgs());
        } catch (NoLanguagePluginFoundException e) {
            logger.error(
                    "No suitable language plugin for project at {}", getExercisePathFromArgs(), e);
            printErrAndExit(
                    "ERROR: Could not find suitable language plugin for the given exercise "
                            + "path.");
        }

        try {
            JsonWriter.writeObjectIntoJsonFormat(validationResult, getOutputPathFromArgs());
            System.out.println("Codestyle report can be found at " + getOutputPathFromArgs());
        } catch (IOException e) {
            logger.error("Could not write result into {}", getOutputPathFromArgs(), e);
            printErrAndExit("ERROR: Could not write the results to the given file.");
        }
    }

    private static void runScanExercise() {
        String exerciseName = getExercisePathFromArgs().toFile().getName();
        Optional<ExerciseDesc> exerciseDesc = Optional.absent();
        try {
            exerciseDesc = executor.scanExercise(getExercisePathFromArgs(), exerciseName);

            if (!exerciseDesc.isPresent()) {
                logger.error("Absent exercise description after running scanExercise");
                printErrAndExit("ERROR: Could not scan the exercises.");
            }
        } catch (NoLanguagePluginFoundException e) {
            logger.error(
                    "No suitable language plugin for project at {}", getExercisePathFromArgs(), e);
            printErrAndExit(
                    "ERROR: Could not find suitable language plugin for the given "
                            + "exercise path.");
        }

        try {
            JsonWriter.writeObjectIntoJsonFormat(exerciseDesc.get(), getOutputPathFromArgs());
            System.out.println(
                    "Exercises scanned successfully, results can be found in "
                            + getOutputPathFromArgs());
        } catch (IOException e) {
            logger.error("Could not write output to {}", getOutputPathFromArgs(), e);
            printErrAndExit("ERROR: Could not write the results to the given file.");
        }
    }

    private static void runFindExercises() {
        Path clonePath = getExercisePathFromArgs();
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
                    public void visitFile(Path source, Path relativePath) {}
                };
        new FilterFileTreeVisitor()
                .addSkipper(new GeneralDirectorySkipper())
                .setClonePath(clonePath)
                .setStartPath(clonePath)
                .setFiler(exerciseMatchingFiler)
                .traverse();

        try {
            JsonWriter.writeObjectIntoJsonFormat(exercises, getOutputPathFromArgs());
            System.out.println("Results can be found in " + getOutputPathFromArgs());
        } catch (IOException e) {
            logger.error("Could not write output to {}", getOutputPathFromArgs(), e);
            printErrAndExit("ERROR: Could not write the results to the given file.");
        }
    }

    private static void runTests() {
        RunResult runResult = null;
        try {
            runResult = executor.runTests(getExercisePathFromArgs());
        } catch (NoLanguagePluginFoundException e) {
            logger.error(
                    "No suitable language plugin for project at {}", getExercisePathFromArgs(), e);
            printErrAndExit(
                    "ERROR: Could not find suitable language plugin for the given "
                            + "exercise path.");
        }

        try {
            JsonWriter.writeObjectIntoJsonFormat(runResult, getOutputPathFromArgs());
            System.out.println("Test results can be found in " + getOutputPathFromArgs());
        } catch (IOException e) {
            logger.error("Could not write output to {}", getOutputPathFromArgs(), e);
            printErrAndExit("ERROR: Could not write the results to the given file.");
        }
    }

    private static void runPrepareStubs() {
        try {
            executor.prepareStubs(
                    findExerciseDirectoriesAndGetLanguagePlugins(),
                    getExercisePathFromArgs(),
                    getOutputPathFromArgs());
        } catch (NoLanguagePluginFoundException e) {
            logger.error(
                    "No suitable language plugin for project at {}", getExercisePathFromArgs(), e);
            printErrAndExit(
                    "ERROR: Could not find suitable language plugin for the given "
                            + "exercise path.");
        }
    }

    private static void runClean() {
        try {
            executor.clean(getExercisePathFromArgs());
        } catch (NoLanguagePluginFoundException e) {
            logger.error(
                    "No suitable language plugin for project at {}", getExercisePathFromArgs(), e);
            printErrAndExit(
                    "ERROR: Could not find suitable language plugin for the given "
                            + "exercise path.");
        }
    }

    private static Map<Path, LanguagePlugin> findExerciseDirectoriesAndGetLanguagePlugins() {
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
                    public void visitFile(Path source, Path relativePath) {}
                };
        new FilterFileTreeVisitor()
                .addSkipper(new GeneralDirectorySkipper())
                .setClonePath(getExercisePathFromArgs())
                .setStartPath(getExercisePathFromArgs())
                .setFiler(exerciseMatchingFiler)
                .traverse();
        return map;
    }

    private static void runPrepareSolutions() {
        try {
            executor.prepareSolutions(
                    findExerciseDirectoriesAndGetLanguagePlugins(),
                    getExercisePathFromArgs(),
                    getOutputPathFromArgs());
        } catch (NoLanguagePluginFoundException e) {
            logger.error(
                    "No suitable language plugin for project at {}", getExercisePathFromArgs(), e);
            printErrAndExit(
                    "ERROR: Could not find suitable language plugin for the given "
                            + "exercise path.");
        }
    }

    private static void runGetExercisePackagingConfiguration() {
        ExercisePackagingConfiguration configuration = null;
        try {
            configuration = executor.getExercisePackagingConfiguration(getExercisePathFromArgs());
        } catch (NoLanguagePluginFoundException e) {
            logger.error(
                    "No suitable language plugin for project at {}", getExercisePathFromArgs(), e);
            printErrAndExit(
                    "ERROR: Could not find suitable language plugin for the given "
                            + "exercise path.");
        }

        try {
            JsonWriter.writeObjectIntoJsonFormat(configuration, getOutputPathFromArgs());
            System.out.println("Results can be found in " + getOutputPathFromArgs());
        } catch (IOException e) {
            logger.error("Could not write output to {}", getOutputPathFromArgs(), e);
            printErrAndExit("ERROR: Could not write the results to the given file.");
        }
    }

    private static void checkTestPath(Path exercisePath) {
        if (!exercisePath.toFile().isDirectory()) {
            logger.error("checkTestPath was given a non directory path {}", exercisePath);
            System.err.println("ERROR: Given test path is not a directory.");
            printHelpAndExit();
        }
    }

    private static void parsePaths(String[] args) {
        Iterator<String> argsIterator = Arrays.asList(args).iterator();
        argsIterator.next(); // the first is the command itself.
        while (argsIterator.hasNext()) {
            String command = argsIterator.next();
            String commandName;
            String commandValue;
            if (command.contains("=")) {
                String[] commandAndValue = command.split("=");
                commandName = commandAndValue[0];
                commandValue = commandAndValue[1];
            } else {
                commandName = command;
                commandValue = argsIterator.next();
            }

            while (commandName.startsWith("-")) {
                commandName = commandName.substring(1);
            }

            argsMap.put(commandName, commandValue);
        }
    }

    private static void printErrAndExit(String errorText) {
        logger.error("Exiting");
        System.err.println(errorText);
        System.exit(1);
    }
}
