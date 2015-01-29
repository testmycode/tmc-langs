package fi.helsinki.cs.tmc.langs;

import com.google.common.collect.ImmutableList;
import java.nio.file.Path;

/**
 * The interface that each language plug-in must implement.
 *
 * <p>
 * These implement the operations needed by the TMC server to support a
 * programming language. These are provided as a library to IDE plug-ins as a
 * convenience. IDE plug-ins often need additional integration work to support a
 * language properly. This interface does NOT attempt to provide everything that
 * an IDE plug-in might need to fully support a language.
 *
 * <p>
 * Parts of this interface may be called in a TMC sandbox.
 *
 * <p>
 * Implementations must be thread-safe and preferably fully stateless.
 * Users of this interface are free to cache results if needed.
 */
public interface LanguagePlugin {
    /**
     * Returns the name of the programming language supported by this plug-in.
     *
     * @return The name of the language supported by this plug-in.
     */
    public String getLanguageName();

    /**
     * Returns a list of all directories that contain an exercise in this
     * language.
     *
     * <p>
     * These directories might overlap with directories returned by some other
     * language plug-in.
     *
     * @param basePath The directory to search in.
     * @return A list of subdirectories. Never null.
     */
    public ImmutableList<Path> findExercises(Path basePath);

    /**
     * Produces an exercise description of an exercise directory.
     *
     * <p>
     * This involves finding the test cases and the points offered by
     * the exercise.
     *
     * <p>
     * Must return null if the given path is not a valid exercise directory
     * for this language.
     *
     * @param path The path of the exercise directory.
     * @param exerciseName This must be set as the name of the returned value.
     * @return The exercise description, or null if none.
     */
    public ExerciseDesc scanExercise(Path path, String exerciseName);

    /**
     * Runs the tests for the exercise.
     *
     * @param path The path to the exercise directory.
     * @return The results of the run. Never null.
     */
    public RunResult runTests(Path path);

    /**
     * Prepares a submission for processing in the sandbox.
     *
     * <p>
     * The destination path is initialised with the original exercise as it
     * appears in the course repository. The implementation should copy over
     * a selection of files from the submission so that the student cannot e.g.
     * easily replace the tests.
     *
     * <p>
     * TODO: make it easy for an implementation to take into account a possible
     * <tt>extra_student_files</tt> setting in a <tt>.tmcproject.yml</tt> file.
     * See http://tmc.mooc.fi/usermanual/pages/instructors.html#_tmcproject_yml
     *
     * @param submissionPath A path to a directory where the submission has
     * been extracted. May be modified (e.g. by doing moves instead of copies).
     * @param destPath A path to a directory where the original exercise has
     * been copied and where parts of the submission are to be copied.
     */
    public void prepareSubmission(Path submissionPath, Path destPath);

    /**
     * Prepares a stub exercise from the original.
     *
     * <p>
     * The stub is a copy of the original where the model solution and special
     * comments have been stripped and stubs like ('return 0') have been added.
     *
     * @param path A path to a directory where the original exercise has been
     * copied. This method should modify the contents of this directory.
     */
    public void prepareStub(Path path);

    /**
     * Prepares a presentable solution from the original.
     *
     * <p>
     * The solution usually has stubs and special comments stripped.
     *
     * @param path A path to a directory where the original exercise has been
     * copied. This method should modify the contents of this directory.
     */
    public void prepareSolution(Path path);
}
