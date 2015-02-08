/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.helsinki.cs.tmc.langs;

import java.nio.file.Path;

/**
 *
 * @author TMC-langs
 */
public abstract class LanguagePluginAbstract implements LanguagePlugin {

    protected final String languageName;

    /**
     * 
     * @param languageName The name of the language as given in the extending class.
     */
    public LanguagePluginAbstract(String languageName) {
        this.languageName = languageName;
    }
           
    @Override
    public String getLanguageName() {
       return languageName;
    }
    
    @Override
        public void prepareSubmission(Path submissionPath, Path destPath) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
        public void prepareStub(Path path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
        public void prepareSolution(Path path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isExerciseTypeCorrect(Path path) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
