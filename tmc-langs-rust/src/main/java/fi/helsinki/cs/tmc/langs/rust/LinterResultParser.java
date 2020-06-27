package fi.helsinki.cs.tmc.langs.rust;

import fi.helsinki.cs.tmc.langs.abstraction.ValidationError;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.utils.ProcessResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LinterResultParser {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Parses given process result to a validation result.
     */
    public ValidationResult parse(ProcessResult processResult) {
        String output = processResult.output;
        String[] lines = output.split("\\r?\\n");
        return new LinterResult(Arrays
            .stream(lines)
            .filter(line -> line.startsWith("{"))
            .flatMap(line -> {
                try {
                    return Stream.of(mapper.readTree(line));
                } catch (IOException e) {
                    // TODO: When does IOException happen? Or does it?
                    throw new RuntimeException(e);
                }
            })
            .filter(parseTree -> parseTree
                .get("reason")
                .asText()
                .equals("compiler-message")
            )
            .map(parseTree -> parseTree.get("message"))
            .flatMap(parseTree -> {
                for (JsonNode node : parseTree.get("spans")) {
                    if (node.get("is_primary").asBoolean()) {
                        return Stream.of(createLintError(
                            node,
                            parseTree.get("rendered").asText()
                        ));
                    }
                }
                return Stream.empty();
            })
            .collect(Collectors.groupingBy(error ->
                Paths.get(error.getSourceName()).toFile()
            ))
        );
    }

    private ValidationError createLintError(JsonNode node, String message) {
        return new LintError(
            node.get("file_name").asText(),
            message,
            node.get("line_start").asInt(),
            node.get("column_start").asInt()
        );
    }
}
