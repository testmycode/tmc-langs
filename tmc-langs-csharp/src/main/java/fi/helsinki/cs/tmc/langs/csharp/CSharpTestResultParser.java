package fi.helsinki.cs.tmc.langs.csharp;

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

public class CSharpTestResultParser {
    private static Path RESULT_FILE = Paths.get(".tmc_test_results.json");

    public static RunResult parse(Path path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<TestResult> testResults = getResults(path, mapper);
        
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
    
    private static List<TestResult> getResults(Path path, ObjectMapper mapper) throws IOException {
        byte[] json = Files.readAllBytes(path.resolve(RESULT_FILE));
        List<TestResult> results = new ArrayList<>();
        
        JsonNode tree = mapper.readTree(json);
        for (JsonNode node : tree) {
            results.add(toTestResult(node));
        }

        return results;
    }

    private static TestResult toTestResult(JsonNode node) {
        List<String> points = new ArrayList<>();
        for (JsonNode point : node.get("Points")) {
            points.add(point.asText());
        }

        List<String> backTrace = new ArrayList<>();
        for (JsonNode line : node.get("ErrorStackTrace")) {
            backTrace.add(line.asText());
        }

        return new TestResult(
                node.get("Name").asText(),
                node.get("Passed").asBoolean(),
                ImmutableList.copyOf(points),
                node.get("Message").asText(),
                ImmutableList.copyOf(backTrace));
    }
    
}
