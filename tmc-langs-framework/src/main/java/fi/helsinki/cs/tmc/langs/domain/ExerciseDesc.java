package fi.helsinki.cs.tmc.langs.domain;

import fi.helsinki.cs.tmc.testscanner.TestMethod;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;

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

    public static ExerciseDesc from(String name, List<TestMethod> testCases) {
        List<TestDesc> testsList = Lists.newArrayList();
        for (TestMethod testCase : testCases) {
            testsList.add(TestDesc.from(testCase));
        }
        ImmutableList<TestDesc> tests = ImmutableList.copyOf(testsList);

        return new ExerciseDesc(name, tests);
    }

    @Override
    public String toString() {
        return "<ExerciseDesc name: " + name + ", tests: " + tests + ">";
    }
}
