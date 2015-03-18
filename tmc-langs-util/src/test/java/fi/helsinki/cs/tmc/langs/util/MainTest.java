package fi.helsinki.cs.tmc.langs.util;

import fi.helsinki.cs.tmc.edutestutils.MockStdio;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class MainTest {

    @Rule
    public MockStdio mio = new MockStdio();

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    private final String HELP_TEXT = "Usage: TODO: Write instructions here.";
    private String expectedMessage = "";

    @Test
    public void testMain() {
        String[] args = null;
        exit.expectSystemExitWithStatus(0);
        exitStringContainsAssertion(HELP_TEXT);
        Main.main(args);
    }
    
    @Test
    public void testWithNoArgs() {
        String[] args = {};
        expectedMessage = HELP_TEXT;
        mainTest(0, args);
    }
    
    @Test
    public void testHelp() {
        String[] args = {"--help"};
        expectedMessage = HELP_TEXT;
        mainTest(0, args);
    }
    
    @Test
    public void testHelpWithInvalidArgumentCountOneArg() {
        String[] args = {"--help","dummy_string"};
        expectedMessage = "ERROR: wrong argument count for --help expected 0 got 1\n" + HELP_TEXT;
        mainTest(0, args);
    }

    @Test
    public void testScanExercise() {
        String exercisePath = getTargetPath("arith_funcs");
        String outputPath = exercisePath + "/checkstyle.txt";
        String[] args = {"--scanexercise", exercisePath, outputPath};
        expectedMessage = "Exercises scanned successfully, results can be found in " + outputPath;
        mainTest(0, args);
    }

    @Test
    public void testScanExerciseWithInvalidArgs() {
        String[] args = {"--scanexercise", "dummy string", "another"};
        exit.expectSystemExitWithStatus(0);
        expectedMessage = "ERROR: Given test path is not a directory.\n" + HELP_TEXT;
        mainTest(0, args);
    }

    @Test
    public void testScanExerciseWithInvalidArgumentCountZeroArgs() {
        String[] args = {"--scanexercise"};
        expectedMessage = "ERROR: wrong argument count for --scanexercise expected 2 got 0\n" + HELP_TEXT;
        mainTest(0, args);
    }

    @Test
    public void testScanExerciseWithInvalidArgumentCountOneArg() {
        String[] args = {"--scanexercise", "dummy string"};
        expectedMessage = "ERROR: wrong argument count for --scanexercise expected 2 got 1\n" + HELP_TEXT;
        mainTest(0, args);
    }

    @Test
    public void testRunTests() {
        String exercisePath = getTargetPath("arith_funcs");
        String outputPath = exercisePath + "/results.txt";
        String[] args = {"--runtests", exercisePath, outputPath};
        expectedMessage = "Test results can be found in " + outputPath;
        mainTest(0, args);
    }

    @Test
    public void testRunCheckCodeStyle() {
        String exercisePath = getTargetPath("arith_funcs");
        String outputPath = exercisePath + "/exercises.txt";
        String[] args = {"--checkstyle", exercisePath, outputPath};
        expectedMessage = "Codestyle report can be found at " + outputPath;
        mainTest(0, args);
    }
    
//    @Test
//    public void testPrepareStub() {
//        
//    }
    
//    @Test
//    public void testPrepareSolution() {
//        
//    }

    /**
     * Call main method with given args, and assert that main exits with given
     * exit status. Additionally do assertion that main prints out
     * {@link #expectedMessage}.
     *
     * @param exitStatus expected exit status for main.
     * @param args for calling main.
     * @param optionalErrorMessage for system out assertion.
     */
    private void mainTest(int exitStatus, String[] args, String... optionalErrorMessage) {
        exit.expectSystemExitWithStatus(exitStatus);
        exitStringContainsAssertion(expectedMessage, optionalErrorMessage);
        Main.main(args);
    }

    /**
     * Convert the given location into absolute test target path.
     *
     * @param location to be converted into target path.
     * @return Absolute test target path, with file:/ stripped away.
     */
    private String getTargetPath(String location) {
        return getClass().getResource(File.separatorChar + location).toString().substring(5);
    }

    private void exitStringContainsAssertion(final String expected, final String... optionalErrorMessage) {
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() throws Exception {
                String defaultErrorMessage = "Expected system output to contain\n" + expected
                        + "\ninstead got: " + mio.getSysOut().trim();
                if (optionalErrorMessage != null && optionalErrorMessage.length == 1) {
                    assertTrue(optionalErrorMessage[1], mio.getSysOut().trim().contains(expected));
                } else {
                    assertTrue(defaultErrorMessage, mio.getSysOut().trim().contains(expected));
                }
            }
        });
    }

}
