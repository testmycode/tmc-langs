package fi.helsinki.cs.tmc.langs;

import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import fi.helsinki.cs.tmc.langs.domain.ExerciseBuilder;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.sandbox.SubmissionProcessor;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class AbstractLanguagePluginTest {

    private class StubLanguagePlugin extends AbstractLanguagePlugin {

        public StubLanguagePlugin(ExerciseBuilder exerciseBuilder,
                                  SubmissionProcessor submissionProcessor) {
            super(exerciseBuilder, submissionProcessor);
        }

        @Override
        protected boolean isExerciseTypeCorrect(Path path) {
            return path.resolve("build.xml").toFile().exists();
        }

        @Override
        public String getLanguageName() {
            return null;
        }

        @Override
        public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName) {
            return null;
        }

        @Override
        public RunResult runTests(Path path) {
            return null;
        }

        @Override
        public ValidationResult checkCodeStyle(Path path) {
            return null;
        }
    }

    private LanguagePlugin plugin;
    private ExerciseBuilder exerciseBuilder;
    private SubmissionProcessor submissionProcessor;

    @Before
    public void setUp() {
        exerciseBuilder = mock(ExerciseBuilder.class);
        submissionProcessor = mock(SubmissionProcessor.class);
        plugin = new StubLanguagePlugin(exerciseBuilder, submissionProcessor);
    }

    @Test
    public void findExercisesReturnsAListOfExerciseDirectories() {
        Path project = TestUtils.getPath(getClass(), "ant_project");

        ImmutableList<Path> dirs = plugin.findExercises(project);

        Path pathOne = TestUtils.getPath(getClass(), "ant_project");
        Path pathTwo = TestUtils.getPath(getClass(), "ant_project/ant_sub_project");

        assertTrue(dirs.contains(pathOne) && dirs.contains(pathTwo));
    }

    @Test
    public void findExercisesReturnsAnEmptyListWhenInvalidPath() {
        Path buildFile = TestUtils.getPath(getClass(), "ant_project/build.xml");

        assertTrue(plugin.findExercises(buildFile).isEmpty());
    }

    @Test
    public void findExercisesReturnsAnEmptyListWhenTargetDoesNotExist() {
        Path buildFile = TestUtils.getPath(getClass(), "");
        buildFile = buildFile.resolve("no-such-directory");

        assertTrue(plugin.findExercises(buildFile).isEmpty());
    }

    @Test
    public void findExercisesReturnsAnEmptyListWhenNoExercisesFound() {
        Path project = TestUtils.getPath(getClass(), "dummy_project");

        assertTrue(plugin.findExercises(project).isEmpty());
    }

    @Test
    public void prepareStubDelegatesRequestToExerciseBuilder() {
        Path path = Paths.get("testPath");
        plugin.prepareStub(path);

        verify(exerciseBuilder).prepareStub(path);
    }

    @Test
    public void prepareSolutionDelegatesRequestToExerciseBuilder() {
        Path path = Paths.get("testPath");
        plugin.prepareSolution(path);

        verify(exerciseBuilder).prepareSolution(path);
    }

    @Test
    public void prepareSubmissionDelegatesRequestToSubmissionProcessor() {
        Path source = Paths.get("source");
        Path target = Paths.get("target");

        plugin.prepareSubmission(source, target);
        verify(submissionProcessor).moveFiles(source, target);
    }

    @Test
    public void hasConstructorWithNoArguments() {
        new AbstractLanguagePlugin() {
            @Override
            protected boolean isExerciseTypeCorrect(Path path) {
                return false;
            }

            @Override
            public String getLanguageName() {
                return null;
            }

            @Override
            public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName) {
                return null;
            }

            @Override
            public RunResult runTests(Path path) {
                return null;
            }

            @Override
            public ValidationResult checkCodeStyle(Path path) {
                return null;
            }
        };
    }

    @Test
    public void hasConstructorThatTakesOnlyAnExerciseBuilderAsParameter() {
        new AbstractLanguagePlugin(new ExerciseBuilder()) {
            @Override
            protected boolean isExerciseTypeCorrect(Path path) {
                return false;
            }

            @Override
            public String getLanguageName() {
                return null;
            }

            @Override
            public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName) {
                return null;
            }

            @Override
            public RunResult runTests(Path path) {
                return null;
            }

            @Override
            public ValidationResult checkCodeStyle(Path path) {
                return null;
            }
        };
    }

    @Test
    public void hasConstructorThatTakesOnlyAnSubmissionProcessorAsParameter() {
        new AbstractLanguagePlugin(new SubmissionProcessor()) {
            @Override
            protected boolean isExerciseTypeCorrect(Path path) {
                return false;
            }

            @Override
            public String getLanguageName() {
                return null;
            }

            @Override
            public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName) {
                return null;
            }

            @Override
            public RunResult runTests(Path path) {
                return null;
            }

            @Override
            public ValidationResult checkCodeStyle(Path path) {
                return null;
            }
        };
    }
}
