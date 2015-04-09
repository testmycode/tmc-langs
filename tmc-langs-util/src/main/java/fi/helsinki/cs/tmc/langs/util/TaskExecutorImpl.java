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
        Optional<LanguagePlugin> languagePlugin = ProjectTypeHandler.getLanguagePlugin(path);

        if (languagePlugin.isPresent()) {
            return languagePlugin.get().checkCodeStyle(path);
        }

        throw new NoLanguagePluginFoundException("No suitable language plugin found.");
    }

    @Override
    public RunResult runTests(Path path) throws NoLanguagePluginFoundException {
        Optional<LanguagePlugin> languagePlugin = ProjectTypeHandler.getLanguagePlugin(path);

        if (languagePlugin.isPresent()) {
            return languagePlugin.get().runTests(path);
        }

        throw new NoLanguagePluginFoundException("No suitable language plugin found.");
    }

    @Override
    public ExerciseDesc scanExercise(Path path, String exerciseName) throws NoLanguagePluginFoundException {
        Optional<LanguagePlugin> languagePlugin = ProjectTypeHandler.getLanguagePlugin(path);

        if (languagePlugin.isPresent()) {
            return languagePlugin.get().scanExercise(path, exerciseName);
        }

        throw new NoLanguagePluginFoundException("No suitable language plugin found.");
    }

    @Override
    public void prepareStub(Path path) throws NoLanguagePluginFoundException {
        Optional<LanguagePlugin> languagePlugin = ProjectTypeHandler.getLanguagePlugin(path);

        if (languagePlugin.isPresent()) {
            languagePlugin.get().prepareStub(path);
        } else {
            throw new NoLanguagePluginFoundException("No suitable language plugin found.");
        }
    }

    @Override
    public void prepareSolution(Path path) throws NoLanguagePluginFoundException {
        Optional<LanguagePlugin> languagePlugin = ProjectTypeHandler.getLanguagePlugin(path);

        if (languagePlugin.isPresent()) {
            languagePlugin.get().prepareSolution(path);
        } else {
            throw new NoLanguagePluginFoundException("No suitable language plugin found.");
        }
    }
}
