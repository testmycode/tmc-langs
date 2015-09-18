package fi.helsinki.cs.tmc.langs.rust;

import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.RunResult.Status;
import fi.helsinki.cs.tmc.langs.domain.SpecialLogs;
import fi.helsinki.cs.tmc.langs.domain.TestResult;
import fi.helsinki.cs.tmc.langs.utils.ProcessResult;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses Cargos test output.
 */
public class CargoResultParser {

    private static final RunResult PARSING_FAILED = new RunResult(
            Status.GENERIC_ERROR,
            ImmutableList.<TestResult>of(),
            new ImmutableMap.Builder<String, byte[]>()
            .put("generic_error_message", "Test results couldn't be parsed."
                    .getBytes(StandardCharsets.UTF_8))
            .build());

    //test result: FAILED. 25 passed; 1 failed; 0 ignored; 0 measured
    private static final String PASSED = "(?<passes>\\d*) passed;";
    private static final String FAILED = "(?<fails>\\d*) failed;";
    private static final Pattern RESULT
            = Pattern.compile("test result: .*\\. "
                    + PASSED
                    + " "
                    + FAILED
                    + " \\d* ignored; \\d* measured");
    //test test::dim4::test_4d_1_80_perioditic ... ok
    private static final Pattern TEST = Pattern.compile("test (?<name>.*) \\.\\.\\. .*");
    //thread 'test::dim2::test_2d_1_80_normal' panicked at
    // 'assertion failed: false', src\test\dim2.rs:12
    private static final Pattern FAILURES
            = Pattern.compile(".*thread '(?<name>.*)' panicked at '(?<description>.*)', .*");

    /**
     * Parses given process result to a run result.
     */
    public RunResult parse(ProcessResult processResult) {
        String output = processResult.output;
        String[] lines = output.split("\\r?\\n");

        Matcher matcher = RESULT.matcher(output);
        while (matcher.find()) {
            int fails = Integer.parseInt(matcher.group("fails"));
            int passes = Integer.parseInt(matcher.group("passes"));
            if (fails + passes > 0) {
                Map<String, String> failures = findFailures(processResult.output, fails);
                Status status = fails == 0 ? Status.PASSED : Status.TESTS_FAILED;
                List<String> tests = parseResults(lines);
                return new RunResult(
                        status,
                        buildTestResults(tests, failures),
                        new ImmutableMap.Builder()
                        .put(SpecialLogs.STDOUT,
                                processResult.output.getBytes(StandardCharsets.UTF_8))
                        .put(SpecialLogs.STDERR,
                                processResult.errorOutput.getBytes(StandardCharsets.UTF_8))
                        .build());
            }
        }
        return PARSING_FAILED;
    }

    private List<String> parseResults(String[] lines) {
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            Matcher matcher = TEST.matcher(line);
            if (matcher.matches()) {
                result.add(matcher.group("name"));
            }
        }
        return result;
    }

    private Map<String, String> findFailures(String errorOutput, int fails) {
        Map<String, String> result = new HashMap<>();
        String[] lines = errorOutput.split("\\r?\\n");
        for (String line : lines) {
            Matcher matcher = FAILURES.matcher(line);
            if (matcher.matches()) {
                result.put(matcher.group("name"), matcher.group("description"));
            }
        }
        return result;
    }

    private ImmutableList<TestResult> buildTestResults(List<String> results,
            Map<String, String> failures) {
        ImmutableList.Builder<TestResult> testResults = ImmutableList.builder();
        for (String test : results) {
            String description = failures.get(test);
            boolean pass = false;
            if (description == null) {
                description = "";
                pass = true;
            }
            TestResult testResult = new TestResult(
                    test,
                    pass,
                    ImmutableList.<String>of(),
                    description,
                    ImmutableList.<String>of());
            testResults.add(testResult);
        }
        return testResults.build();
    }
}
