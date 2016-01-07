package fi.helsinki.cs.tmc.langs.domain;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Represents configuration based on which submission may be packaged.
 */
@Beta
public class ExercisePackagingConfiguration {

    /**
     * Student folders or files which are copied from submission.
     */
    public ImmutableList<String> studentFilePaths;
    /**
     * Exercise folders or files which are copied from exercise template or clone.
     */
    public ImmutableList<String> exerciseFilePaths;

    public ExercisePackagingConfiguration(
            ImmutableList<String> studentFilePaths, ImmutableList<String> exerciseFilePaths) {
        this.studentFilePaths = studentFilePaths;
        this.exerciseFilePaths = exerciseFilePaths;
    }
}
