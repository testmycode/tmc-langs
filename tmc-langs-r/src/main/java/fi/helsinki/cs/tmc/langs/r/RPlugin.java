package fi.helsinki.cs.tmc.langs.r;

import fi.helsinki.cs.tmc.langs.AbstractLanguagePlugin;
import fi.helsinki.cs.tmc.langs.abstraction.Strategy;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationError;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.ExerciseBuilder;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.ExercisePackagingConfiguration;
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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class RPlugin extends AbstractLanguagePlugin {

    /**
     * R folder contains the actual R files used in the 
     * project/package. It is automatically included when creating a
     * R package but now when making a regular project in RStudio.
     */
    private static final Path R_FOLDER_PATH = Paths.get("R");

    /** 
     * test/testthat folder contains the unit testing 
     * files which use the testThat library for the R project.
     */
    private static final Path TEST_FOLDER_PATH = Paths.get("tests");
    private static final Path TESTTHAT_FOLDER_PATH = Paths.get("testthat");

    private static final String CANNOT_RUN_TESTS_MESSAGE = "Failed to run tests.";
    private static final String CANNOT_PARSE_TEST_RESULTS_MESSAGE = "Failed to read test results.";
    private static final String CANNOT_SCAN_EXERCISE_MESSAGE = "Failed to scan exercise.";
    private static final String CANNOT_PARSE_EXERCISE_DESCRIPTION_MESSAGE =
            "Failed to parse exercise description.";

    private static Logger log = LoggerFactory.getLogger(RPlugin.class);

    public RPlugin() {
        super(
                new ExerciseBuilder(),
                new StudentFileAwareSubmissionProcessor(),
                new StudentFileAwareZipper(),
                new StudentFileAwareUnzipper());
    }

    /**
     * NOTE: Files.exists does not seem to be able to verify the R and 
     * testthat folder's existence if they are empty.
     */
    @Override
    public boolean isExerciseTypeCorrect(Path path) {
        return Files.exists(path.resolve(R_FOLDER_PATH))
                || Files.exists(path.resolve(TEST_FOLDER_PATH).resolve(TESTTHAT_FOLDER_PATH));
    }

    @Override
    protected StudentFilePolicy getStudentFilePolicy(Path projectPath) {
        return new RStudentFilePolicy(projectPath);
    }

    @Override
    public String getPluginName() {
        return "r";
    }

    @Override
    public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName) {
        ProcessRunner runner = new ProcessRunner(this.getAvailablePointsCommand(), path);

        try {
            ProcessResult result = runner.call();
            if (result.statusCode != 0) {
                log.error(CANNOT_SCAN_EXERCISE_MESSAGE);
                return Optional.absent();
            }
        } catch (Exception e) {
            log.error(CANNOT_SCAN_EXERCISE_MESSAGE, e);
            return Optional.absent();
        }

        try {
            ImmutableList<TestDesc> testDescs = new RExerciseDescParser(path).parse();
            return Optional.of(new ExerciseDesc(exerciseName, testDescs));
        } catch (IOException e) {
            log.error(CANNOT_PARSE_EXERCISE_DESCRIPTION_MESSAGE, e);
        }

        return Optional.absent();
    }

    @Override
    public RunResult runTests(Path path) {
        try {
            ProcessRunner runner = new ProcessRunner(getTestCommand(), path);
            deleteResultsJson(path);
            ProcessResult result = runner.call();
            if (result.statusCode != 0) {
                log.error(CANNOT_RUN_TESTS_MESSAGE);
                return getGenericErrorRunResult(new Exception(CANNOT_RUN_TESTS_MESSAGE));
            }
        } catch (Exception e) {
            log.error(CANNOT_RUN_TESTS_MESSAGE, e);
            return getGenericErrorRunResult(e);
        }

        try {
            return new RTestResultParser(path).parse();
        } catch (IOException e) {
            log.error(CANNOT_PARSE_TEST_RESULTS_MESSAGE, e);
            return getGenericErrorRunResult(e);
        }
    }

    private RunResult getGenericErrorRunResult(Throwable exception) {
        Map<String, byte[]> logMap = new HashMap<>();
        byte[] stackTraceAsByteArray = ExceptionUtils.getStackTrace(exception).getBytes();
        logMap.put(SpecialLogs.GENERIC_ERROR_MESSAGE, stackTraceAsByteArray);
        
        ImmutableMap<String, byte[]> logs = ImmutableMap.copyOf(logMap);
        
        return new RunResult(RunResult.Status.GENERIC_ERROR,
                ImmutableList.copyOf(new ArrayList<TestResult>()), logs);
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

    public String[] getTestCommand() {
        String[] command = new String[]{"Rscript"};
        String[] args;
        if (SystemUtils.IS_OS_WINDOWS) {
            args = new String[]{"-e", "\"library('tmcRtestrunner');run_tests()\""};
        } else {
            args = new String[]{"-e", "library(tmcRtestrunner);run_tests()"};
        }
        return ArrayUtils.addAll(command, args);
    }

    public String[] getAvailablePointsCommand() {
        String[] command = new String[]{"Rscript"};
        String[] args;
        if (SystemUtils.IS_OS_WINDOWS) {
            args = new String[]{"-e", "\"library('tmcRtestrunner');run_available_points()\""};
        } else {
            args = new String[]{"-e", "library(tmcRtestrunner);run_available_points()"};
        }
        return ArrayUtils.addAll(command, args);
    }

    public void deleteResultsJson(Path path) {
        try {
            Files.deleteIfExists(path.resolve(".results.json"));
        } catch (Exception e) {
            log.error("Could not delete .results.json", e);
        }
    }

    /**
     * No operation for now. To be possibly implemented later: remove .Rdata, .Rhistory etc
     */
    @Override
    public void clean(Path path) {
    }

    @Override
    protected ImmutableList<String> getDefaultStudentFilePaths() {
        return ImmutableList.of("R");
    }

    @Override
    protected ImmutableList<String> getDefaultExerciseFilePaths() {
        return ImmutableList.of("tests");
    }
}
