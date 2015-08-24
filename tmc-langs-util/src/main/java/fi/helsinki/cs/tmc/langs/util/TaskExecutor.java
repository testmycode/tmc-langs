package fi.helsinki.cs.tmc.langs.util;

import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;

import com.google.common.base.Optional;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Interface for calling different tasks of TMC-langs language plug-ins.
 *
 * <p>Implementations must provide the correct language plug-in for the given
 * exercises if one exists. And call the required task for the correct language
 * plug-in.
 *
 * @see fi.helsinki.cs.tmc.langs.LanguagePlugin
 */
public interface TaskExecutor {

    /**
     * Finds the correct language plug-in for the given exercise path. After which calls the
     * {@link fi.helsinki.cs.tmc.langs.LanguagePlugin#prepareSolution(java.nio.file.Path)
     * prepareSolution(Path path)} task.
     */
    void prepareSolution(Path path) throws NoLanguagePluginFoundException;

    /**
     * Finds the correct language plug-in for the given exercise path. After which calls the
     * {@link fi.helsinki.cs.tmc.langs.LanguagePlugin#prepareStub(java.nio.file.Path)
     * prepareStub(Path path)} task.
     */
    void prepareStub(Path path) throws NoLanguagePluginFoundException;

    /**
     * Finds the correct language plug-in for the given exercise path. After which calls the
     * {@link fi.helsinki.cs.tmc.langs.LanguagePlugin#checkCodeStyle(java.nio.file.Path)
     * checkCodeStyle(Path path)} task.
     */
    ValidationResult runCheckCodeStyle(Path path) throws NoLanguagePluginFoundException;

    /**
     * Finds the correct language plug-in for the given exercise path. After which calls the
     * {@link fi.helsinki.cs.tmc.langs.LanguagePlugin#runTests(java.nio.file.Path)
     * runTests(Path path)} task.
     */
    RunResult runTests(Path path) throws NoLanguagePluginFoundException;

    /**
     * Finds the correct language plug-in for the given exercise path. After which calls the
     * {@link fi.helsinki.cs.tmc.langs.LanguagePlugin#scanExercise(java.nio.file.Path, String)
     * scanExercise(Path path, String exerciseName)} task.
     */
    Optional<ExerciseDesc> scanExercise(Path path, String exerciseName)
            throws NoLanguagePluginFoundException;

    /**
     * Figures out if this path contains any exercise that TMC-langs can process.
     */
    boolean isExerciseRootDirectory(Path path);

    /**
     * Extract a given archive file containing a compressed project to a target location.
     *
     * <p>This will overwrite any existing files as long as they are not specified as StudentFiles
     * by the language dependent {@link fi.helsinki.cs.tmc.langs.io.StudentFilePolicy}.
     *
     * @param compressedProject A path to the compressed archive.
     * @param targetLocation Location where the archive should be extracted to
     */
    void extractProject(Path compressedProject, Path targetLocation) throws IOException;

    /**
     * Extract a given archive file containing a compressed project to a target location.
     *
     * <p>This will overwrite any existing files as long as they are not specified as StudentFiles
     * by the language dependent {@link fi.helsinki.cs.tmc.langs.io.StudentFilePolicy}.
     *
     * @param compressedProject A path to the compressed archive.
     * @param targetLocation Location where the archive should be extracted to
     * @param overwriteEverything A boolean that tells whether the extraction should overwrite all
     *     the files.
     */
    void extractProject(Path compressedProject, Path targetLocation, boolean overwriteEverything)
            throws IOException;

    /**
     * Compresses a project, creating a zip that can be sent to the TMC server as a submission.
     */
    byte[] compressProject(Path path) throws IOException, NoLanguagePluginFoundException;
}
