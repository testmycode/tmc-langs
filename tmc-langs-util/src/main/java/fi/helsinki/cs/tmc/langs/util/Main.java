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

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            printHelp();
        }
        run(args);
        System.exit(0);
    }

    private static void printHelp() {
        System.out.println("Usage: TODO: Write instructions here.");
        System.exit(0);
    }

    private static void run(String[] args) {
        Map<String, Integer> commands = getCommands();
        String command = args[0];
        Integer argsCount = commands.get(command);

        if (argsCount == null) {
            printHelp();
        } else if (!argsCount.equals(args.length - 1)) {
            System.out.println("ERROR: wrong argument count for " + command);
            printHelp();
        }

        Map<String, Path> paths = parsePaths(args);

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
            default:
                printHelp();
                break;
        }
    }

    private static Map<String, Integer> getCommands() {
        //Command name and required argument count
        Map<String, Integer> commands = new HashMap<>();
        commands.put("--help", 0);
        commands.put("--checkstyle", 1);
        commands.put("--scanexercise", 2);
        commands.put("--runtests", 2);
        return commands;
    }

    private static void runCheckCodeStyle(Map<String, Path> paths) {
        Optional<ValidationResult> validationResult = TaskExecutor.runCheckCodeStyle(paths.get("testPath"));

        if (validationResult.isPresent()) {
            JsonWriter.writeCodeStyleReport(validationResult.get(), paths.get("outputPath"));
        }
    }

    private static void runScanExercise(Map<String, Path> paths) {
        // Exercise name, should it be something else than directory name?
        String exerciseName = paths.get("testPath").toFile().getName();
        Optional<ExerciseDesc> exerciseDesc = TaskExecutor.scanExercise(paths.get("testPath"), exerciseName);

        if (exerciseDesc.isPresent()) {
            JsonWriter.writeExerciseDesc(exerciseDesc.get(), paths.get("outputPath"));
        }
    }

    private static void runTests(Map<String, Path> paths) {
        Optional<RunResult> runResult = TaskExecutor.runTests(paths.get("testPath"));

        if (runResult.isPresent()) {
            JsonWriter.writeRunResult(runResult.get(), paths.get("outputPath"));
        }
    }

    private static void checkTestPath(Path testPath) {
        if (!testPath.toFile().isDirectory()) {
            System.out.println("ERROR: Given test path is not a directory.");
            printHelp();
        }
    }

    private static Map<String, Path> parsePaths(String[] args) {
        Map<String, Path> argsMap = new HashMap<>();

        argsMap.put("testPath", Paths.get(args[1]));
        argsMap.put("outputPath", Paths.get(args[2]));

        checkTestPath(argsMap.get("testPath"));

        return argsMap;
    }
}
