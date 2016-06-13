package fi.helsinki.cs.tmc.langs.make;

import fi.helsinki.cs.tmc.langs.domain.TestResult;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CTestCase {

    private static final String VALGRIND_FAIL_MESSAGE =
            " - Failed due to errors in valgrind log; see log below. "
                    + "Try submitting to server, some leaks might be platform dependent";

    private String name;
    private boolean passed;
    private String message;
    private List<String> points;
    private String valgrindTrace;
    private boolean failOnValgrindError;

    /**
     * Create a test case for C-tests.
     */
    public CTestCase(
            String name,
            boolean passed,
            String message,
            List<String> points,
            boolean failOnValgrindError) {
        this.name = name;
        this.passed = passed;
        this.message = message;
        this.points = points;
        this.valgrindTrace = null;
        this.failOnValgrindError = failOnValgrindError;
    }

    public CTestCase(String name, boolean passed, String message, List<String> points) {
        this(name, passed, message, points, true);
    }

    private boolean failedDueToValgrind(String valgrindTrace) {
        return failOnValgrindError && !Strings.isNullOrEmpty(valgrindTrace);
    }

    /**
     * Get the test result of this test case.
     */
    public TestResult getTestResult() {
        String msg = message;

        boolean valgrindFailed = failedDueToValgrind(valgrindTrace);
        boolean successful = passed && !valgrindFailed;
        boolean failedOnlyBecauseOfValgrind = passed && valgrindFailed;

        if (failedOnlyBecauseOfValgrind) {
            msg += VALGRIND_FAIL_MESSAGE;
        }

        ArrayList<String> trace = new ArrayList<>();

        if (!successful && valgrindTrace != null) {
            trace.addAll(Arrays.asList(valgrindTrace.split("\\n")));
        }

        ImmutableList<String> points = ImmutableList.of();
        if (this.points != null) {
            points = ImmutableList.copyOf(this.points);
        }

        return new TestResult(name, successful, points, msg, ImmutableList.copyOf(trace));
    }

    public String getName() {
        return name;
    }

    public boolean getResult() {
        return passed;
    }

    public String getMessage() {
        return message;
    }

    public void setValgrindTrace(String valgrindTrace) {
        this.valgrindTrace = valgrindTrace;
    }
}
