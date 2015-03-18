package fi.helsinki.cs.tmc.langs.maven;

import fi.helsinki.cs.tmc.langs.AbstractLanguagePlugin;
import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.RunResult;
import java.io.File;
import java.nio.file.Path;
import java.util.logging.Logger;

public class MavenPlugin extends AbstractLanguagePlugin {

    private static final Logger log = Logger.getLogger(MavenPlugin.class.getName());

    @Override
    protected boolean isExerciseTypeCorrect(Path path) {
        return new File(path.toString() + File.separatorChar + "pom.xml").exists();
    }

    @Override
    public String getLanguageName() {
        return "apache-maven";
    }

    @Override
    public ExerciseDesc scanExercise(Path path, String exerciseName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RunResult runTests(Path path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
