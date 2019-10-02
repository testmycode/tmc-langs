package fi.helsinki.cs.tmc.langs.rust;

import fi.helsinki.cs.tmc.langs.abstraction.ValidationError;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.utils.ProcessResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LinterResultParser {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    
    /**
     * Parses given process result to a validation result.
     */
    public ValidationResult parse(ProcessResult processResult) {
        String output = processResult.errorOutput;
        String[] lines = output.split("\\r?\\n");
        List<LintError> errors = Arrays.stream(lines)
            .map((line) -> mapper.readTree(line))
            .filter((parseTree) -> parseTree.get("reason").asText().equals("compiler-message"))
            .flatMap((parseTree) -> {
                for (JsonNode node : parseTree.get("message").get("spans")) {
                    if (node.get("is_primary").asBoolean()) {
                        return Stream.of(createLintError(node, parseTree.get("rendered").asText()));
                    }
                }
                return Stream.empty();
            })
            .collect(Collectors.toList());
        
        
        // LinterResult result = new LinterResult();
        // List<String> code = new ArrayList<>();
        // for (String line : lines) {
        //     Matcher matcher = ERROR_START.matcher(line);
        //     if (matcher.matches()) {
        //         code = new ArrayList<>();
        //         addLintError(matcher, code, result);
        //     } else {
        //         matcher = ERROR_CONTINUE.matcher(line);
        //         if (matcher.matches()) {
        //             code.add(line);
        //         } else {
        //             matcher = ARROW.matcher(line);
        //             if (!matcher.matches()) {
        //                 break;
        //             }
        //         }
        //     }
        // }
        return result;
    }

    private LintError createLintError(JsonNode node, String message) {
        String file = node.get(fieldName)
        return new LintError(file, message, code, startLine, startColumn, endLine, endColumn);
    }

    private void addLintError(Matcher matcher, List<String> code, LinterResult result) {
        String fileName = matcher.group("file");
        LintError current = new LintError(
                fileName,
                matcher.group("description"),
                code,
                Integer.parseInt(matcher.group("startLine")),
                Integer.parseInt(matcher.group("startColumn")),
                Integer.parseInt(matcher.group("endLine")),
                Integer.parseInt(matcher.group("endColumn")));
        File file = Paths.get(fileName).toFile();
        List<ValidationError> errors = result.getValidationErrors().get(file);
        if (errors == null) {
            errors = new ArrayList<>();
            result.getValidationErrors().put(file, errors);
        }
        errors.add(current);
    }
}
