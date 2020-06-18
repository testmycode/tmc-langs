package fi.helsinki.cs.tmc.langs.python3;

import fi.helsinki.cs.tmc.langs.AbstractLanguagePlugin;
import fi.helsinki.cs.tmc.langs.abstraction.Strategy;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationError;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.ExerciseBuilder;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class Python3Plugin extends AbstractLanguagePlugin {

    private static final Path SETUP_PY_PATH = Paths.get("setup.py");
    private static final Path REQUIREMENTS_TXT_PATH = Paths.get("requirements.txt");
    private static final Path TEST_FOLDER_PATH = Paths.get("test");
    private static final Path INIT_PY_PATH = Paths.get("__init__.py");
    private static final Path TMC_TEST_LIBRARY_PATH = Paths.get("tmc");
    private static final Path MAIN_PY_PATH = Paths.get("__main__.py");

    private static final String CANNOT_CLEAN_FILES_MESSAGE = "Failed to clean files.";
    private static final String CANNOT_RUN_TESTS_MESSAGE = "Failed to run tests.";
    private static final String CANNOT_PARSE_TEST_RESULTS_MESSAGE = "Failed to read test results.";
    private static final String CANNOT_SCAN_EXERCISE_MESSAGE = "Failed to scan exercise.";
    private static final String CANNOT_PARSE_EXERCISE_DESCRIPTION_MESSAGE =
            "Failed to parse exercise description.";

    private static Logger log = LoggerFactory.getLogger(Python3Plugin.class);

    /**
     * Instantiates a new Python3Plugin.
     */
    public Python3Plugin() {
        super(
                new ExerciseBuilder(),
                new StudentFileAwareSubmissionProcessor(),
                new StudentFileAwareZipper(),
                new StudentFileAwareUnzipper());
    }

    @Override
    public boolean isExerciseTypeCorrect(Path path) {
        return Files.exists(path.resolve(SETUP_PY_PATH))
                || Files.exists(path.resolve(REQUIREMENTS_TXT_PATH))
                || Files.exists(path.resolve(TEST_FOLDER_PATH).resolve(INIT_PY_PATH))
                || Files.exists(path.resolve(TMC_TEST_LIBRARY_PATH).resolve(MAIN_PY_PATH));
    }

    @Override
    protected StudentFilePolicy getStudentFilePolicy(Path projectPath) {
        return new Python3StudentFilePolicy(projectPath);
    }

    @Override
    public String getPluginName() {
        return "python3";
    }

    @Override
    public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName) {

        ProcessRunner runner = new ProcessRunner(getAvailablePointsCommand(), path);
        try {
            runner.call();
        } catch (Exception e) {
            log.error(CANNOT_SCAN_EXERCISE_MESSAGE, e);
        }

        try {
            ImmutableList<TestDesc> testDescs = new Python3ExerciseDescParser(path).parse();
            return Optional.of(new ExerciseDesc(exerciseName, testDescs));
        } catch (IOException e) {
            log.error(CANNOT_PARSE_EXERCISE_DESCRIPTION_MESSAGE, e);
        }

        return Optional.absent();
    }

    @Override
    public RunResult runTests(Path path) {

        ProcessRunner runner = new ProcessRunner(getTestCommand(), path);
        try {
            // TODO: handle non successful return codes
            ProcessResult result = runner.call();
            if (result.timedOut) {
                return new RunResult(RunResult.Status.TESTS_FAILED, ImmutableList.copyOf(
                        Arrays.asList(new TestResult("Timeout test", false,
                                "Tests timed out. "
                                        + "Make sure you don't have an infinite loop in your code."
                        ))),
                        ImmutableMap.of());
            }
        } catch (Exception e) {
            log.error(CANNOT_RUN_TESTS_MESSAGE, e);
        }

        try {
            return new Python3TestResultParser(path).parse();
        } catch (IOException e) {
            log.error(CANNOT_PARSE_TEST_RESULTS_MESSAGE, e);
        }
        return null;
    }

    @Override
    public ValidationResult checkCodeStyle(Path path, Locale locale) {
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

    private String[] getAvailablePointsCommand() {
        return ArrayUtils.add(getTestCommand(), "available_points");
    }

    private String[] getTestCommand() {
        String[] command = null;
        if (SystemUtils.IS_OS_WINDOWS) {
            command = new String[] {"py", "-3"};
            // Conda only works with the "python"-command on windows
            String condaPython = System.getenv("CONDA_PYTHON_EXE");
            if (condaPython != null && !condaPython.isEmpty()
                    && Files.exists(Paths.get(condaPython))) {
                command = new String[] {condaPython};
            }
        } else {
            command = new String[] {"python3"};
        }
        return ArrayUtils.addAll(command, "-m", "tmc");
    }

    @Override
    public void clean(Path path) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) 
                throws IOException {
                    if (file.endsWith(".available_points.json")
                            || file.endsWith(".tmc_test_results.json")
                            || file.toString().contains("__pycache__")) {
                        Files.delete(file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) 
                throws IOException {
                    if (dir.endsWith("__pycache__")) {
                        Files.delete(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.error(CANNOT_CLEAN_FILES_MESSAGE, e);
        }
    }

    @Override
    protected ImmutableList<String> getDefaultStudentFilePaths() {
        return ImmutableList.of("src");
    }

    @Override
    protected ImmutableList<String> getDefaultExerciseFilePaths() {
        return ImmutableList.of("test", "tmc");
    }
}
