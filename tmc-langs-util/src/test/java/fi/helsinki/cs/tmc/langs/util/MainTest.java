package fi.helsinki.cs.tmc.langs.util;

import fi.helsinki.cs.tmc.edutestutils.MockStdio;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import java.io.File;

public class MainTest {
    @Rule
    public MockStdio mio = new MockStdio();

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    private String HELP_TEXT = "Usage: TODO: Write instructions here.";
    private String expectedMessage = "";

    @Test
    public void testMain() {
        String[] args = null;
        exit.expectSystemExitWithStatus(0);
        exitEqualsAssertion(HELP_TEXT);
        Main.main(args);
    }

    @Test
    public void testScanExercise() {
        String exercisePath = getTargetPath("arith_funcs");
        String outputPath = exercisePath + "/checkstyle.txt";
        String[] args = {"--scanexercise", exercisePath, outputPath};
        expectedMessage = "Exercise scanned successfully, results can be found in " + outputPath;
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
    public void testScanExerciseWithWrongArgumentCount() {
        String[] args = {"--scanexercise"};
        expectedMessage = "ERROR: wrong argument count for --scanexercise expected 2 got 0\n" + HELP_TEXT;
        mainTest(0, args);
    }

    private void mainTest(int exitStatus, String[] args, String... optionalErrorMessage) {
        exit.expectSystemExitWithStatus(exitStatus);
        exitEqualsAssertion(expectedMessage, optionalErrorMessage);
        Main.main(args);
    }

    /**
     * Convert the given location into absolute test target path.
     * @param location to be converted into target path.
     * @return Absolute test target path, with file:/ stripped away.
     */
    private String getTargetPath(String location) {
        return getClass().getResource(File.separatorChar + location).toString().substring(5);
    }

    private void exitEqualsAssertion(final String expected, final String... optionalErrorMessage) {
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() throws Exception {
                if (optionalErrorMessage != null && optionalErrorMessage.length == 1) {
                    assertEquals(optionalErrorMessage[1], expected, mio.getSysOut());
                } else {
                    assertEquals(expected, mio.getSysOut().trim());
                }
            }
        });
    }

}
