package fi.helsinki.cs.tmc.langs.java;

import java.io.File;

/**
 * Contains results file and {@code stderr} and {@code stdout}.
 */
public class TestRunFileAndLogs {

    private File testResultsFile;

    private byte[] stdout;

    private byte[] stderr;

    public TestRunFileAndLogs(File testResultsFile, byte[] stdout, byte[] stderr) {
        this.testResultsFile = testResultsFile;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    public File getTestResultsFile() {
        return testResultsFile;
    }

    public byte[] getStderr() {
        return stderr;
    }

    public byte[] getStdout() {
        return stdout;
    }
}
