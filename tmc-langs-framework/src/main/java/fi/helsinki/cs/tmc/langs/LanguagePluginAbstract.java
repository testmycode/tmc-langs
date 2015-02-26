/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.helsinki.cs.tmc.langs;

import java.nio.file.Path;

public abstract class LanguagePluginAbstract implements LanguagePlugin {
    
    /**
      * Exercisebuilder uses an instance because it is somewhat likely
      * that it will need some language specific configuration
     */
    
    private ExerciseBuilder exerciseBuilder = new ExerciseBuilder();

    @Override
    public String getLanguageName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void prepareSubmission(Path submissionPath, Path destPath) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void prepareStub(Path path) {
        exerciseBuilder.prepareStub(path);
    }

    @Override
    public void prepareSolution(Path path) {
        exerciseBuilder.prepareSolution(path);
    }

    @Override
    public boolean isExerciseTypeCorrect(Path path) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
