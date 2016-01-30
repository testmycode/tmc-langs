package fi.helsinki.cs.tmc.langs.domain;

import fi.helsinki.cs.tmc.testscanner.TestMethod;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * A description of an exercise's test case.
 */
public final class TestDesc {

    /**
     * The full name of the test.
     *
     * <p>If the language organises tests into suites or classes, it is customary
     * to name the test as "class_name.method_name".
     */
    public final String name;

    /**
     * The list of point names that passing this test may give.
     *
     * <p>To obtain a point X, the user must pass all exercises that require point X.
     */
    public final ImmutableList<String> points;

    /**
     * Create a new TestDesc.
     *
     * @param name Name of the test case
     * @param points List of point names related to this test case
     */
    public TestDesc(String name, ImmutableList<String> points) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(points);
        this.name = name;
        this.points = points;
    }

    public static TestDesc from(TestMethod method) {
        return new TestDesc(
                method.className + " " + method.methodName, ImmutableList.copyOf(method.points));
    }

    @Override
    public String toString() {
        return "<TestDesc name: " + name + ", points: " + points + ">";
    }
}
