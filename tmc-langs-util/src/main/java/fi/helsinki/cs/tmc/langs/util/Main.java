package fi.helsinki.cs.tmc.langs.util;

import com.google.common.base.Optional;
import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Main {

    final static String EXERCISE_PATH = "exercisePath";
    final static String OUTPUT_PATH = "outputPath";
    private static TaskExecutor executor = new TaskExecutorImpl();

    public Main(TaskExecutor executor) {
        Main.executor = executor;
    }

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            printHelp();
        }
        run(args);
        System.exit(0);
    }

    private static void printHelp() {
        System.out.println("\nUsage: Main <exercise path> <output path>\n"
                + "\nOptions:\n"
                + " --checkstyle <exercise path> <output path>\t\tRun checkstyle or similar plugin to project if applicable.\n"
                + " --help\t\t\t\t\t\t\tDisplay help information.\n"
                + " --preparesolution <exercise path>\t\t\tPrepare a presentable solution from the original.\n"
                + " --preparestub <exercise path>\t\t\t\tPrepare a stub exercise from the original.\n"
                + " --runtests <exercise path> <output path>\t\tRun the tests for the exercise.\n"
                + " --scanexercise <exercise path> <output path>\t\tProduce an exercise description of an exercise directory.\n");
        System.exit(0);
    }

    private static void run(String[] args) {
        Map<String, Integer> commands = getCommands();
        String command = args[0];
        Integer pathsCount = commands.get(command);

        if (pathsCount == null) {
            printHelp();
        } else if (!pathsCount.equals(args.length - 1)) {
            System.out.println("ERROR: wrong argument count for " + command
                    + " expected " + pathsCount + " got " + (args.length - 1));
            printHelp();
        }

        Map<String, Path> paths = parsePaths(args, pathsCount);

        switch (command) {
            case "--help":
                printHelp();
                break;
            case "--checkstyle":
                runCheckCodeStyle(paths);
                break;
            case "--scanexercise":
                runScanExercise(paths);
                break;
            case "--runtests":
                runTests(paths);
                break;
            case "--preparestub":
                runPrepareStub(paths);
                break;
            case "--preparesolution":
                runPrepareSolution(paths);
                break;
            default:
                printHelp();
                break;
        }
    }

    private static Map<String, Integer> getCommands() {
        //Command name and required argument count
        Map<String, Integer> commands = new HashMap<>();
        commands.put("--help", 0);
        commands.put("--checkstyle", 2);
        commands.put("--scanexercise", 2);
        commands.put("--runtests", 2);
        commands.put("--preparestub", 1);
        commands.put("--preparesolution", 1);
        return commands;
    }

    private static void runCheckCodeStyle(Map<String, Path> paths) {
        Optional<ValidationResult> validationResult = executor.runCheckCodeStyle(paths.get(EXERCISE_PATH));

        if (validationResult.isPresent()) {
            JsonWriter.writeObjectIntoJsonFormat(validationResult.get(), paths.get(OUTPUT_PATH));
            System.out.println("Codestyle report can be found at " + paths.get(OUTPUT_PATH));
        } else {
            System.out.println("ERROR: Something went wrong, could not output validation result!");
        }
    }

    private static void runScanExercise(Map<String, Path> paths) {
        // Exercise name, should it be something else than directory name?
        String exerciseName = paths.get(EXERCISE_PATH).toFile().getName();
        Optional<ExerciseDesc> exerciseDesc = executor.scanExercise(paths.get(EXERCISE_PATH), exerciseName);

        if (exerciseDesc.isPresent()) {
            JsonWriter.writeObjectIntoJsonFormat(exerciseDesc.get(), paths.get(OUTPUT_PATH));
            System.out.println("Exercises scanned successfully, results can be found in " + paths.get(OUTPUT_PATH).toString());
        } else {
            System.out.println("ERROR: Failed to write scanExercise output!!");
        }

    }

    private static void runTests(Map<String, Path> paths) {
        Optional<RunResult> runResult = executor.runTests(paths.get(EXERCISE_PATH));

        if (runResult.isPresent()) {
            JsonWriter.writeObjectIntoJsonFormat(runResult.get(), paths.get(OUTPUT_PATH));
            System.out.println("Test results can be found in " + paths.get(OUTPUT_PATH));
        } else {
            System.out.println("ERROR: Could not output test results!");
        }

    }

    private static void runPrepareStub(Map<String, Path> paths) {
        executor.prepareStub(paths.get(EXERCISE_PATH));
    }

    private static void runPrepareSolution(Map<String, Path> paths) {
        executor.prepareSolution(paths.get(EXERCISE_PATH));
    }

    private static void checkTestPath(Path exercisePath) {
        if (!exercisePath.toFile().isDirectory()) {
            System.out.println("ERROR: Given test path is not a directory.");
            printHelp();
        }
    }

    private static Map<String, Path> parsePaths(String[] args, int pathsCount) {
        Map<String, Path> argsMap = new HashMap<>();

        if(pathsCount == 0) {
            return argsMap;
        }
        argsMap.put(EXERCISE_PATH, Paths.get(args[1]));
        if (pathsCount == 2) {
            argsMap.put(OUTPUT_PATH, Paths.get(args[2]));
        }
        checkTestPath(argsMap.get(EXERCISE_PATH));

        return argsMap;
    }
}
