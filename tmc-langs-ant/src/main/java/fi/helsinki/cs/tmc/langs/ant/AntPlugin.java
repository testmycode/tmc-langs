package fi.helsinki.cs.tmc.langs.ant;

import com.google.common.collect.ImmutableList;
import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.LanguagePluginAbstract;
import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Logger;

public class AntPlugin extends LanguagePluginAbstract {

    private static final Logger log = Logger.getLogger(AntPlugin.class.getName());


    @Override
    public String getLanguageName() {
        return "apache-ant";
    }

    @Override
    public ImmutableList<Path> findExercises(Path basePath) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ExerciseDesc scanExercise(Path path, String exerciseName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RunResult runTests(Path path) {
        ArrayList<String> args = new ArrayList<>();
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isExerciseTypeCorrect(Path path) {
        return new File(path.toString() + File.separatorChar + "build.xml").exists();
    }

    @Override
    public ValidationResult checkCodeStyle(Path path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
