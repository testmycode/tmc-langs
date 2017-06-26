package fi.helsinki.cs.tmc.langs.util;

import fi.helsinki.cs.tmc.langs.LanguagePlugin;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.ExercisePackagingConfiguration;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;
import fi.helsinki.cs.tmc.langs.domain.RunResult;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;

import org.apache.commons.compress.archivers.ArchiveException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

/**
 * Interface for calling different tasks of TMC-langs language plug-ins.
 *
 * <p>Implementations must provide the correct language plug-in for the given
 * exercises if one exists. And call the required task for the correct language
 * plug-in.
 *
 * @see fi.helsinki.cs.tmc.langs.LanguagePlugin
 */
@Beta
public interface TaskExecutor {

    /**
     * Finds the correct language plug-in for the given exercise path. After which calls the
     * {@link fi.helsinki.cs.tmc.langs.LanguagePlugin#prepareSolution(java.nio.file.Path)
     * prepareSolution(Path path)} task.
     */
    void prepareSolutions(Map<Path, LanguagePlugin> exerciseMap, Path repoPath, Path destPath)
            throws NoLanguagePluginFoundException;

    /**
     * Finds the correct language plug-in for the given exercise path. After which calls the
     * {@link fi.helsinki.cs.tmc.langs.LanguagePlugin#prepareStub(java.nio.file.Path)
     * prepareStub(Path path)} task.
     */
    void prepareStubs(Map<Path, LanguagePlugin> exerciseMap, Path repoPath, Path destPath)
            throws NoLanguagePluginFoundException;

    /**
     * Finds the correct language plug-in for the given exercise path. After which calls the
     * {@link fi.helsinki.cs.tmc.langs.LanguagePlugin#checkCodeStyle(java.nio.file.Path)
     * checkCodeStyle(Path path)} task.
     */
    ValidationResult runCheckCodeStyle(Path path, Locale locale)
            throws NoLanguagePluginFoundException;

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
     * Extract a given archive file containing a compressed project to a target location.
     *
     * <p>This will overwrite all files, even when specified as student files. Similar to
     * {@link fi.helsinki.cs.tmc.langs.util.TaskExecutor#extractProject(Path, Path, boolean)}
     * but more implicit naming
     *
     * @param compressedProject A path to the compressed archive.
     * @param targetLocation Location where the archive should be extracted to
     */
    void extractAndRewriteEveryhing(Path compressedProject, Path targetLocation) throws IOException;

    /**
     * Compresses a project, creating a zip that can be sent to the TMC server as a submission.
     */
    byte[] compressProject(Path path) throws IOException, NoLanguagePluginFoundException;

    ExercisePackagingConfiguration getExercisePackagingConfiguration(Path path)
            throws NoLanguagePluginFoundException;

    /**
     * Creates a tarball that can be submitted to TMC-sandbox.
     * The tar is created to the target location
     * 
     * @param projectDir Location of the unzipped project
     * @param tmcLangs Location of tmc-langs-cli.jar
     * @param tmcrun Location of tmc-run init script
     * @param targetLocation Location where the tar archive should be extracted to
     */
    void compressTarForSubmitting(Path projectDir, Path tmcLangs, Path tmcrun, Path targetLocation)
            throws IOException, ArchiveException;

    /**
     * Run clean for given path using proper language plugin.
     */
    void clean(Path path) throws NoLanguagePluginFoundException;
}
