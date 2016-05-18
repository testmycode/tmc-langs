package fi.helsinki.cs.tmc.langs.rust;

import fi.helsinki.cs.tmc.langs.AbstractLanguagePlugin;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.ExerciseBuilder;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.RunResult.Status;
import fi.helsinki.cs.tmc.langs.domain.SpecialLogs;
import fi.helsinki.cs.tmc.langs.domain.TestResult;
import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.io.sandbox.StudentFileAwareSubmissionProcessor;
import fi.helsinki.cs.tmc.langs.io.zip.StudentFileAwareUnzipper;
import fi.helsinki.cs.tmc.langs.io.zip.StudentFileAwareZipper;
import fi.helsinki.cs.tmc.langs.rust.util.Constants;
import fi.helsinki.cs.tmc.langs.utils.ProcessResult;
import fi.helsinki.cs.tmc.langs.utils.ProcessRunner;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CargoPlugin extends AbstractLanguagePlugin {

    private static final Logger log = LoggerFactory.getLogger(CargoPlugin.class);
    private static final RunResult EMPTY_FAILURE =
            new RunResult(
                    Status.COMPILE_FAILED,
                    ImmutableList.<TestResult>of(),
                    new ImmutableMap.Builder<String, byte[]>().build());

    /**
     * Creates new plugin for cargo with all default stuff set.
     */
    public CargoPlugin() {
        super(
                new ExerciseBuilder(),
                new StudentFileAwareSubmissionProcessor(),
                new StudentFileAwareZipper(),
                new StudentFileAwareUnzipper());
    }

    @Override
    public boolean isExerciseTypeCorrect(Path path) {
        return Files.isRegularFile(path.resolve(Constants.CARGO_TOML));
    }

    @Override
    protected StudentFilePolicy getStudentFilePolicy(Path projectPath) {
        return new CargoStudentFilePolicy(projectPath);
    }

    @Override
    public ValidationResult checkCodeStyle(Path path, Locale locale) {
        if (run(new String[] {"cargo", "clean"}, path).isPresent()) {
            String[] command = {"cargo", "rustc", "--", "--forbid", "warnings"};
            log.info("Building for lints with command {}", Arrays.deepToString(command));
            Optional<ProcessResult> result = run(command, path);
            if (result.isPresent()) {
                return parseLints(result.get());
            }
        }
        log.error("Build for lints failed.");
        return null;
    }

    public String getLanguageName() {
        return "cargo";
    }

    public String getPluginName() {
        return "cargo";
    }

    @Override
    public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName) {
        if (!isExerciseTypeCorrect(path)) {
            log.error("Failed to scan exercise due to missing Cargo.toml.");
            return Optional.absent();
        }

        try {
            runTests(path);
        } catch (Exception e) {
            log.error("Failed to run tests: {}", e);
            return Optional.absent();
        }
        try {
            Path pointsFile = path.resolve(Constants.POINTS);
            List<String> lines = Files.readAllLines(pointsFile, StandardCharsets.UTF_8);
            return Optional.of(parseExercisePoints(lines, exerciseName));
        } catch (IOException e) {
            log.error("Failed to parse test points: {}", e);
            return Optional.absent();
        }
    }

    @Override
    public RunResult runTests(Path path) {
        Optional<RunResult> result = build(path);
        if (result.isPresent()) {
            log.info("Failed to compile project.");
            return result.get();
        }
        return runBuiltTests(path);
    }

    private Optional<RunResult> build(Path path) {
        String[] command = {"cargo", "test", "--no-run"};
        log.info("Building project with command {}", Arrays.deepToString(command));
        Optional<ProcessResult> result = run(command, path);
        if (result.isPresent()) {
            if (result.get().statusCode == 0) {
                return Optional.absent();
            }
            return Optional.of(filledFailure(result.get()));
        }
        return Optional.of(EMPTY_FAILURE);
    }

    private RunResult runBuiltTests(Path dir) {
        String[] command = {"cargo", "test"};
        log.info("Running tests with command {}", Arrays.deepToString(command));
        Optional<ProcessResult> result = run(command, dir);
        if (result.isPresent()) {
            return parseResult(result.get(), dir);
        }
        return EMPTY_FAILURE;
    }

    private Optional<ProcessResult> run(String[] command, Path dir) {
        ProcessRunner runner = new ProcessRunner(command, dir);
        try {
            return Optional.of(runner.call());
        } catch (Exception e) {
            log.error("Running command {} failed {}", Arrays.deepToString(command), e);
            return Optional.absent();
        }
    }

    private RunResult filledFailure(ProcessResult processResult) {
        byte[] errorOutput = processResult.errorOutput.getBytes(StandardCharsets.UTF_8);
        ImmutableMap<String, byte[]> logs =
                new ImmutableMap.Builder()
                        .put(SpecialLogs.COMPILER_OUTPUT, errorOutput)
                        .<String, byte[]>build();
        return new RunResult(Status.COMPILE_FAILED, ImmutableList.<TestResult>of(), logs);
    }

    private RunResult parseResult(ProcessResult processResult, Path path) {
        return new CargoResultParser().parse(processResult);
    }

    private ValidationResult parseLints(ProcessResult processResult) {
        return new LinterResultParser().parse(processResult);
    }

    @Override
    public void clean(Path path) {
        String[] command = {"cargo", "clean", "&&", "rm", "tmc-points.txt"};
        ProcessRunner runner = new ProcessRunner(command, path);

        try {
            ProcessResult result = runner.call();
            if (result.statusCode == 0) {
                log.info("Cleaned cargo project");
            } else {
                log.warn("Cleaning cargo project was not successful");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ExerciseDesc parseExercisePoints(List<String> lines, String exerciseName) {
        Optional<ExerciseDesc> result = new RustPointsParser().parse(lines, exerciseName);
        if (result.isPresent()) {
            return result.get();
        }
        log.error("Parsing points file failed.");
        return null;
    }
}
