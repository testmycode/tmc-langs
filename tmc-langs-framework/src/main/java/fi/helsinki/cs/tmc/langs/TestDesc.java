package fi.helsinki.cs.tmc.langs;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public final class TestDesc {
    /**
     * The full name of the test.
     *
     * <p>
     * If the language organises tests into suites or classes, it is customary
     * to name the test as "class_name.method_name".
     */
    public final String name;

    /**
     * The list of point names that passing this test may give.
     *
     * To obtain a point X, the user must pass all exercises that require
     * point X.
     */
    public final ImmutableList<String> points;

    public TestDesc(String name, ImmutableList<String> points) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(points);
        this.name = name;
        this.points = points;
    }
}
