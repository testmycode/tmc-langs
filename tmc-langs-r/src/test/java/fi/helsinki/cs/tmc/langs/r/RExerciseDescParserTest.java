/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.helsinki.cs.tmc.langs.r;

import com.google.common.collect.ImmutableList;
import fi.helsinki.cs.tmc.langs.domain.TestDesc;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;
import java.io.IOException;
import java.nio.file.Path;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author janne
 */
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
    public void testThatParseSeemsToWorkOnExampleJson(){
        System.out.println(re);
        assertEquals(re.size(),6);
        assertEquals(re.get(0).points.size(),2);
        assertEquals(re.get(0).name,"Addition works");
        assertEquals(re.get(1).points.size(),2);
        assertEquals(re.get(1).name,"Multiplication works");
        assertEquals(re.get(2).points.size(),1);
        assertEquals(re.get(2).name,"Subtraction works");
        assertEquals(re.get(3).points.size(),1);
        assertEquals(re.get(3).name,"Division works");
        assertEquals(re.get(4).points.size(),0);
        assertEquals(re.get(4).name, "Test with no points");
        assertEquals(re.get(5).points.size(),0);
        assertEquals(re.get(5).name, "Dummy test set to fail");
    }
}
