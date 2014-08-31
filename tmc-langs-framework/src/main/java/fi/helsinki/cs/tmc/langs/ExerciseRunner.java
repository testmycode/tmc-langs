package fi.helsinki.cs.tmc.langs;

import java.nio.file.Path;

/**
 * Runs the test suite of the exercise.
 */
public interface ExerciseRunner {
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
}
