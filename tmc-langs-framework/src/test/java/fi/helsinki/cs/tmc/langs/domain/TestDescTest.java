package fi.helsinki.cs.tmc.langs.domain;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

public class TestDescTest {

    private String name = "name";
    private ImmutableList<String> points = ImmutableList.of("1", "2");

    @Test
    public void constructorSetsValues() {
        TestDesc testDesc = new TestDesc(name, points);
        assertEquals(name, testDesc.name);
        assertEquals(points, testDesc.points);
    }

    @Test(expected = NullPointerException.class)
    public void constructorDoesNotAllowNullName() {
        new TestDesc(null, points);
    }

    @Test(expected = NullPointerException.class)
    public void constructorDoesNotAllowNullPoints() {
        new TestDesc(name, null);
    }
}
