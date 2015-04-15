package fi.helsinki.cs.tmc.langs.maven;

import com.google.common.base.Optional;
import fi.helsinki.cs.tmc.langs.AbstractLanguagePlugin;
import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.RunResult;
import java.io.File;
import java.nio.file.Path;
import java.util.logging.Logger;

public class MavenPlugin extends AbstractLanguagePlugin {

    @Override
    protected boolean isExerciseTypeCorrect(Path path) {
        return new File(path.toString() + File.separatorChar + "pom.xml").exists();
    }

    @Override
    public String getLanguageName() {
        return "apache-maven";
    }

    @Override
    public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RunResult runTests(Path path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
