package fi.helsinki.cs.tmc.langs.rust;

import fi.helsinki.cs.tmc.langs.abstraction.Strategy;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationError;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.utils.ProcessResult;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinterResult implements ValidationResult {

    //src\lib.rs:3:9: 3:14 error: variable does not need to be mutable, #[forbid(unused_mut)] on by default
    private static final Pattern ERROR_START
            = Pattern.compile("(?<file>.+):(?<start_line>\\d+):(?<start_column>\\d+): (?<end_line>\\d+):(?<end_column>\\d+) error: (?<description>.*), #[forbid\\(.*\\)] on by default");
    //src\lib.rs:3     let mut x = a * b;
    private static final Pattern ERROR_CONTINUE
            = Pattern.compile("(?<before>(?<file>.+):(?<line>\\d+) )(?<code>.*)");
    //                     ^~~~~
    private static final Pattern ARROW
            = Pattern.compile("(?<before> *)^(?<stem>~*)");

    private Strategy strategy;
    private Map<File, List<ValidationError>> errors;

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
        State state = State.FIND_ERROR;
        LintError current;
        Matcher matcher;
        for (String line : lines) {
            switch (state) {
                case FIND_ERROR:
                    matcher = ERROR_START.matcher(line);
                    if (matcher.matches()) {
                        String file = matcher.group("file");
                        String description = matcher.group("description");
                        int start_line = Integer.parseInt(matcher.group("start_line"));
                        int start_column = Integer.parseInt(matcher.group("start_column"));
                        int end_line = Integer.parseInt(matcher.group("end_line"));
                        int end_column = Integer.parseInt(matcher.group("end_column"));
                        current = new LintError(file, description, start_line, start_column, end_line, end_column);
                    }
                    state = State.INSPECT_ERROR;
                    break;
                case INSPECT_ERROR:
                    matcher = ERROR_CONTINUE.matcher(line);
                    if(matcher.matches()) {

                    } else {
                        matcher = ARROW.matcher(line);
                        if(matcher.matches()) {

                        } else {
                            matcher = ERROR_START.matcher(line);
                            if (matcher.matches()) {

                            }
                        }
                    }
                    break;
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

    private static enum State {

        FIND_ERROR,
        INSPECT_ERROR,
    }
}
