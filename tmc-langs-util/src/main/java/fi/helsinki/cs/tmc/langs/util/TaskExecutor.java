package fi.helsinki.cs.tmc.langs.util;

import com.google.common.base.Optional;
import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.NoLanguagePluginFoundException;
import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;

import java.nio.file.Path;

/**
 * Interface for calling different tasks of TMC-langs language plug-ins.
 *
 * <p>
 * Implementations must provide the correct language plug-in for the given
 * exercises if one exists. And call the required task for the correct language
 * plug-in.
 *
 * @see fi.helsinki.cs.tmc.langs.LanguagePlugin
 */
public interface TaskExecutor {

    /**
     * Finds the correct language plug-in for the given exercise path. After
     * which calls the
     * {@link fi.helsinki.cs.tmc.langs.LanguagePlugin#prepareSolution(java.nio.file.Path) prepareSolution(Path path)}
     * task.
     */
    public void prepareSolution(Path path) throws NoLanguagePluginFoundException;

    /**
     * Finds the correct language plug-in for the given exercise path. After
     * which calls the
     * {@link fi.helsinki.cs.tmc.langs.LanguagePlugin#prepareStub(java.nio.file.Path) prepareStub(Path path)}
     * task.
     */
    public void prepareStub(Path path) throws NoLanguagePluginFoundException;

    /**
     * Finds the correct language plug-in for the given exercise path. After
     * which calls the
     * {@link fi.helsinki.cs.tmc.langs.LanguagePlugin#checkCodeStyle(java.nio.file.Path) checkCodeStyle(Path path)}
     * task.
     */
    public ValidationResult runCheckCodeStyle(Path path) throws NoLanguagePluginFoundException;

    /**
     * Finds the correct language plug-in for the given exercise path. After
     * which calls the
     * {@link fi.helsinki.cs.tmc.langs.LanguagePlugin#runTests(java.nio.file.Path) runTests(Path path)}
     * task.
     */
    public RunResult runTests(Path path) throws NoLanguagePluginFoundException;

    /**
     * Finds the correct language plug-in for the given exercise path. After
     * which calls the
     * {@link fi.helsinki.cs.tmc.langs.LanguagePlugin#scanExercise(java.nio.file.Path, String) scanExercise(Path path, String exerciseName)}
     * task.
     */
    public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName) throws NoLanguagePluginFoundException;

}
