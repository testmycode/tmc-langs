package fi.helsinki.cs.tmc.langs.rust;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.helsinki.cs.tmc.langs.AbstractLanguagePlugin;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.Configuration;
import fi.helsinki.cs.tmc.langs.domain.ExerciseBuilder;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.RunResult.Status;
import fi.helsinki.cs.tmc.langs.domain.SpecialLogs;
import fi.helsinki.cs.tmc.langs.domain.TestResult;
import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.io.sandbox.StudentFileAwareSubmissionProcessor;
import fi.helsinki.cs.tmc.langs.io.sandbox.SubmissionProcessor;
import fi.helsinki.cs.tmc.langs.io.zip.StudentFileAwareUnzipper;
import fi.helsinki.cs.tmc.langs.io.zip.StudentFileAwareZipper;
import fi.helsinki.cs.tmc.langs.io.zip.Unzipper;
import fi.helsinki.cs.tmc.langs.io.zip.Zipper;
import fi.helsinki.cs.tmc.langs.rust.util.Constants;
import fi.helsinki.cs.tmc.langs.utils.ProcessResult;
import fi.helsinki.cs.tmc.langs.utils.ProcessRunner;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.concurrent.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CargoPlugin extends AbstractLanguagePlugin {

    private static final Logger log = LoggerFactory.getLogger(CargoPlugin.class);
    private static final RunResult EMPTY_FAILURE = new RunResult(
            Status.COMPILE_FAILED,
            ImmutableList.<TestResult>of(),
            new ImmutableMap.Builder<String, byte[]>().build());

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
    public ValidationResult checkCodeStyle(Path path) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getLanguageName() {
        return "cargo";
    }

    @Override
    public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName) {
        throw new UnsupportedOperationException("Not supported yet.");
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

    private Optional<RunResult> build(Path dir) {
        String[] command = {"cargo", "test", "--no-run"};
        log.info("Building project with command {0}", Arrays.deepToString(command));
        Optional<ProcessResult> result = run(command, dir);
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
        log.info("Running tests with command {0}", Arrays.deepToString(command));
        Optional<ProcessResult> result = run(command, dir);
        if (result.isPresent()) {
            if (result.get().statusCode == 0) {
                return parseResult(result.get(), dir);
            }
            return filledFailure(result.get());
        }
        return EMPTY_FAILURE;
    }

    private Optional<ProcessResult> run(String[] command, Path dir) {
        ProcessRunner runner = new ProcessRunner(command, dir);
        try {
            return Optional.of(runner.call());
        } catch (Exception e) {
            return Optional.absent();
        }
    }

    private RunResult filledFailure(ProcessResult processResult) {
        byte[] output = processResult.output.getBytes(StandardCharsets.UTF_8);
        byte[] errorOutput = processResult.errorOutput.getBytes(StandardCharsets.UTF_8);
        ImmutableMap.Builder<String, byte[]> logs = new ImmutableMap.Builder<>();
        logs.put(SpecialLogs.STDOUT, output);
        logs.put(SpecialLogs.STDERR, errorOutput);
        return new RunResult(
                Status.COMPILE_FAILED,
                ImmutableList.<TestResult>of(),
                logs.build());
    }

    private RunResult parseResult(ProcessResult processResult, Path path) {
        Configuration configuration = new Configuration(path);
        return new CargoResultParser().parse(processResult);
    }

}
