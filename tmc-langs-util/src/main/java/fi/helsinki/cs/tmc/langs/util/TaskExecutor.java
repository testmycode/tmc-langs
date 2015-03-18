package fi.helsinki.cs.tmc.langs.util;

import com.google.common.base.Optional;
import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;
import java.nio.file.Path;


public interface TaskExecutor {

    public void prepareSolution(Path path);

    public void prepareStub(Path path);

    public Optional<ValidationResult> runCheckCodeStyle(Path path);

    public Optional<RunResult> runTests(Path path);

    public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName);
    
}
