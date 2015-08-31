package fi.helsinki.cs.tmc.langs.util;

import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.edutestutils.MockStdio;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;
import fi.helsinki.cs.tmc.langs.domain.TestDesc;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;


import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.junit.contrib.java.lang.system.Assertion;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

public class MainTest {

    @Rule
    public MockStdio mio = new MockStdio();

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    private String expectedMessage = "";
    private final String helpText = Main.HELP_TEXT + "\n";
    private final TaskExecutor executor = Mockito.mock(TaskExecutor.class);
    private Main mainClass;

    @Before
    public void setUp() {
        TestUtils.skipTestIfOnWindowsContinuosIntegration();
        mainClass = new Main();
        Main.setExecutor(executor);
    }

    @Test
    public void testMain() {
        String[] args = null;
        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() throws Exception {
                Mockito.verifyZeroInteractions(executor);
                assertTrue("Error output should be clean.", mio.getSysErr().isEmpty());
                assertContains(helpText, mio.getSysOut());
            }
        });
        Main.main(args);
    }

    @Test
    public void testWithNoArgs() {
        String[] args = {};
        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() throws Exception {
                Mockito.verifyZeroInteractions(executor);
                assertTrue("Error output should be clean.", mio.getSysErr().isEmpty());
                assertContains(helpText, mio.getSysOut());
            }
        });
        Main.main(args);
    }

    @Test
    public void testHelp() {
        String[] args = {"help"};
        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() throws Exception {
                Mockito.verifyZeroInteractions(executor);
                assertTrue("Error output should be clean.", mio.getSysErr().isEmpty());
                assertContains(helpText, mio.getSysOut());
            }
        });
        Main.main(args);
    }

    @Test
    public void testHelpWithExtraCommands() {
        String[] args = {"help", "dummy_string"};
        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() throws Exception {
                Mockito.verifyZeroInteractions(executor);
                assertTrue("Error output should be clean.", mio.getSysErr().isEmpty());
                assertContains(helpText, mio.getSysOut());
            }
        });
        Main.main(args);
    }

    @Test
    public void testScanExercise() throws NoLanguagePluginFoundException {
        final String exercisePath = getTargetPath("arith_funcs");
        final String outputPath = exercisePath + "/checkstyle.txt";

        Mockito.when(executor.scanExercise(Paths.get(exercisePath), "arith_funcs"))
                .thenReturn(Optional.of(new ExerciseDesc("Name",
                        ImmutableList.copyOf(new ArrayList<TestDesc>()))));
        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() throws Exception {
                Mockito.verify(executor).scanExercise(Paths.get(exercisePath), "arith_funcs");
                assertContains("Exercises scanned successfully, results can be found in "
                                + outputPath
                                + "\n",
                        mio.getSysOut());
                assertTrue("Error output should be clean.", mio.getSysErr().isEmpty());
            }
        });
        String[] args = {"scan-exercise", exercisePath, outputPath};
        Main.main(args);
    }

    @Test
    public void testScanExerciseWithInvalidArgs() {
        String[] args = {"scan-exercise", "dummy string", "another"};
        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() throws Exception {
                Mockito.verifyZeroInteractions(executor);
                assertContains("Error output wasn't what was expected",
                        "ERROR: Given test path is not a directory.\n",
                        mio.getSysErr());
                assertContains("System output wasn't what was expected",
                        helpText,
                        mio.getSysOut());
            }
        });
        Main.main(args);
    }

    @Test
    public void testScanExerciseWithInvalidArgumentCountZeroArgs() {
        String[] args = {"scan-exercise"};
        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() throws Exception {
                Mockito.verifyZeroInteractions(executor);
                assertContains(helpText, mio.getSysOut());
                assertContains("ERROR: wrong argument count for scan-exercise expected 2 got 0\n",
                        mio.getSysErr());
            }
        });
        Main.main(args);
    }

    @Test
    public void testScanExerciseWithInvalidArgumentCountOneArg() {
        String[] args = {"scan-exercise", "dummy string"};
        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() throws Exception {
                Mockito.verifyZeroInteractions(executor);
                assertContains("System output should contain help text",
                        helpText,
                        mio.getSysOut());
                assertContains("Error output should contain the error message",
                        "ERROR: wrong argument count for scan-exercise expected 2 got 1\n",
                        mio.getSysErr());
            }
        });
        Main.main(args);
    }

    @Test
    public void testRunTests() {
        final String exercisePath = getTargetPath("arith_funcs");
        final String outputPath = exercisePath + "/results.txt";
        String[] args = {"run-tests", exercisePath, outputPath};

        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() throws Exception {
                Mockito.verify(executor).runTests(Paths.get(exercisePath));
                assertContains("Test results can be found in " + outputPath + "\n",
                        mio.getSysOut());
                assertTrue("Error output should be clean.", mio.getSysErr().isEmpty());
            }
        });
        Main.main(args);
    }

    @Test
    public void testRunCheckCodeStyle() {
        final String exercisePath = getTargetPath("arith_funcs");
        final String outputPath = exercisePath + "/exercises.txt";
        String[] args = {"checkstyle", exercisePath, outputPath};

        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() throws Exception {
                Mockito.verify(executor).runCheckCodeStyle(Paths.get(exercisePath));
                assertContains("Codestyle report can be found at " + outputPath + "\n",
                        mio.getSysOut());
                assertTrue("Error output should be clean.", mio.getSysErr().isEmpty());
            }
        });
        Main.main(args);
    }

    @Test
    public void testPrepareStub() {
        String[] args = {"prepare-stub", getTargetPath("arith_funcs")};
        final Path stubPath = new File(getTargetPath("arith_funcs")).toPath();

        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() throws Exception {
                Mockito.verify(executor).prepareStub(stubPath);
            }
        });
        Main.main(args);
    }

    @Test
    public void testPrepareSolution() {
        String[] args = {"prepare-solution", getTargetPath("arith_funcs")};
        final Path solutionPath = new File(getTargetPath("arith_funcs")).toPath();

        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() throws Exception {
                Mockito.verify(executor).prepareSolution(solutionPath);
            }
        });
        Main.main(args);
    }

    /**
     * Convert the given location into absolute test target path.
     *
     * @param location to be converted into target path.
     * @return Absolute test target path, with file:/ stripped away.
     */
    private String getTargetPath(String location) {
        String targetPath = getClass().getResource(File.separatorChar + location).toString();

        if (targetPath.startsWith("file:/")) {
            return targetPath.substring(5);
        }

        return targetPath;
    }

    private void assertContains(String message, String needle, String haystack) {
        assertTrue(message, haystack.contains(needle));
    }

    private void assertContains(String needle, String haystack) {
        assertTrue(haystack.contains(needle));
    }
}
