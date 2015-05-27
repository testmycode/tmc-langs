package fi.helsinki.cs.tmc.langs.util;

import com.google.common.base.Optional;

import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.LanguagePlugin;
import fi.helsinki.cs.tmc.langs.NoLanguagePluginFoundException;
import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;

import java.nio.file.Path;

public class TaskExecutorImpl implements TaskExecutor {

    @Override
    public ValidationResult runCheckCodeStyle(Path path) throws NoLanguagePluginFoundException {
        return getLanguagePlugin(path).checkCodeStyle(path);
    }

    @Override
    public RunResult runTests(Path path) throws NoLanguagePluginFoundException {
        return getLanguagePlugin(path).runTests(path);
    }

    @Override
    public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName) throws NoLanguagePluginFoundException {
        Optional<ExerciseDesc> result = getLanguagePlugin(path).scanExercise(path, exerciseName);
        if (result.isPresent()) {
            return result;
        }

        return Optional.absent();
    }

    @Override
    public void prepareStub(Path path) throws NoLanguagePluginFoundException {
        getLanguagePlugin(path).prepareStub(path);
    }

    @Override
    public void prepareSolution(Path path) throws NoLanguagePluginFoundException {
        getLanguagePlugin(path).prepareSolution(path);
    }

    /**
     * Get language plugin for the given path.
     *
     * @param path of the exercise.
     * @return Language Plugin that recognises the exercise.
     */
    private LanguagePlugin getLanguagePlugin(Path path) throws NoLanguagePluginFoundException {
        return ProjectTypeHandler.getLanguagePlugin(path);
    }
}
