package fi.helsinki.cs.tmc.langs.rust;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.RunResult.Status;
import fi.helsinki.cs.tmc.langs.domain.TestResult;
import fi.helsinki.cs.tmc.langs.utils.ProcessResult;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CargoResultParser {

    private static final RunResult PARSING_FAILED = new RunResult(
            Status.GENERIC_ERROR,
            ImmutableList.<TestResult>of(),
            new ImmutableMap.Builder<String, byte[]>()
            .put("generic_error_message", "Test results couldn't be parsed."
                    .getBytes(StandardCharsets.UTF_8))
            .build());

    //test result: FAILED. 25 passed; 1 failed; 0 ignored; 0 measured
    private static final Pattern RESULT = Pattern.compile("test result: .*\\. (?<passed>\\d*) passed; (?<fails>\\d*) failed; \\d* ignored; \\d* measured");

    public RunResult parse(ProcessResult processResult) {
        String[] lines = processResult.output.split(System.lineSeparator());
        String last = lines[lines.length - 1];
        Matcher matcher = RESULT.matcher(last);
        if (matcher.matches()) {
            int fails = Integer.parseInt(matcher.group("fails"));
            int passes = Integer.parseInt(matcher.group("passes"));
            int total = fails + passes;
            Optional<Map<String, String>> failures
                    = findFailures(processResult.errorOutput, fails);
            if (failures.isPresent()) {
                Status status = fails == 0 ? Status.PASSED : Status.TESTS_FAILED;
                Optional<List<String>> tests = parseResults(lines, total);
                if (tests.isPresent()) {
                    return new RunResult(
                            status,
                            buildTestResults(tests.get(), failures.get()),
                            ImmutableMap.<String, byte[]>of());
                }
            }
        }
        return PARSING_FAILED;
    }

    //test test::dim4::test_4d_1_80_perioditic ... ok
    private static final Pattern TEST = Pattern.compile("test (?<name>.*) \\.\\.\\. .*");

    private Optional<List<String>> parseResults(String[] lines, int tests) {
        List<String> result = new ArrayList<>();
        for (int n = 3; n < 3 + tests; n++) {
            Matcher matcher = TEST.matcher(lines[n]);
            if (matcher.matches()) {
                result.add(matcher.group("name"));
            } else {
                return Optional.absent();
            }
        }
        return Optional.of(result);
    }

    //thread 'test::dim2::test_2d_1_80_normal' panicked at 'assertion failed: false', src\test\dim2.rs:12
    private static final Pattern FAILURES = Pattern.compile("thread '(?<name>.*)' panicked at '(?<description>.*)', .*");

    private Optional<Map<String, String>> findFailures(String errorOutput, int fails) {
        Map<String, String> result = new HashMap<>();
        String[] lines = errorOutput.split(System.lineSeparator());
        for (int n = lines.length - 1; n > lines.length - fails - 1; n--) {
            Matcher matcher = FAILURES.matcher(lines[n]);
            if (matcher.matches()) {
                result.put(matcher.group("name"), matcher.group("description"));
            } else {
                return Optional.absent();
            }
        }
        return Optional.of(result);
    }

    private ImmutableList<TestResult> buildTestResults(List<String> results, Map<String, String> failures) {
        ImmutableList.Builder<TestResult> testResults = ImmutableList.builder();
        for (String test : results) {
            String description = failures.get(test);
            boolean fail = true;
            if (description == null) {
                description = "";
                fail = false;
            }
            TestResult testResult = new TestResult(
                    test,
                    fail,
                    ImmutableList.<String>of(),
                    description,
                    ImmutableList.<String>of());
            testResults.add(testResult);
        }
        return testResults.build();
    }
}
