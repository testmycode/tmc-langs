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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public final class RPlugin extends AbstractLanguagePlugin {

    // Various static final Path-variables for filepaths
    // to various folders and files in a R exercise project here
    private static final Path R_FOLDER_PATH = Paths.get("R");
    private static final Path TEST_FOLDER_PATH = Paths.get("tests");
    private static final Path TESTTHAT_FOLDER_PATH = Paths.get("testthat");
    private static final Path TMC_FOLDER_PATH = Paths.get("tmc");
    private static final Path DESCRIPTION_PATH = Paths.get("DESCRIPTION");
    private static final Path RHISTORY_PATH = Paths.get(".RHistory");
    private static final Path RESULT_R_PATH = Paths.get("result.R");

    private static final String CANNOT_RUN_TESTS_MESSAGE = "Failed to run tests.";
    private static final String CANNOT_PARSE_TEST_RESULTS_MESSAGE = "Failed to read test results.";
    private static final String CANNOT_SCAN_EXERCISE_MESSAGE = "Failed to scan exercise.";
    private static final String CANNOT_PARSE_EXERCISE_DESCRIPTION_MESSAGE =
            "Failed to parse exercise description.";

    // Various static final String-variables for
    // error messages related to parsing and running R tests here

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
        return Files.exists(path.resolve(R_FOLDER_PATH))
                || Files.exists(path.resolve(TEST_FOLDER_PATH).resolve(TESTTHAT_FOLDER_PATH))
                || Files.exists(path.resolve(DESCRIPTION_PATH))
                || Files.exists(path.resolve(RHISTORY_PATH))
                || Files.exists(path.resolve(TMC_FOLDER_PATH).resolve(RESULT_R_PATH));
        /*
        R folder contains the actual R files used in the
        project/package. It is automatically included when creating a
        R package but now when making a regular project in RStudio.

        test/testthat folder contains the unit testing
        files which use the testThat library for the R project.

        DESCRIPTION file contains package information.
        Included automatically when making a new package, but not
        included when making a regular project in RStudio.

        .RHistory file contains the history of executed code on
        the R terminal. Generated after running code on the R
        terminal for the first time.

        tmc/result.R contains the call to tmcRtestrunner's runTests function.
         */
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
        // TO DO
    }
}
