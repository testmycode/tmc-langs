package fi.helsinki.cs.tmc.langs.r;

import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.TestResult;

import com.fasterxml.jackson.databind.JsonNode;
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

public class RTestResultParser {

    Path path;
    private ObjectMapper mapper;

    private static Path RESULT_FILE = Paths.get(".results.json");

    public RTestResultParser(Path path) {
        this.path = path;
        this.mapper = new ObjectMapper();
    }

    RunResult parse() throws IOException {
        List<TestResult> testResults = getTestResults();

        RunResult.Status status = RunResult.Status.PASSED;
        for (TestResult result : testResults) {
            if (!result.isSuccessful()) {
                status = RunResult.Status.TESTS_FAILED;
            }
        }

        ImmutableList<TestResult> immutableResults = ImmutableList.copyOf(testResults);
        ImmutableMap<String, byte[]> logs = ImmutableMap.copyOf(new HashMap<String, byte[]>());
        return new RunResult(status, immutableResults, logs);
    }

    private List<TestResult> getTestResults() throws IOException {
        byte[] json = Files.readAllBytes(path.resolve(RESULT_FILE));
        
        List<TestResult> results = new ArrayList<>();

        JsonNode tree = mapper.readTree(json).get("testResults");
        for (JsonNode node : tree) {
            results.add(toTestResult(node));
        }

        return results;
    }
    
    private TestResult toTestResult(JsonNode node) {
        List<String> points = new ArrayList<>();
        for (JsonNode point : node.get("points")) {
            points.add(point.asText());
        }

        List<String> backTrace = new ArrayList<>();
        for (JsonNode line : node.get("backtrace")) {
            backTrace.add(line.asText());
        }
        
        boolean passed = node.get("status").asText().equals("pass");

        return new TestResult(
                node.get("name").asText(),
                passed,
                ImmutableList.copyOf(points),
                node.get("message").toString(),
                ImmutableList.copyOf(backTrace));
    }
}
