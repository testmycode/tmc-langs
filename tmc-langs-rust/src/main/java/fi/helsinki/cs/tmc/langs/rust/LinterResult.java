package fi.helsinki.cs.tmc.langs.rust;

import fi.helsinki.cs.tmc.langs.abstraction.Strategy;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationError;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.utils.ProcessResult;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinterResult implements ValidationResult {

    //src\lib.rs:3:9: 3:14 error: variable does not need to be mutable, #[forbid(unused_mut)] on by default
    private static final Pattern ERROR_START
            = Pattern.compile("(?<file>.+):(?<startLine>\\d+):(?<startColumn>\\d+): (?<endLine>\\d+):(?<endColumn>\\d+) error: (?<description>.*), #[forbid\\(.*\\)] on by default");
    //src\lib.rs:3     let mut x = a * b;
    private static final Pattern ERROR_CONTINUE
            = Pattern.compile("(?<before>(?<file>.+):(?<line>\\d+) )(?<code>.*)");
    //                     ^~~~~
    private static final Pattern ARROW
            = Pattern.compile("(?<before> *)^(?<stem>~*)");

    private Strategy strategy;
    private final Map<File, List<ValidationError>> errors;

    private LinterResult() {
        errors = new HashMap<>();
        strategy = Strategy.DISABLED;
    }

//src\lib.rs:3:9: 3:14 error: variable does not need to be mutable, #[forbid(unused_mut)] on by default
//src\lib.rs:3     let mut x = a * b;
//                     ^~~~~
//src\lib.rs:7:1: 9:2 error: function `xorAdd` should have a snake case name such as `xor_add`, #[forbid(non_snake_case)] on by default
//src\lib.rs:7 fn xorAdd(x: u64, a: u64, b: u64) -> u64 {
//src\lib.rs:8     (x ^ a) + (x ^ b)
//src\lib.rs:9 }
    public static LinterResult parse(ProcessResult processResult) {
        String output = processResult.output;
        String[] lines = output.split("\\r?\\n");
        LinterResult result = new LinterResult();
        errorFind:
        for (int n = 0; n < lines.length;) {
            String line = lines[n];
            Matcher matcher = ERROR_START.matcher(line);
            if (matcher.matches()) {
                String file_name = matcher.group("file");
                String description = matcher.group("description");
                int start_line = Integer.parseInt(matcher.group("startLine"));
                int start_column = Integer.parseInt(matcher.group("startColumn"));
                int end_line = Integer.parseInt(matcher.group("endLine"));
                int end_column = Integer.parseInt(matcher.group("endColumn"));
                LintError current = new LintError(file_name, description, start_line, start_column, end_line, end_column);
                for (; n < lines.length; n++) {
                    line = lines[n];
                    matcher = ERROR_CONTINUE.matcher(line);
                    if (matcher.matches()) {
                    } else {
                        matcher = ARROW.matcher(line);
                        if (matcher.matches()) {
                        } else {
                            File file = Paths.get(file_name).toFile();
                            List<ValidationError> errors = result.errors.get(file);
                            if (errors == null) {
                                errors = new ArrayList<>();
                                result.errors.put(file, errors);
                            }
                            errors.add(current);
                            continue errorFind;
                        }
                    }
                }
            } else {
                n++;
            }
        }
        return result;
    }

    @Override
    public Strategy getStrategy() {
        return strategy;
    }

    @Override
    public Map<File, List<ValidationError>> getValidationErrors() {
        return errors;
    }
}
