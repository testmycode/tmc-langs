package fi.helsinki.cs.tmc.langs.domain;

/**
 * Holds constants for special log types for use in RunResult.
 */
public final class SpecialLogs {

    public static final String STDOUT = "stdout";
    public static final String STDERR = "stderr";
    public static final String COMPILER_OUTPUT = "compiler_output";
    public static final String GENERIC_ERROR_MESSAGE = "generic_error_message";

    private SpecialLogs() {}
}
