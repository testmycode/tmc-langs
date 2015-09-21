package fi.helsinki.cs.tmc.langs.rust;

import fi.helsinki.cs.tmc.langs.AbstractLanguagePlugin;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
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
import fi.helsinki.cs.tmc.langs.domain.ExerciseBuilder;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.helsinki.cs.tmc.langs.domain.TestDesc;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            log.info("Building for lints with command {0}", Arrays.deepToString(command));
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
            return Optional.of(parseExercisePoints(new Scanner(path.resolve(Constants.TESTS)), exerciseName));
        } catch (IOException e) {
            log.error("Failed to parse test points: {}", e);
            return Optional.absent();
        }
    }
    
    //#[cfg(points = "10")]
    private static final Pattern POINTS = Pattern.compile("\\s*#\\[cfg\\(points = \"(?<points>\\d+)\"\\)\\]\\s*");
    //fn it_shall_work() {
    private static final Pattern TEST = Pattern.compile("\\s*fn (?<name>.+)\\(\\) \\{\\s*");
    //mod test1 {
    private static final Pattern MODULE = Pattern.compile("\\s*mod (?<name>.+) \\{\\s*");
    
    private ExerciseDesc parseExercisePoints(Scanner scanner, String exerciseName) {
        ImmutableList.Builder<TestDesc> tests = ImmutableList.builder();
        String points = "";
        ImmutableList.Builder<String> testsInModule = ImmutableList.builder();
        String module = "";
        String module_points = "";
        int counter = 0;
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if(!module.isEmpty()) {
                char last = ' ';
                for(char c: line.toCharArray()) {
                    if(c == '{' && last != '\\' && last != '\'') {
                        counter++;
                    }else if(c == '}' && last != '\\'  && last != '\'') {
                        counter--;
                    }
                    last = c;
                }
                if (counter == 0) {
                    module = "";
                }
            }
            Matcher matcher = POINTS.matcher(line);
            if(matcher.matches()) {
                points = matcher.group("points");
            } else {
                matcher = TEST.matcher(line);
                if(matcher.matches()) {
                    String name = matcher.group("name");
                    tests.add(new TestDesc(name, ImmutableList.of(points)));
                    if(!module.isEmpty()) {
                        testsInModule.add(points);
                    }
                    points = "";
                } else {
                    matcher = MODULE.matcher(line);
                    if(matcher.matches()) {
                        counter = 1;
                        ImmutableList<String> a = testsInModule.add(module_points).build();
                        if(!a.isEmpty()) {
                            tests.add(new TestDesc(module, a));
                        }
                        testsInModule = ImmutableList.builder();
                        module_points = points;
                        points = "";
                        module = matcher.group("name");
                    }
                }
            }
        }
        return new ExerciseDesc(exerciseName, tests.build());
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
        log.info("Building project with command {0}", Arrays.deepToString(command));
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
        log.info("Running tests with command {0}", Arrays.deepToString(command));
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
            log.error("Running command {0} failed {1}", Arrays.deepToString(command), e);
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
        // no op?
    }
}
