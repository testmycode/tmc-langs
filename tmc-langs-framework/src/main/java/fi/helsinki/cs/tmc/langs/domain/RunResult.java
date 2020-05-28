package fi.helsinki.cs.tmc.langs.domain;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.stream.Collectors;

/**
 * The result of running an exercise's test suite against a submission.
 */
public final class RunResult {

    public enum Status {

        /**
         * The submission and tests compiled and all tests passed.
         */
        PASSED,
        /**
         * The submission and tests compiled but some tests failed.
         */
        TESTS_FAILED,
        /**
         * The submission or tests did not compile.
         *
         * <p>The compiler error should be given in {@code logs[SpecialLogs.COMPILER_OUTPUT]}.
         */
        COMPILE_FAILED,
        /**
         * The submission compiled but testrun was interrupted.
         */
        TESTRUN_INTERRUPTED,
        /**
         * For when no other status seems suitable, or the language plugin has
         * suffered an internal error.
         *
         * <p>Details should be given in {@code logs[SpecialLogs.GENERIC_ERROR_MESSAGE]}.
         */
        GENERIC_ERROR,
    }

    /**
     * The overall status of the run.
     */
    public final Status status;

    /**
     * Whether each test passed and which points were awarded.
     *
     * <p>If the tests could not be run (e.g. due to compilation failure) then this
     * may be empty (but not null).
     */
    public final ImmutableList<TestResult> testResults;

    /**
     * Logs from the test run.
     *
     * <p>The key may be an arbitrary string identifying the type of log.
     *
     * <p>See the SpecialLogs class for names of logs that TMC understands. The
     * result may also contain other custom log types.
     */
    public final ImmutableMap<String, byte[]> logs;

    /**
     * Create a new RunResult to represent the results of run of the test suite.
     */
    public RunResult(
            Status status,
            ImmutableList<TestResult> testResults,
            ImmutableMap<String, byte[]> logs) {
        Preconditions.checkNotNull(status);
        Preconditions.checkNotNull(testResults);
        Preconditions.checkNotNull(logs);
        this.status = status;
        this.testResults = testResults;
        this.logs = logs;
    }

    @Override
    public String toString() {
        return "RunResult{"
                + "status=" + status
                + ", testResults=" + testResults
                + ", logKeys=" + logs.entrySet().stream().collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> new String(e.getValue())
                ))
                + '}';
    }
}
