package fi.helsinki.cs.tmc.langs.domain;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * A description of an exercise.
 */
public final class ExerciseDesc {

    /**
     * The name of the exercise to be shown to the user.
     *
     * <p>Does not necessarily match or even contain the directory name.
     */
    public final String name;

    /**
     * Descriptions of the tests that will be run for this exercise.
     */
    public final ImmutableList<TestDesc> tests;

    /**
     * Create a new ExerciseDesc comprising of a name and a list of tests.
     */
    public ExerciseDesc(String name, ImmutableList<TestDesc> tests) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(tests);
        this.name = name;
        this.tests = tests;
    }
}