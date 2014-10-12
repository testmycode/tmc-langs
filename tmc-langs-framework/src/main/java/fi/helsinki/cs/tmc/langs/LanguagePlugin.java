package fi.helsinki.cs.tmc.langs;

import java.nio.file.Path;

/**
 * The interface that each language plugin must implement.
 */
public interface LanguagePlugin {
    /**
     * Returns the name of the programming language supported by this plugin.
     * @return The name of the language supported by this plugin.
     */
    public String getLanguageName();

    /**
     * Produces an exercise description of an exercise directory.
     *
     * <p>
     * This involves finding the test cases and the points offered by
     * the exercise.
     *
     * <p>
     * Must return null if the given path is not a valid exercise directory
     * for this langauge.
     *
     * @param path The path of the exercise directory.
     * @param exerciseName This must be set as the returned value's `name`.
     * @return The exercise description, or null if none.
     */
    public ExerciseDesc scanExercise(Path path, String exerciseName);

    /**
     * Returns test results for the exercise.
     *
     * <p>
     * This is called either by an IDE plugin or by the sandbox runner.
     *
     * @param path The path to the exercise directory.
     * @return The results of the run (not null).
     */
    public RunResult runExercise(Path path);

    /**
     * Prepares a submission for processing.
     *
     * <p>
     * Usually this picks source files from the submission and test and other
     * files from the original exercise.
     *
     * <p>
     * The destination path is initialized with the original exercise as it
     * appears in the course repository. The implementation shall copy over
     * files from the submission such that the student cannot easily replace
     * the tests.
     *
     * @param submissionPath A path to a directory where the submission has
     * been extracted. May be modified (e.g. by doing moves instead of copies).
     * @param destPath A path to a directory where the raw exercise has been
     * copied and where parts of the submission are to be copied.
     */
    public void prepareSubmission(Path submissionPath, Path destPath);

    /**
     * Prepares a stub exercise from the original.
     *
     * <p>
     * The stub is a copy of the original where the model solution and special
     * comments have been stripped and stubs like ('return 0') have been added.
     *
     * @param originalPath A path to the original exercise. May not be modified.
     * @param stubPath A path to an empty directory where the stub
     * should be assembled.
     */
    public void prepareStub(Path originalPath, Path stubPath);

    /**
     * Prepares a presentable solution from the original.
     *
     * <p>
     * The solution usually has stubs and special comments stripped.
     *
     * @param originalPath A path to the original exercise. May not be modified.
     * @param solutionPath A path to an empty directory where the solution
     * should be assembled.
     */
    public void prepareSolution(Path originalPath, Path solutionPath);

    /**
     * Prepares the sandbox from within during the build process.
     *
     * <p>
     * This is run inside the sandbox chroot.
     *
     * <p>
     * Guidelines:
     * <ul>
     *   <li>Do not repeat work that has been done. If you e.g. download
     *       something, check whether it's already been downloaded.
     *   <li>Place any temporary files in the /tmp/build directory.
     *       For instance, downloads should go there. These files are excluded
     *       from the sandbox image.
     *   <li>Do not depend on the execution order of language plugins.
     * </ul>
     */
    public void prepareSandboxChroot();
}
