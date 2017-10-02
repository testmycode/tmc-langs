package fi.helsinki.cs.tmc.langs;

import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.ExercisePackagingConfiguration;
import fi.helsinki.cs.tmc.langs.domain.RunResult;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

/**
 * The interface that each language plug-in must implement.
 *
 * <p>These implement the operations needed by the TMC server to support a
 * programming language. These are provided as a library to IDE plug-ins as a
 * convenience. IDE plug-ins often need additional integration work to support a
 * language properly. This interface does NOT attempt to provide everything that
 * an IDE plug-in might need to fully support a language.
 *
 * <p>Parts of this interface may be called in a TMC sandbox.
 *
 * <p>Implementations must be thread-safe and preferably fully stateless. Users of
 * this interface are free to cache results if needed.
 */
public interface LanguagePlugin {

    /**
     * Returns the name of the plug-in.
     */
    String getPluginName();

    /**
     * Returns the name of the programming language supported by this plug-in.
     */
    @Deprecated
    String getLanguageName();

    /**
     * Returns a list of all directories that contain an exercise in this
     * language.
     *
     * <p>These directories might overlap with directories returned by some other
     * language plug-in.
     *
     * @param basePath The directory to search in.
     * @return A list of subdirectories. Never null.
     */
    ImmutableList<Path> findExercises(Path basePath);

    /**
     * Produces an exercise description of an exercise directory.
     *
     * <p>This involves finding the test cases and the points offered by the
     * exercise.
     *
     * <p>Must return null if the given path is not a valid exercise directory for
     * this language.
     *
     * @param path The path of the exercise directory.
     * @param exerciseName This must be set as the name of the returned exercise.
     * @return The exercise description, or Optional absent if none.
     */
    Optional<ExerciseDesc> scanExercise(Path path, String exerciseName);

    /**
     * Runs the tests for the exercise.
     *
     * @param path The path to the exercise directory.
     * @return The results of the run. Never null.
     */
    RunResult runTests(Path path);

    /**
     * Prepares a submission for processing in the sandbox.
     *
     * <p>The destination path is initialised with the original exercise as it
     * appears in the course repository. The implementation should copy over a
     * selection of files from the submission so that the student cannot e.g.
     * easily replace the tests.
     *
     * <p>TODO: make it easy for an implementation to take into account a possible
     * <tt>extra_student_files</tt> setting in a <tt>.tmcproject.yml</tt> file.
     * See http://tmc.mooc.fi/usermanual/pages/instructors.html#_tmcproject_yml
     *
     * @param submissionPath A path to a directory where the submission has been
     *     extracted. May be modified (e.g. by doing moves instead of copies).
     * @param destPath A path to a directory where the original exercise has
     *     been copied and where parts of the submission are to be copied.
     */
    void prepareSubmission(Path submissionPath, Path destPath);

    /**
     * Prepares a stub exercise from the original.
     *
     * <p>The stub is a copy of the original where the model solution and special
     * comments have been stripped and stubs like ('return 0') have been added.
     *
     * @param exerciseMap consists of exercise locations and which plugin belongs to it.
     * @param destPath path to which directory with prepared files will be copied.
     */
    void prepareStubs(Map<Path, LanguagePlugin> exerciseMap, Path repoPath, Path destPath);

    /**
     * Prepares a presentable solution from the original.
     *
     * <p>The solution usually has stubs and special comments stripped.
     *
     * @param clonePath path in which the original exercise is located.
     * @param destPath path to which directory with prepared files will be copied.
     */
    void prepareSolutions(Map<Path, LanguagePlugin> exerciseMap, Path repoPath, Path destPath);

    /**
     * Run checkstyle or similar plugin to project if applicable
     *
     * @param path The path to the exercise directory.
     * @param locale Locale to use for code style messages.
     * @return Validation result of the checkstyle ran, or null if not
     *     applicable
     */
    ValidationResult checkCodeStyle(Path path, Locale messageLocale)
            throws UnsupportedOperationException;

    /**
     * Compress a given project so that it can be sent to the TestMyCode server.
     *
     * @param path Path to the root of the project.
     * @return The compressed file as a byte array.
     */
    byte[] compressProject(Path path) throws IOException;

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
     * Tells if there's a valid exercise in this path.
     * @param path The path to the exercise directory.
     * @return True if given path is valid directory for this language plugin
     */
    boolean isExerciseTypeCorrect(Path path);

    /**
     * Copy shared stuff to stub or solution used for example for copying tmc-junit-runner.
     *
     * @param destPath path of the project.
     */
    public void maybeCopySharedStuff(Path destPath);

    /**
     * Returns configuration which is used to package submission on tmc-server.
     */
    @Beta
    public ExercisePackagingConfiguration getExercisePackagingConfiguration(Path path);

    /**
     * Runs clean command e.g {@code make clean} for make or {@code mvn clean} for maven.
     */
    void clean(Path path);
}
