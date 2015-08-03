package fi.helsinki.cs.tmc.langs.python3;

import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.TestResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Python3TestResultParser {
    private static Path RESULT_FILE = Paths.get(".tmc_test_results.json");

    private Path path;

    public Python3TestResultParser(Path path) {
        this.path = path;
    }

    /**
     * Parses the test results from the result file.
     * @return Test run results.
     */
    public RunResult result() throws IOException {
        boolean allPassed = true;
        ArrayList<TestResult> testResults = new ArrayList<>();

        for (Map<String, Object> details : getDetails()) {
            TestResult result = createTestResult(details);
            if (!result.passed) {
                allPassed = false;
            }
            testResults.add(result);
        }
        RunResult.Status status =
                allPassed ? RunResult.Status.PASSED : RunResult.Status.TESTS_FAILED;
        ImmutableList<TestResult> immutableResults = ImmutableList.copyOf(testResults);
        ImmutableMap<String, byte[]> logs = ImmutableMap.copyOf(new HashMap<String, byte[]>());
        return new RunResult(status, immutableResults, logs);
    }

    private List<Map<String, Object>> getDetails() throws IOException {
        byte[] json = Files.readAllBytes(path.resolve(RESULT_FILE));
        ObjectMapper mapper = new ObjectMapper();

        return (List<Map<String, Object>>) mapper.readValue(json, List.class);
    }

    private TestResult createTestResult(Map<String, Object> details) {
        String status = (String) details.get("status");
        String message = (String) details.get("message");
        String name = (String) details.get("name");
        List<String> points = (List<String>) details.get("points");
        List<String> backtrace = (List<String>) details.get("backtrace");
        boolean passed = status.equals("passed");
        ImmutableList<String> immutableBacktrace = ImmutableList.copyOf(backtrace);
        ImmutableList<String> immutablePoints = ImmutableList.copyOf(points);
        return new TestResult(name, passed, immutablePoints, message, immutableBacktrace);
    }
}
