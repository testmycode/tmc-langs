package fi.helsinki.cs.tmc.langs.cli;

import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.edutestutils.MockStdio;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;
import fi.helsinki.cs.tmc.langs.domain.TestDesc;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import org.mockito.Mockito;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Locale;

public class MainTest {

    @Rule public MockStdio mio = new MockStdio();

    @Rule public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    private Main mainClass;
    private String expectedMessage = "";
    private final String helpText = Main.HELP_TEXT + "\n";
    private final TaskExecutor executor = Mockito.mock(TaskExecutor.class);

    private static final String EXERCISE_PATH = "--exercisePath";
    private static final String OUTPUT_PATH = "--outputPath";
    private static final String LOCALE = "--locale";

    @Before
    public void setUp() {
        mainClass = new Main();
        Main.setExecutor(executor);
    }

    @Test
    public void testMain() {
        String[] args = null;
        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(
                new Assertion() {
                    @Override
                    public void checkAssertion() throws Exception {
                        Mockito.verifyZeroInteractions(executor);
                        assertTrue(
                                "Error output should be clean, but it was " + mio.getSysErr(),
                                mio.getSysErr().isEmpty());
                        assertContains(helpText, mio.getSysOut());
                    }
                });
        Main.main(args);
    }

    @Test
    public void testWithNoArgs() {
        String[] args = {};
        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(
                new Assertion() {
                    @Override
                    public void checkAssertion() throws Exception {
                        Mockito.verifyZeroInteractions(executor);
                        assertTrue(
                                "Error output should be clean, but it was " + mio.getSysErr(),
                                mio.getSysErr().isEmpty());
                        assertContains(helpText, mio.getSysOut());
                    }
                });
        Main.main(args);
    }

    @Test
    public void testHelp() {
        String[] args = {"help"};
        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(
                new Assertion() {
                    @Override
                    public void checkAssertion() throws Exception {
                        Mockito.verifyZeroInteractions(executor);
                        assertTrue(
                                "Error output should be clean, but it was " + mio.getSysErr(),
                                mio.getSysErr().isEmpty());
                        assertContains(helpText, mio.getSysOut());
                    }
                });
        Main.main(args);
    }

    @Test
    public void testHelpWithExtraCommands() {
        String[] args = {"help", "dummy_string"};
        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(
                new Assertion() {
                    @Override
                    public void checkAssertion() throws Exception {
                        Mockito.verifyZeroInteractions(executor);
                        assertTrue(
                                "Error output should be clean, but it was " + mio.getSysErr(),
                                mio.getSysErr().isEmpty());
                        assertContains(helpText, mio.getSysOut());
                    }
                });
        Main.main(args);
    }

    @Test
    public void testScanExercise() throws NoLanguagePluginFoundException {
        final String exercisePath = getTargetPath("arith_funcs");
        final String outputPath = exercisePath + File.separator + "checkstyle.txt";

        Mockito.when(executor.scanExercise(Paths.get(exercisePath), "arith_funcs"))
                .thenReturn(
                        Optional.of(
                                new ExerciseDesc(
                                        "Name", ImmutableList.copyOf(new ArrayList<TestDesc>()))));
        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(
                new Assertion() {
                    @Override
                    public void checkAssertion() throws Exception {
                        Mockito.verify(executor)
                                .scanExercise(Paths.get(exercisePath), "arith_funcs");
                        assertContains(
                                "Exercises scanned successfully, results can be found in "
                                        + outputPath
                                        + "\n",
                                mio.getSysOut());
                        assertTrue(
                                "Error output should be clean, but it was " + mio.getSysErr(),
                                mio.getSysErr().isEmpty());
                    }
                });
        String[] args = {"scan-exercise", EXERCISE_PATH, exercisePath, OUTPUT_PATH, outputPath};
        Main.main(args);
    }

    @Test
    public void testRunTests() {
        final String exercisePath = getTargetPath("arith_funcs");
        final String outputPath = exercisePath + File.separator + "results.txt";
        String[] args = {"run-tests", EXERCISE_PATH, exercisePath, OUTPUT_PATH, outputPath};

        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(
                new Assertion() {
                    @Override
                    public void checkAssertion() throws Exception {
                        Mockito.verify(executor).runTests(Paths.get(exercisePath));
                        assertContains(
                                "Test results can be found in " + outputPath + "\n",
                                mio.getSysOut());
                        assertTrue(
                                "Error output should be clean, but it was " + mio.getSysErr(),
                                mio.getSysErr().isEmpty());
                    }
                });
        Main.main(args);
    }

    @Test
    public void testRunCheckCodeStyle() {
        final String exercisePath = getTargetPath("arith_funcs");
        final String outputPath = exercisePath + File.separator + "exercises.txt";
        String[] args = {
            "checkstyle", EXERCISE_PATH, exercisePath, OUTPUT_PATH, outputPath, LOCALE, "en"
        };

        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(
                new Assertion() {
                    @Override
                    public void checkAssertion() throws Exception {
                        Mockito.verify(executor)
                                .runCheckCodeStyle(Paths.get(exercisePath), new Locale("en"));
                        assertContains(
                                "Codestyle report can be found at " + outputPath + "\n",
                                mio.getSysOut());
                        assertTrue(
                                "Error output should be clean, but it was " + mio.getSysErr(),
                                mio.getSysErr().isEmpty());
                    }
                });
        Main.main(args);
    }

    @Test
    public void testPrepareStub() {
        String[] args = {"prepare-stub", EXERCISE_PATH, getTargetPath("arith_funcs")};
        final Path stubPath = new File(getTargetPath("arith_funcs")).toPath();

        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(
                new Assertion() {
                    @Override
                    public void checkAssertion() throws Exception {
                        //                        Mockito.verify(executor).prepareStub(stubPath);
                    }
                });
        Main.main(args);
    }

    @Test
    public void testPrepareSolution() {
        String[] args = {"prepareSolution", EXERCISE_PATH, getTargetPath("arith_funcs")};
        final Path solutionPath = new File(getTargetPath("arith_funcs")).toPath();

        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(
                new Assertion() {
                    @Override
                    public void checkAssertion() throws Exception {
                        // Why is this commented out?
                        // Mockito.verify(executor).prepareSolution(solutionPath);
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
        String targetPath = TestUtils.getPath(getClass(), location).toString();

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
