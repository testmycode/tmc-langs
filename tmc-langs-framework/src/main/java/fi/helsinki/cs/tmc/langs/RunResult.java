package fi.helsinki.cs.tmc.langs;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public class RunResult {
    public static enum Status {
        PASSED,
        TESTS_FAILED,
        COMPILE_FAILED,
        GENERIC_ERROR,
    }

    /**
     * The overall status of the run.
     */
    public final Status status;

    /**
     * Whether each test passed and which points were awarded.
     *
     * If the tests could not be run (e.g. due to compilation failure) then this
     * may be empty (but not null).
     */
    public final ImmutableMap<String, TestResult> testResults;

    /**
     * Logs from the test run.
     *
     * See the SpecialLogs class for names of logs that TMC understands. The
     * result may also contain other custom log types.
     */
    public final ImmutableMap<String, byte[]> logs;

    /**
     * Attributes to be stored by TMC but have no special meaning to its generic
     * components.
     *
     * May be empty (but not null).
     */
    public final ImmutableMap<String, String> extraAttributes;

    public RunResult(Status status, ImmutableMap<String, TestResult> testResults, ImmutableMap<String, byte[]> logs, ImmutableMap<String, String> extraAttributes) {
        Preconditions.checkNotNull(status);
        Preconditions.checkNotNull(testResults);
        Preconditions.checkNotNull(logs);
        Preconditions.checkNotNull(extraAttributes);
        this.status = status;
        this.testResults = testResults;
        this.logs = logs;
        this.extraAttributes = extraAttributes;
    }

    public RunResult(Status status, ImmutableMap<String, TestResult> testResults, ImmutableMap<String, byte[]> logs) {
        this(status, testResults, logs, ImmutableMap.<String, String>of());
    }
}
