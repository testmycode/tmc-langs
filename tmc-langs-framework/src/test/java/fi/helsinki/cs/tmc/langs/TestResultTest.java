package fi.helsinki.cs.tmc.langs;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestResultTest {

    private String name = "name";
    private boolean passed = true;
    private ImmutableList<String> points = ImmutableList.of("p1", "p2");
    private String errorMessage = "error";
    private ImmutableList<String> backtrace = ImmutableList.of("e1, e2");

    @Test
    public void constructorSetsValues() {
        TestResult result = new TestResult(name, passed, points, errorMessage, backtrace);

        assertEquals(name, result.name);
        assertEquals(passed, result.passed);
        assertEquals(points, result.points);
        assertEquals(errorMessage, result.errorMessage);
        assertEquals(backtrace, result.backtrace);
    }

    @Test(expected = NullPointerException.class)
    public void nameCanNotBeNull() {
        new TestResult(null, passed, points, errorMessage, backtrace);
    }

    @Test(expected = NullPointerException.class)
    public void pointsCanNotBeNull() {
        new TestResult(name, passed, null, errorMessage, backtrace);
    }

    @Test(expected = NullPointerException.class)
    public void errorMessageCanNotBeNull() {
        new TestResult(name, passed, points, null, backtrace);
    }

    @Test(expected = NullPointerException.class)
    public void backtraceCanNotBeNull() {
        new TestResult(name, passed, points, errorMessage, null);
    }
}
