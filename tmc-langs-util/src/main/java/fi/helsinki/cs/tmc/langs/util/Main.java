package fi.helsinki.cs.tmc.langs.util;

import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;
import fi.helsinki.cs.tmc.langs.domain.RunResult;

import com.google.common.base.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class Main {

    private static final String EXERCISE_PATH = "exercisePath";
    private static final String OUTPUT_PATH = "outputPath";
    public static final String HELP_TEXT =
            "\n"
                    + " Usage: Main <command> [<command-arguments>] \n\n"
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
                    + "  Produce an exercise description of an exercise directory.";

    private static TaskExecutor executor = new TaskExecutorImpl();

    private static Logger log = LoggerFactory.getLogger(Main.class);

    /**
     * Main entry point for the CLI.
     */
    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            printHelpAndExit();
        }

        if (Arrays.asList(args).contains("-h") || Arrays.asList(args).contains("--help")) {
            printHelpAndExit();
        }

        run(args);
        System.exit(0);
    }

    public static void setExecutor(TaskExecutor taskExecutor) {
        executor = taskExecutor;
    }

    private static void printHelpAndExit() {
        System.out.println(HELP_TEXT);
        System.exit(0);
    }

    private static void run(String[] args) {
        Map<String, Integer> commands = getCommands();
        String command = args[0];
        Integer pathsCount = commands.get(command);

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
            log.error(
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
            case "run-tests":
                runTests(paths);
                break;
            case "prepare-stub":
                runPrepareStub(paths);
                break;
            case "prepare-solution":
                runPrepareSolution(paths);
                break;
            default:
                printHelpAndExit();
                break;
        }
    }

    private static Map<String, Integer> getCommands() {
        //Command name and required argument count
        Map<String, Integer> commands = new HashMap<>();
        commands.put("help", 0);
        commands.put("checkstyle", 2);
        commands.put("scan-exercise", 2);
        commands.put("run-tests", 2);
        commands.put("prepare-stub", 1);
        commands.put("prepare-solution", 1);
        return commands;
    }

    private static void runCheckCodeStyle(Map<String, Path> paths) {
        ValidationResult validationResult = null;
        try {
            validationResult = executor.runCheckCodeStyle(paths.get(EXERCISE_PATH));
        } catch (NoLanguagePluginFoundException e) {
            log.error(
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
            log.error("Could not write result into {}", paths.get(OUTPUT_PATH), e);
            printErrAndExit("ERROR: Could not write the results to the given file.");
        }
    }

    private static void runScanExercise(Map<String, Path> paths) {
        // Exercise name, should it be something else than directory name?
        String exerciseName = paths.get(EXERCISE_PATH).toFile().getName();
        Optional<ExerciseDesc> exerciseDesc = Optional.absent();
        try {
            exerciseDesc = executor.scanExercise(paths.get(EXERCISE_PATH), exerciseName);

            if (!exerciseDesc.isPresent()) {
                log.error("Absent exercise description after running scanExercise");
                printErrAndExit("ERROR: Could not scan the exercises.");
            }
        } catch (NoLanguagePluginFoundException e) {
            log.error("No suitable language plugin for project at {}", paths.get(EXERCISE_PATH), e);
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
            log.error("Could not write output to {}", paths.get(OUTPUT_PATH), e);
            printErrAndExit("ERROR: Could not write the results to the given file.");
        }
    }

    private static void runTests(Map<String, Path> paths) {
        RunResult runResult = null;
        try {
            runResult = executor.runTests(paths.get(EXERCISE_PATH));
        } catch (NoLanguagePluginFoundException e) {
            log.error("No suitable language plugin for project at {}", paths.get(EXERCISE_PATH), e);
            printErrAndExit(
                    "ERROR: Could not find suitable language plugin for the given "
                            + "exercise path.");
        }

        try {
            JsonWriter.writeObjectIntoJsonFormat(runResult, paths.get(OUTPUT_PATH));
            System.out.println("Test results can be found in " + paths.get(OUTPUT_PATH));
        } catch (IOException e) {
            log.error("Could not write output to {}", paths.get(OUTPUT_PATH), e);
            printErrAndExit("ERROR: Could not write the results to the given file.");
        }
    }

    private static void runPrepareStub(Map<String, Path> paths) {
        try {
            executor.prepareStub(paths.get(EXERCISE_PATH));
        } catch (NoLanguagePluginFoundException e) {
            log.error("No suitable language plugin for project at {}", paths.get(EXERCISE_PATH), e);
            printErrAndExit(
                    "ERROR: Could not find suitable language plugin for the given "
                            + "exercise path.");
        }
    }

    private static void runPrepareSolution(Map<String, Path> paths) {
        try {
            executor.prepareSolution(paths.get(EXERCISE_PATH));
        } catch (NoLanguagePluginFoundException e) {
            log.error("No suitable language plugin for project at {}", paths.get(EXERCISE_PATH), e);
            printErrAndExit(
                    "ERROR: Could not find suitable language plugin for the given "
                            + "exercise path.");
        }
    }

    private static void checkTestPath(Path exercisePath) {
        if (!exercisePath.toFile().isDirectory()) {
            log.error("checkTestPath was given a non directory path {}", exercisePath);
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
        log.error("Exiting");
        System.err.println(errorText);
        System.exit(1);
    }
}
