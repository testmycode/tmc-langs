package fi.helsinki.cs.tmc.langs.domain;

import com.google.common.annotations.Beta;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;

/**
 * The result of a single test case. NetBeanses TestCaseResult
 */
public class TestResult {

    private String name;
    private boolean successful;
    private String message;

    @SerializedName("detailed_message")
    private String detailedMessage;

    // TODO: is this used?
    private boolean valgrindFailed;

    public final ImmutableList<String> points;
    private ImmutableList<String> exception;

    public TestResult() {
        this.points = ImmutableList.of();
    }

    public TestResult(String name, boolean successful, String message) {
        this.name = name;
        this.successful = successful;
        this.message = message;
        this.points = ImmutableList.of();
    }

    public TestResult(String name,
                      boolean successful,
                      String message,
                      String detailedMessage,
                      boolean valgrindFailed) {
        this.name = name;
        this.successful = successful;
        this.message = message;
        this.detailedMessage = detailedMessage;
        this.valgrindFailed = valgrindFailed;
        this.points = ImmutableList.of();
    }

    public TestResult(String name,
                      boolean passed,
                      ImmutableList<String> points,
                      String message,
                      ImmutableList<String> exception) {
        this.name = name;
        this.successful = passed;
        this.message = message;
        this.points = points;
        this.exception = exception;
    }

    public String getName() {
        return name;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getMessage() {
        return message;
    }

    public ImmutableList<String> getException() {
        return exception;
    }

    public ImmutableList<String> getDetailedMessage() {
        if (Strings.isNullOrEmpty(detailedMessage)) {
            return ImmutableList.of();
        }
        return ImmutableList.copyOf(detailedMessage.split("\n"));
    }

    @Beta
    // TODO: is this used?
    public boolean getValgrindFailed() {
        return this.valgrindFailed;
    }

    @Override
    public String toString() {
        return "TestResult{"
                + "name='" + name + '\''
                + ", successful=" + successful
                + ", message='" + message + '\''
                + ", detailedMessage='" + getDetailedMessage() + '\''
                + ", valgrindFailed=" + valgrindFailed
                + ", points=" + points
                + ", exception=" + exception
                + '}';
    }
}
