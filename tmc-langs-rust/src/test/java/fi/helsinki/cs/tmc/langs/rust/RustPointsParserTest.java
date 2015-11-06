package fi.helsinki.cs.tmc.langs.rust;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.TestDesc;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

public class RustPointsParserTest {

    private RustPointsParser parser;
    private static final String name = "Exercise";

    @Before
    public void setUp() {
        parser = new RustPointsParser();
    }

    @Test
    public void worksWithNothing() {
        Optional<ExerciseDesc> result = parser.parse(Collections.<String>emptyList(), name);
        assertTrue(result.isPresent());
        assertEquals(name, result.get().name);
        assertTrue(result.get().tests.isEmpty());
    }

    @Test
    public void failsWithNoValue() {
        assertFalse(parser.parse(ImmutableList.of("test"), name).isPresent());
    }

    @Test
    public void worksWithOne() {
        Optional<ExerciseDesc> result
                = parser.parse(ImmutableList.of("test = 1"), name);
        assertTrue(result.isPresent());
        assertEquals(name, result.get().name);
        assertEquals(1, result.get().tests.size());
        TestDesc desc = result.get().tests.get(0);
        assertEquals("test", desc.name);
        assertEquals(1, desc.points.size());
        assertEquals("1", desc.points.get(0));
    }

    @Test
    public void worksWithMultipleSame() {
        Optional<ExerciseDesc> result
                = parser.parse(ImmutableList.of("test = 1", "test = 2"), name);
        assertTrue(result.isPresent());
        assertEquals(name, result.get().name);
        assertEquals(1, result.get().tests.size());
        TestDesc desc = result.get().tests.get(0);
        assertEquals("test", desc.name);
        assertEquals(2, desc.points.size());
        assertTrue(desc.points.contains("1"));
        assertTrue(desc.points.contains("2"));
    }

    @Test
    public void worksWithMultipleDifferent() {
        Optional<ExerciseDesc> result
                = parser.parse(ImmutableList.of("test = 1", "asd = 2"), name);
        assertTrue(result.isPresent());
        assertEquals(name, result.get().name);
        assertEquals(2, result.get().tests.size());
        TestDesc desc1 = result.get().tests.get(0);
        TestDesc desc2 = result.get().tests.get(1);
        if (desc2.name.equals("test")) {
            TestDesc temp = desc1;
            desc1 = desc2;
            desc2 = temp;
        }
        assertEquals("test", desc1.name);
        assertEquals(1, desc1.points.size());
        assertEquals("1", desc1.points.get(0));

        assertEquals("asd", desc2.name);
        assertEquals(1, desc2.points.size());
        assertEquals("2", desc2.points.get(0));
    }

    @Test
    public void worksWithSuites() {
        Optional<ExerciseDesc> result
                = parser.parse(ImmutableList.of("test = 1", "test.asd = 2"), name);
        assertTrue(result.isPresent());
        assertEquals(name, result.get().name);
        assertEquals(2, result.get().tests.size());
        TestDesc desc1 = result.get().tests.get(0);
        TestDesc desc2 = result.get().tests.get(1);
        if (desc2.name.equals("test")) {
            TestDesc temp = desc1;
            desc1 = desc2;
            desc2 = temp;
        }
        assertEquals("test", desc1.name);
        assertEquals(1, desc1.points.size());
        assertTrue(desc1.points.contains("1"));

        assertEquals("test.asd", desc2.name);
        assertEquals(2, desc2.points.size());
        assertTrue(desc2.points.contains("1"));
        assertTrue(desc2.points.contains("2"));
    }

    @Test
    public void failsWithDeepSuites() {
        assertFalse(parser.parse(ImmutableList.of("a.b.c = 0"), name).isPresent());
    }
}
