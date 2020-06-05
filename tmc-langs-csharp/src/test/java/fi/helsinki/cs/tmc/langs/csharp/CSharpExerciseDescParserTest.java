package fi.helsinki.cs.tmc.langs.csharp;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import fi.helsinki.cs.tmc.langs.domain.TestDesc;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;

public class CSharpExerciseDescParserTest {

    private final Path allPassedSampleDir;
    private final Path noPointsSampleDir;

    public CSharpExerciseDescParserTest() {
        Path descSamplesDir = TestUtils.getPath(getClass(), "Desc_samples");
        allPassedSampleDir = descSamplesDir.resolve("all_tests_passed_sample");
        noPointsSampleDir = descSamplesDir.resolve("no_points_sample");
    }

    private void testDescAsExpected(TestDesc desc, String name, String[] points) {
        assertEquals(name, desc.name);
        assertArrayEquals(points, desc.points.toArray());
    }

    @Test
    public void testDescParsePointsCorrectly() throws IOException {
        ImmutableList<TestDesc> descs = CSharpExerciseDescParser.parse(allPassedSampleDir);

        testDescAsExpected(descs.get(0), "PassingSampleTests.ProgramTest.TestGetName", 
                new String[]{"1", "1.1"});
        testDescAsExpected(descs.get(1), "PassingSampleTests.ProgramTest.TestGetYear", 
                new String[]{"1", "1.2"});
    }

    @Test
    public void testDescParseEmptyPointsCorrectly() throws IOException {
        ImmutableList<TestDesc> descs = CSharpExerciseDescParser.parse(noPointsSampleDir);

        testDescAsExpected(descs.get(0), 
                "NoPoints.ProgramTest.TestCheckSameFailed", new String[]{});
        testDescAsExpected(descs.get(1), 
                "NoPoints.ProgramTest.TestCheckFinePassed", new String[]{});
    }
}
