package fi.helsinki.cs.tmc.langs.rust;

import fi.helsinki.cs.tmc.langs.abstraction.Strategy;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationError;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LinterResult implements ValidationResult {
    private Strategy strategy;
    private final Map<File, List<ValidationError>> errors;

    public LinterResult(Map<File, List<ValidationError>> errors) {
        this.errors = errors;
        strategy = Strategy.DISABLED;
    }

    @Override
    public Strategy getStrategy() {
        return strategy;
    }

    @Override
    public Map<File, List<ValidationError>> getValidationErrors() {
        return Collections.unmodifiableMap(errors);
    }
}