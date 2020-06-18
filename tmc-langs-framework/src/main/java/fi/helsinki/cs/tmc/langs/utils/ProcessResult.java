package fi.helsinki.cs.tmc.langs.utils;

public final class ProcessResult {
    public final int statusCode;
    public final String output;
    public final String errorOutput;
    public final boolean timedOut;

    /**
     * This is returned by Process Runner and has information about the result of the process.
     */
    public ProcessResult(int statusCode, String output, String errorOutput, boolean timedOut) {
        this.statusCode = statusCode;
        this.output = output;
        this.errorOutput = errorOutput;
        this.timedOut = timedOut;
    }
}
