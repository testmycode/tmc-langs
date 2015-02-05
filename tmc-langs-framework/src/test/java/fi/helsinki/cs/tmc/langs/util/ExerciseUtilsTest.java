/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.helsinki.cs.tmc.langs.util;

import fi.helsinki.cs.tcm.langs.util.ExerciseUtils;
import java.nio.file.Path;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author villheik
 */
public class ExerciseUtilsTest {
    
    public ExerciseUtilsTest() {
    }
    
    /**
     * Test of prepareStub method, of class ExerciseUtils.
     */
    @Test
    public void testPrepareStub() {
        System.out.println("prepareStub");
        Path path = null;
        ExerciseUtils instance = new ExerciseUtils();
        instance.prepareStub(path);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of prepareSolution method, of class ExerciseUtils.
     */
    @Test
    public void testPrepareSolution() {
        System.out.println("prepareSolution");
        Path path = null;
        ExerciseUtils instance = new ExerciseUtils();
        instance.prepareSolution(path);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
