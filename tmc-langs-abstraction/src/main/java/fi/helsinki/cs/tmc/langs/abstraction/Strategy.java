package fi.helsinki.cs.tmc.langs.abstraction;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Strategy {
    FAIL,
    WARN,
    DISABLED;

    @JsonCreator
    public static Strategy fromValue(final String value) {
        return Strategy.valueOf(Strategy.class, value.toUpperCase());
    }
}
