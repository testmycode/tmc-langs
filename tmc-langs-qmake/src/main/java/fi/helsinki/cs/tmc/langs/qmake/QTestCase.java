package fi.helsinki.cs.tmc.langs.qmake;

import fi.helsinki.cs.tmc.langs.domain.TestResult;

import com.google.common.collect.ImmutableList;

import java.util.List;

public final class QTestCase {

    private String name;
    private boolean passed;
    private String message;
    private List<String> points;

    /**
     * Create a test case for QT tests.
     *
     * @param name Name of the test
     * @param passed Passed status
     * @param message for failed assertion
     * @param points for test case
     */
    public QTestCase(String name, boolean passed, String message, List<String> points) {
        this.name = name;
        this.passed = passed;
        this.message = message;
        this.points = points;
    }

    /**
     * Get the test result of this test case.
     */
    public TestResult getTestResult() {
        String msg = message;

        ImmutableList<String> trace = ImmutableList.of();
        ImmutableList<String> points = ImmutableList.of();
        if (this.points != null) {
            points = ImmutableList.copyOf(this.points);
        }

        return new TestResult(name, passed, points, msg, trace);
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

    public List<String> getPoints() {
        return points;
    }

}
