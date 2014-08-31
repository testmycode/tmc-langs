package fi.helsinki.cs.tmc.langs;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class TestResult {
    public boolean passed;
    public ImmutableList<String> points;

    public TestResult(boolean passed, ImmutableList<String> points) {
        Preconditions.checkNotNull(points);
        this.passed = passed;
        this.points = points;
    }
}
