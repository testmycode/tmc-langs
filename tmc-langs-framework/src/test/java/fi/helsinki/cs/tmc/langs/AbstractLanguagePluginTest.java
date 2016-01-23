package fi.helsinki.cs.tmc.langs;

import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.ExerciseBuilder;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.io.sandbox.StudentFileAwareSubmissionProcessor;
import fi.helsinki.cs.tmc.langs.io.sandbox.SubmissionProcessor;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AbstractLanguagePluginTest {

    private class StubLanguagePlugin extends AbstractLanguagePlugin {

        public StubLanguagePlugin(
                ExerciseBuilder exerciseBuilder, SubmissionProcessor submissionProcessor) {
            super(exerciseBuilder, submissionProcessor, null, null);
        }

        @Override
        public boolean isExerciseTypeCorrect(Path path) {
            return Files.exists(path.resolve("build.xml"));
        }

        @Override
        protected StudentFilePolicy getStudentFilePolicy(Path projectPath) {
            return null;
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
        public ValidationResult checkCodeStyle(Path path, Locale locale) {
            return null;
        }

        @Override
        public String getPluginName() {
            return null;
        }

        @Override
        public void maybeCopySharedStuff(Path destPath) {
            // Nothing to do!
        }

        @Override
        public void clean(Path path) {}
    }

    private LanguagePlugin plugin;
    private ExerciseBuilder exerciseBuilder;
    private SubmissionProcessor submissionProcessor;

    @Before
    public void setUp() {
        exerciseBuilder = mock(ExerciseBuilder.class);
        submissionProcessor = mock(StudentFileAwareSubmissionProcessor.class);
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
        Map<Path, LanguagePlugin> exerciseMap = new HashMap<>();
        exerciseMap.put(path, null);
        plugin.prepareStubs(exerciseMap, path, path);

        verify(exerciseBuilder).prepareStubs(exerciseMap, path, path);
    }

    @Test
    public void prepareSolutionDelegatesRequestToExerciseBuilder() {
        Path path = Paths.get("testPath");
        Map<Path, LanguagePlugin> exerciseMap = new HashMap<>();
        exerciseMap.put(path, null);
        plugin.prepareSolutions(exerciseMap, path, path);

        verify(exerciseBuilder).prepareSolutions(exerciseMap, path, path);
    }

    @Test
    public void prepareSubmissionDelegatesRequestToSubmissionProcessor() {
        Path source = Paths.get("source");
        Path target = Paths.get("target");

        plugin.prepareSubmission(source, target);
        verify(submissionProcessor).moveFiles(source, target);
    }
}
