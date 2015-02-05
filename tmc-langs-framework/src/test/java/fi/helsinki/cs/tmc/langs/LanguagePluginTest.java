/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.helsinki.cs.tmc.langs;

import com.google.common.collect.ImmutableList;
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
public class LanguagePluginTest {
    
    public LanguagePluginTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getLanguageName method, of class LanguagePlugin.
     */
    @Test
    public void testGetLanguageName() {
        System.out.println("getLanguageName");
        LanguagePlugin instance = new LanguagePluginImpl();
        String expResult = "";
        String result = instance.getLanguageName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findExercises method, of class LanguagePlugin.
     */
    @Test
    public void testFindExercises() {
        System.out.println("findExercises");
        Path basePath = null;
        LanguagePlugin instance = new LanguagePluginImpl();
        ImmutableList<Path> expResult = null;
        ImmutableList<Path> result = instance.findExercises(basePath);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of scanExercise method, of class LanguagePlugin.
     */
    @Test
    public void testScanExercise() {
        System.out.println("scanExercise");
        Path path = null;
        String exerciseName = "";
        LanguagePlugin instance = new LanguagePluginImpl();
        ExerciseDesc expResult = null;
        ExerciseDesc result = instance.scanExercise(path, exerciseName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of runTests method, of class LanguagePlugin.
     */
    @Test
    public void testRunTests() {
        System.out.println("runTests");
        Path path = null;
        LanguagePlugin instance = new LanguagePluginImpl();
        RunResult expResult = null;
        RunResult result = instance.runTests(path);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of prepareSubmission method, of class LanguagePlugin.
     */
    @Test
    public void testPrepareSubmission() {
        System.out.println("prepareSubmission");
        Path submissionPath = null;
        Path destPath = null;
        LanguagePlugin instance = new LanguagePluginImpl();
        instance.prepareSubmission(submissionPath, destPath);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of prepareStub method, of class LanguagePlugin.
     */
    @Test
    public void testPrepareStub() {
        System.out.println("prepareStub");
        Path path = null;
        LanguagePlugin instance = new LanguagePluginImpl();
        instance.prepareStub(path);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of prepareSolution method, of class LanguagePlugin.
     */
    @Test
    public void testPrepareSolution() {
        System.out.println("prepareSolution");
        Path path = null;
        LanguagePlugin instance = new LanguagePluginImpl();
        instance.prepareSolution(path);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    public class LanguagePluginImpl implements LanguagePlugin {

        public String getLanguageName() {
            return "";
        }

        public ImmutableList<Path> findExercises(Path basePath) {
            return null;
        }

        public ExerciseDesc scanExercise(Path path, String exerciseName) {
            return null;
        }

        public RunResult runTests(Path path) {
            return null;
        }

        public void prepareSubmission(Path submissionPath, Path destPath) {
        }

        public void prepareStub(Path path) {
        }

        public void prepareSolution(Path path) {
        }
    }
    
}
