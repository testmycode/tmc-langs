/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.helsinki.cs.tmc.langs;

import com.google.common.collect.ImmutableList;
import java.nio.file.Path;

/**
 *
 * @author TMC-langs
 */
public abstract class LanguagePluginAbstract implements LanguagePlugin {

    protected final String languageName;

    /**
     * 
     * @param languageName The name of the language as given in the extending class
     */
    public LanguagePluginAbstract(String languageName) {
        this.languageName = languageName;
    }
           
    @Override
    public String getLanguageName() {
       return languageName;
    }
    
    @Override
    public ImmutableList<Path> findExercises(Path basePath) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
