
package fi.helsinki.cs.tmc.langs.r;

import static org.junit.Assert.assertEquals;

import fi.helsinki.cs.tmc.langs.domain.TestDesc;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;



public class RExerciseDescParserTest {
    private ImmutableList<TestDesc> re;
    private Path jsonDir;
    
    public RExerciseDescParserTest() {
        jsonDir = TestUtils.getPath(getClass(), "example_json");
        try {
            re = new RExerciseDescParser(jsonDir).parse();
        } catch (IOException e) {
            System.out.println("Something wrong: " + e.getMessage());
        }
    }
    
    @Test
    public void testThatParseSeemsToWorkOnExampleJson() {
        assertEquals(re.size(),22);
        assertEquals(re.get(0).points.size(),3);
        assertEquals(re.get(0).name,"Addition works");
        assertEquals(re.get(1).points.size(),3);
        assertEquals(re.get(1).name,"Multiplication works");
        assertEquals(re.get(2).points.size(),2);
        assertEquals(re.get(2).name,"Subtraction works");
        assertEquals(re.get(3).points.size(),2);
        assertEquals(re.get(3).name,"Division works");
        assertEquals(re.get(4).points.size(),1);
        assertEquals(re.get(4).name, "Test with no points");
        assertEquals(re.get(5).points.size(),1);
        assertEquals(re.get(5).name, "Dummy test set to fail");
        assertEquals(re.get(6).points.size(),2);
        assertEquals(re.get(6).name, "Matrix transpose with [[1,2]] works");
        assertEquals(re.get(7).points.size(),2);
        assertEquals(re.get(7).name, "Matrix transpose with [[1,2],[3,4]] works");
        assertEquals(re.get(8).points.size(),2);
        assertEquals(re.get(8).name, "Constant string works");
        for (int i = 1;i <= 13;i++) {
            assertEquals(re.get(8 + i).name, "Exercise " + i + " is correct"); 
            if (i == 3) {
                assertEquals(re.get(8 + i).points.size(),3); 
            } else {
                assertEquals(re.get(8 + i).points.size(),2);
            }
        }
    }
}
