package fi.helsinki.cs.tmc.langs.ant;

import com.google.common.collect.ImmutableList;
import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.LanguagePluginAbstract;
import fi.helsinki.cs.tmc.langs.RunResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

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
        if (!isExerciseTypeCorrect(path)) {
            throw new RuntimeException("Project has no build.xml");
        } else {
            build(path);

            // Run tests, needs relocating
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private void build(Path path) {
        Project project = new Project();
        File buildFile = new File(path.toString() + File.separatorChar + "build.xml");
        project.setUserProperty("ant.file", buildFile.getAbsolutePath());
        project.init();

        ProjectHelper helper = ProjectHelper.getProjectHelper();
        project.addReference("ant.projectHelper", helper);
        helper.parse(project, buildFile);

        project.executeTarget(project.getDefaultTarget());
    }

    @Override
    public boolean isExerciseTypeCorrect(Path path) {
        return new File(path.toString() + File.separatorChar + "build.xml").exists();
    }

}
