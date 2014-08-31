package fi.helsinki.cs.tmc.langs;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public final class ExerciseDesc {
    public final String name;
    public final ImmutableList<TestDesc> tests;

    public ExerciseDesc(String name, ImmutableList<TestDesc> tests) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(tests);
        this.name = name;
        this.tests = tests;
    }
}
