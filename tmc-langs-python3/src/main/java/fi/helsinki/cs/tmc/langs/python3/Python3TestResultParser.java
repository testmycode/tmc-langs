package fi.helsinki.cs.tmc.langs.python3;

import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.TestResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class Python3TestResultParser {
    private static Path RESULT_FILE = Paths.get(".tmc_test_results.json");

    private static Logger log = LoggerFactory.getLogger(Python3TestResultParser.class);

    private Path path;
    private ObjectMapper mapper;

    public Python3TestResultParser(Path path) {
        this.path = path;
        this.mapper = new ObjectMapper();
    }

    /**
     * Parses the test results from the result file.
     *
     * @return Test run results.
     */
    public RunResult parse() throws IOException {
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
        String json;
        try {
            json = String.join("", Files.readAllLines(
                path.resolve(RESULT_FILE), StandardCharsets.UTF_8));
        } catch (IOException e) {
            try {
                json = String.join("", Files.readAllLines(
                path.resolve(RESULT_FILE), Charset.defaultCharset()));
            } catch (IOException ex) {
                log.error("Failed to read test results with both UTF-8 and "
                        + Charset.defaultCharset() + " encodings.", ex);
                throw ex;
            }
        }
        List<TestResult> results = new ArrayList<>();

        JsonNode tree = mapper.readTree(json);
        for (JsonNode node : tree) {
            results.add(toTestResult(node));
        }

        return results;
    }

    private String parseTestName(String testName) {
        String[] parts = testName.split("\\.");
        if (parts.length == 4) {
            return parts[2] + ": " + parts[3];
        }
        return testName;
    }

    private String parseTestMessage(String testMessage) {
        String matcher = testMessage.toLowerCase();
        if (matcher.matches("^(true|false) is not (true|false) :[\\s\\S]*")) {
            return testMessage.split(":", 2)[1].trim();
        }
        return testMessage;
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

        return new TestResult(
                parseTestName(node.get("name").asText()),
                node.get("passed").asBoolean(),
                ImmutableList.copyOf(points),
                parseTestMessage(node.get("message").asText()),
                ImmutableList.copyOf(backTrace));
    }
}
