package fi.helsinki.cs.tmc.langs;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExerciseDescTest {

    @Test
    public void constructorSetsValues() {
        String name = "name";
        ImmutableList<TestDesc> tests = ImmutableList.of();

        ExerciseDesc desc = new ExerciseDesc(name, tests);

        assertEquals(name, desc.name);
        assertEquals(tests, desc.tests);
    }

    @Test(expected = NullPointerException.class)
    public void constructorDoesNotAllowNullName() {
        ImmutableList<TestDesc> tests = ImmutableList.of();
        new ExerciseDesc(null, tests);
    }

    @Test(expected = NullPointerException.class)
    public void constructorDoesNotAllowNullTestList() {
        new ExerciseDesc("name", null);
    }
}
