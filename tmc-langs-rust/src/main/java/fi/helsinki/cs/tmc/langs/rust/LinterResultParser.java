package fi.helsinki.cs.tmc.langs.rust;

import fi.helsinki.cs.tmc.langs.abstraction.ValidationError;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.utils.ProcessResult;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinterResultParser {
    //src\lib.rs:3:9:
    private static final String START_INFO
            = "(?<file>.+):(?<startLine>\\d+):(?<startColumn>\\d+):";
    //3:14
    private static final String END_INFO = "(?<endLine>\\d+):(?<endColumn>\\d+)";
    //error: variable does not need to be mutable, #[forbid(unused_mut)] on by default
    private static final Pattern ERROR_START
            = Pattern.compile(START_INFO + " " + END_INFO + " error: (?<description>.*), .*");
    //src\lib.rs:3     let mut x = a * b;
    private static final Pattern ERROR_CONTINUE
            = Pattern.compile("(?<before>(?<file>.+):(?<line>\\d+) )(?<code>.*)");
    //                     ^~~~~
    private static final Pattern ARROW = Pattern.compile("(?<before> *)\\^(?<stem>~*)");
    /**
     * Parses given process result to a validation result.
     */
    public ValidationResult parse(ProcessResult processResult) {
        String output = processResult.errorOutput;
        String[] lines = output.split("\\r?\\n");
        LinterResult result = new LinterResult();
        List<String> code = new ArrayList<>();
        for (String line : lines) {
            Matcher matcher = ERROR_START.matcher(line);
            if (matcher.matches()) {
                code = new ArrayList<>();
                addLintError(matcher, code, result);
            } else {
                matcher = ERROR_CONTINUE.matcher(line);
                if (matcher.matches()) {
                    code.add(line);
                } else {
                    matcher = ARROW.matcher(line);
                    if (!matcher.matches()) {
                        break;
                    }
                }
            }
        }
        return result;
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
