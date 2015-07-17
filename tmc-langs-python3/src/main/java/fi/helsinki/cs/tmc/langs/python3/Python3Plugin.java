package fi.helsinki.cs.tmc.langs.python3;

import com.google.common.collect.ImmutableList;
import fi.helsinki.cs.tmc.langs.AbstractLanguagePlugin;
import fi.helsinki.cs.tmc.langs.domain.ExerciseBuilder;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.TestDesc;
import fi.helsinki.cs.tmc.langs.io.EverythingIsStudentFileStudentFilePolicy;
import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.io.sandbox.StudentFileAwareSubmissionProcessor;
import fi.helsinki.cs.tmc.langs.io.zip.StudentFileAwareUnzipper;
import fi.helsinki.cs.tmc.langs.io.zip.StudentFileAwareZipper;
import fi.helsinki.cs.tmc.langs.utils.ProcessRunner;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;

import com.google.common.base.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Python3Plugin extends AbstractLanguagePlugin {

    private static final Path SETUP_PY_PATH = Paths.get("setup.py");
    private static final Path REQUIREMENTS_TXT_PATH = Paths.get("requirements.txt");
    private static final Path TEST_FOLDER_PATH = Paths.get("test");

    private static final String CANNOT_RUN_TESTS_MESSAGE = "Failed to run tests.";

    private static Logger log = LoggerFactory.getLogger(Python3Plugin.class);

    /**
     * Instantiates a new Python3Plugin.
     */
    public Python3Plugin() {
        super(new ExerciseBuilder(),
                new StudentFileAwareSubmissionProcessor(),
                new StudentFileAwareZipper(),
                new StudentFileAwareUnzipper());
    }

    @Override
    protected boolean isExerciseTypeCorrect(Path path) {
        return Files.exists(path.resolve(SETUP_PY_PATH)) || Files.exists(path.resolve(REQUIREMENTS_TXT_PATH));
    }

    @Override
    protected StudentFilePolicy getStudentFilePolicy(Path projectPath) {
        return new Python3StudentFilePolicy(projectPath);
    }

    @Override
    public String getLanguageName() {
        return "python3";
    }

    @Override
    public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName) {
        String[] command = {"python3", "-m", "tmc", "available_points"};
        Path testFolder = path.resolve(TEST_FOLDER_PATH);
        ProcessRunner runner = new ProcessRunner(command, testFolder);
        try {
            runner.call();
            ImmutableList<TestDesc> testDescs = new Python3ExerciseDescParser(testFolder).parse();
            return Optional.of(new ExerciseDesc(exerciseName, testDescs));
        } catch (Exception e) {
            log.error(e.toString());
        }
        return Optional.absent();
    }

    @Override
    public RunResult runTests(Path path) {
        String[] command = {"python3", "-m", "tmc"};
        Path testFolder = path.resolve(TEST_FOLDER_PATH);
        ProcessRunner runner = new ProcessRunner(command, testFolder);
        try {
            runner.call();
        } catch (Exception e) {
            log.error(e.toString());
            throw new RuntimeException(CANNOT_RUN_TESTS_MESSAGE);
        }
        try {
            return new Python3TestResultParser(testFolder).result();
        } catch (IOException e) {
            log.error(e.toString());
        }
        return null;
    }

    @Override
    public ValidationResult checkCodeStyle(Path path) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
