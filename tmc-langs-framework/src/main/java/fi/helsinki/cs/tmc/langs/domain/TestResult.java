package fi.helsinki.cs.tmc.langs.domain;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * The result of a single test case.
 */
public class TestResult {

    /**
     * The full name of the test.
     *
     * <p>If the language organises tests into suites or classes, it is customary
     * to name the test as "class_name.method_name".
     */
    public final String name;

    /**
     * Whether the test passed.
     */
    public final boolean passed;

    /**
     * The points that may be awarded for this test case, if other requirements
     * for those points are satisfied.
     *
     * <p>May be empty but not null.
     */
    public final ImmutableList<String> points;

    /**
     * The test failure message.
     *
     * <p>Should be empty if the test passed. May not be null.
     */
    public final String errorMessage;

    /**
     * The backtrace for the test failure.
     *
     * <p>The backtrace may be in whatever format is natural for the language being
     * tested. Only stack frames (or similar) should be part of the backtrace
     * list. All other diagnostic messages and advice to the student should be
     * part of errorMessage.
     *
     * <p>Should be empty if the test passed. May not be null.
     */
    public final ImmutableList<String> backtrace;

    /**
     * Creates a new TestResult from the provided parameters.
     */
    public TestResult(String name,
            boolean passed,
            ImmutableList<String> points,
            String errorMessage,
            ImmutableList<String> backtrace) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(points);
        Preconditions.checkNotNull(errorMessage);
        Preconditions.checkNotNull(backtrace);
        this.name = name;
        this.passed = passed;
        this.points = points;
        this.errorMessage = errorMessage;
        this.backtrace = backtrace;
    }
}
