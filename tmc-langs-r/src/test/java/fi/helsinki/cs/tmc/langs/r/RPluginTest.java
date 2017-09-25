/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.helsinki.cs.tmc.langs.r;

import org.junit.Test;

/**
 *
 * @author janne
 */
public class RPluginTest {
    
    @Test
    public void testGetAvailablePointsCommand(){
        String[] command = new String[] {"Rscript", "-e","\"library('tmcRtestrunner');getAvailablePoints(\"$PWD\")\""};
    
    }
    
    
}
