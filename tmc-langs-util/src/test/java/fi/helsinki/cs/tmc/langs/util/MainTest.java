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
        exit.expectSystemExitWithStatus(0);
        exitEqualsAssertion("Exercise scanned successfully, results can be found in " + outputPath);
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

    private void exitEqualsAssertion(final String expected, final String... optionalMessage) {
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() throws Exception {
                if (optionalMessage != null && optionalMessage.length == 1) {
                    assertEquals(optionalMessage[1], expected, mio.getSysOut());
                } else {
                    assertEquals(expected, mio.getSysOut().trim());
                }
            }
        });
    }

}
