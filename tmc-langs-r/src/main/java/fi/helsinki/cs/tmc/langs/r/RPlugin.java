package fi.helsinki.cs.tmc.langs.r;

import fi.helsinki.cs.tmc.langs.AbstractLanguagePlugin;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.ExerciseBuilder;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.io.sandbox.StudentFileAwareSubmissionProcessor;
import fi.helsinki.cs.tmc.langs.io.sandbox.SubmissionProcessor;
import fi.helsinki.cs.tmc.langs.io.zip.StudentFileAwareUnzipper;
import fi.helsinki.cs.tmc.langs.io.zip.StudentFileAwareZipper;
import fi.helsinki.cs.tmc.langs.io.zip.Unzipper;
import fi.helsinki.cs.tmc.langs.io.zip.Zipper;
import fi.helsinki.cs.tmc.langs.python3.Python3TestResultParser;
import fi.helsinki.cs.tmc.langs.utils.ProcessRunner;

import com.google.common.base.Optional;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SystemUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

public final class RPlugin extends AbstractLanguagePlugin {

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

    @Override
    public boolean isExerciseTypeCorrect(Path path) {
        return false;
    }

    @Override
    protected StudentFilePolicy getStudentFilePolicy(Path projectPath) {
        return null;
    }

    @Override
    public String getPluginName() {
        return null;
    }

    @Override
    public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName) {
        return null;
    }

    @Override
    public RunResult runTests(Path path) {

        ProcessRunner runner = new ProcessRunner(getTestCommand(), path);
        try {
            runner.call();
        } catch (Exception e) {
            log.error(CANNOT_RUN_TESTS_MESSAGE, e);
        }

        try {
            return new RTestResultParser(path).parse();
        } catch (IOException e) {
            log.error(CANNOT_PARSE_TEST_RESULTS_MESSAGE, e);
        }
        return null;
    }

    @Override
    public ValidationResult checkCodeStyle(Path path, Locale messageLocale) {
        return null;
    }

    private String[] getTestCommand() {
        String[] rscr = new String[] {"Rscript", "-e"};
        String[] command;
        if (SystemUtils.IS_OS_WINDOWS) {
            command = new String[] {"\"library('tmcRtestrunner');runTestsWithDefault(TRUE)\""};
        } else {
            command = new String[] {"\"library(tmcRtestrunner);runTests(\"$PWD\", print=TRUE)\""};
        }
        return ArrayUtils.addAll(rscr, command);
    }

    @Override
    public void clean(Path path) {

    }
}
