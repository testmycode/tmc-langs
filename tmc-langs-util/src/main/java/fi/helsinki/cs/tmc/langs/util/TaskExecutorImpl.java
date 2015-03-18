package fi.helsinki.cs.tmc.langs.util;

import com.google.common.base.Optional;
import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.LanguagePlugin;
import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;

import java.nio.file.Path;

public class TaskExecutorImpl implements TaskExecutor {

    @Override
    public Optional<ValidationResult> runCheckCodeStyle(Path path) {
        Optional<LanguagePlugin> languagePlugin = ProjectTypeHandler.getLanguagePlugin(path);

        if (languagePlugin.isPresent()) {
            return Optional.of(languagePlugin.get().checkCodeStyle(path));
        }
        return Optional.absent();
    }

    @Override
    public Optional<RunResult> runTests(Path path) {
        Optional<LanguagePlugin> languagePlugin = ProjectTypeHandler.getLanguagePlugin(path);

        if (languagePlugin.isPresent()) {
            return Optional.of(languagePlugin.get().runTests(path));
        }
        return Optional.absent();
    }

    @Override
    public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName) {
        Optional<LanguagePlugin> languagePlugin = ProjectTypeHandler.getLanguagePlugin(path);

        if (languagePlugin.isPresent()) {
            return Optional.of(languagePlugin.get().scanExercise(path, exerciseName));
        }
        return Optional.absent();
    }

    @Override
    public void prepareStub(Path path) {
        Optional<LanguagePlugin> languagePlugin = ProjectTypeHandler.getLanguagePlugin(path);

        if (languagePlugin.isPresent()) {
            languagePlugin.get().prepareStub(path);
        }
    }

    @Override
    public void prepareSolution(Path path) {
        Optional<LanguagePlugin> languagePlugin = ProjectTypeHandler.getLanguagePlugin(path);

        if (languagePlugin.isPresent()) {
            languagePlugin.get().prepareSolution(path);
        }
    }
}
