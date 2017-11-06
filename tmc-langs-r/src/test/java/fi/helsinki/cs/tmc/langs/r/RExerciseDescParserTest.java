package fi.helsinki.cs.tmc.langs.r;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import fi.helsinki.cs.tmc.langs.domain.TestDesc;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;

public class RExerciseDescParserTest {

    private final Path simpleAllPassJsonDir;
    private final Path noPointsJsonDir;
    
    public RExerciseDescParserTest() {
        Path exampleJsonsDir = TestUtils.getPath(getClass(), "example_jsons");
        simpleAllPassJsonDir = exampleJsonsDir.resolve("simple_all_tests_pass");
        noPointsJsonDir = exampleJsonsDir.resolve("no_points");
    }

    private void testDescAsExpected(TestDesc desc, String name, String[] points) {
        assertEquals(name, desc.name);
        assertArrayEquals(points, desc.points.toArray());
    }

    @Test
    public void testThatParseWorksForSimpleAllPassJson() throws IOException {
        ImmutableList<TestDesc> descs = new RExerciseDescParser(simpleAllPassJsonDir).parse();

        testDescAsExpected(descs.get(0),"ret_true works.", new String[]{"r1", "r1.1"});
        testDescAsExpected(descs.get(1),"ret_one works.", new String[]{"r1", "r1.2"});
        testDescAsExpected(descs.get(2),"add works.", new String[]{"r1", "r1.3", "r1.4"});
        testDescAsExpected(descs.get(3),"minus works", new String[]{"r2", "r2.1"});
    }

    @Test
    public void testThatParseWorksForNoPointsJson() throws IOException {
        ImmutableList<TestDesc> descs = new RExerciseDescParser(noPointsJsonDir).parse();

        testDescAsExpected(descs.get(0),"no points test1", new String[]{});
        testDescAsExpected(descs.get(1),"no points test2", new String[]{});
        testDescAsExpected(descs.get(2),"no points test3", new String[]{});
    }
}
