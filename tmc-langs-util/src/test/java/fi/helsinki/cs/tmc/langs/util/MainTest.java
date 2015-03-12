/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.helsinki.cs.tmc.langs.util;

import fi.helsinki.cs.tmc.edutestutils.MockStdio;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @author jviding
 */
public class MainTest {
    
    @Rule
    public MockStdio mio = new MockStdio();
    
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        Main.main(args);
        String expOutput = "Usage: TODO: Write instructions here.";
        assertEquals(expOutput, mio.getSysOut());
    }
    
}
