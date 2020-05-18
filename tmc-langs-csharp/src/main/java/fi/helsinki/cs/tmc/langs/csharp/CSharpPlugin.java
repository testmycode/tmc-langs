package fi.helsinki.cs.tmc.langs.csharp;

import fi.helsinki.cs.tmc.langs.AbstractLanguagePlugin;
import fi.helsinki.cs.tmc.langs.abstraction.Strategy;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationError;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.ExerciseBuilder;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.SpecialLogs;
import fi.helsinki.cs.tmc.langs.domain.TestDesc;
import fi.helsinki.cs.tmc.langs.domain.TestResult;
import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.io.sandbox.StudentFileAwareSubmissionProcessor;
import fi.helsinki.cs.tmc.langs.io.zip.StudentFileAwareUnzipper;
import fi.helsinki.cs.tmc.langs.io.zip.StudentFileAwareZipper;
import fi.helsinki.cs.tmc.langs.utils.ProcessResult;
import fi.helsinki.cs.tmc.langs.utils.ProcessRunner;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;


public class CSharpPlugin extends AbstractLanguagePlugin {

    private static final Path SRC_PATH = Paths.get("src");

    private static final String CANNOT_RUN_TESTS_MESSAGE = "Failed to run tests.";
    private static final String CANNOT_PARSE_TEST_RESULTS_MESSAGE = "Failed to read test results.";
    private static final String CANNOT_SCAN_EXERCISE_MESSAGE = "Failed to scan exercise.";
    private static final String CANNOT_PARSE_EXERCISE_DESCRIPTION_MESSAGE =
            "Failed to parse exercise description.";
    private static final String CANNOT_LOCATE_RUNNER_MESSAGE = "Failed to locate runner.";
    private static final String CANNOT_PURGE_OLD_RESULTS_MESSAGE =
            "Failed to purge old test results.";
    private static final String CANNOT_SCAN_PROJECT_TYPE_MESSAGE = 
            "Failed to scan project files.";
    private static final String COMPILATION_FAILED_MESSAGE = "Failed to compile excercise.";

    private static Logger log = LoggerFactory.getLogger(CSharpPlugin.class);

    public CSharpPlugin() {
        super(
                new ExerciseBuilder(),
                new StudentFileAwareSubmissionProcessor(),
                new StudentFileAwareZipper(),
                new StudentFileAwareUnzipper()
        );
    }

    @Override
    public boolean isExerciseTypeCorrect(Path path) {
        return doesProjectContainCSharpFiles(path);
    }

    @Override
    protected StudentFilePolicy getStudentFilePolicy(Path projectPath) {
        return new CSharpStudentFilePolicy(projectPath);
    }

    @Override
    public String getPluginName() {
        return "csharp";
    }

    @Override
    public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName) {
        ProcessRunner runner = new ProcessRunner(getAvailablePointsCommand(), path);

        try {
            ProcessResult result = runner.call();
            
            if (result.statusCode != 0) {
                log.error(COMPILATION_FAILED_MESSAGE);
                return Optional.absent();
            }
        } catch (Exception e) {
            log.error(CANNOT_SCAN_EXERCISE_MESSAGE, e);
            return Optional.absent();
        }

        try {
            ImmutableList<TestDesc> testDescs = new CSharpExerciseDescParser(path).parse();
            return Optional.of(new ExerciseDesc(exerciseName, testDescs));
        } catch (IOException e) {
            log.error(CANNOT_PARSE_EXERCISE_DESCRIPTION_MESSAGE, e);
        }

        return Optional.absent();
    }

    @Override
    public RunResult runTests(Path path) {
        deleteOldResults(path);

        ProcessRunner runner = new ProcessRunner(getTestCommand(), path);

        try {
            ProcessResult result = runner.call();

            if (result.statusCode != 0) {
                log.error(COMPILATION_FAILED_MESSAGE);
                return runResultFromFailedCompilation(result);
            }
        } catch (Exception e) {
            log.error(CANNOT_RUN_TESTS_MESSAGE, e);
            return null;
        }

        try {
            return new CSharpTestResultParser(path).parse();
        } catch (IOException e) {
            log.error(CANNOT_PARSE_TEST_RESULTS_MESSAGE, e);
        }

        return null;
    }

    @Override
    public ValidationResult checkCodeStyle(Path path, Locale messageLocale) {
        return new ValidationResult() {
            @Override
            public Strategy getStrategy() {
                return Strategy.DISABLED;
            }

            @Override
            public Map<File, List<ValidationError>> getValidationErrors() {
                return Maps.newHashMap();
            }
        };
    }

    @Override
    public void clean(Path path) {
    }

    private void deleteOldResults(Path path) {
        try {
            Files.deleteIfExists(path.resolve(".tmc_test_results.json"));
        } catch (Exception e) {
            log.error(CANNOT_PURGE_OLD_RESULTS_MESSAGE, e);
        }
    }

    private String[] getAvailablePointsCommand() {
        return new String[]{"dotnet", getBootstrapPath(), "-p"};
    }

    private String[] getTestCommand() {
        return new String[]{"dotnet", getBootstrapPath(), "-t"};
    }

    private String getBootstrapPath() {
        String envVarPath = System.getenv("TMC_CSHARP_BOOTSTRAP_PATH");
        if (envVarPath != null) {
            return envVarPath;
        }

        try {
            Scanner in = new Scanner(new FileReader("tmc-langs-csharp/bootstrapPath.txt"));
            return in.nextLine();
        } catch (Exception e) {
            log.error(CANNOT_LOCATE_RUNNER_MESSAGE, e);
            return null;
        }
    }

    private boolean doesProjectContainCSharpFiles(Path path) {
        PathMatcher matcher = FileSystems.getDefault()
                .getPathMatcher("glob:**.csproj");

        try {
            if (Files.exists(path.resolve(SRC_PATH))) {
                return Files.walk(path.resolve(SRC_PATH), 2).anyMatch(p -> matcher.matches(p));
            }
        } catch (Exception e) {
            log.error(CANNOT_SCAN_PROJECT_TYPE_MESSAGE, e);
        }

        return false;
    }

    private RunResult runResultFromFailedCompilation(ProcessResult result) {
        Map<String, byte[]> logs = new HashMap<>();
        logs.put(SpecialLogs.STDOUT, result.output.getBytes());
        logs.put(SpecialLogs.STDERR, result.errorOutput.getBytes());

        return new RunResult(
                RunResult.Status.COMPILE_FAILED,
                ImmutableList.copyOf(new ArrayList<TestResult>()),
                ImmutableMap.copyOf(logs));
    }
}
