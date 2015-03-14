package fi.helsinki.cs.tmc.langs.util;

import fi.helsinki.cs.tmc.edutestutils.MockStdio;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

public class MainTest {
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
    
    @Rule
    public MockStdio mio = new MockStdio();
    
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        exit.expectSystemExitWithStatus(0);
        Main.main(args);
        String expOutput = "Usage: TODO: Write instructions here.";
        assertEquals(expOutput, mio.getSysOut());
    }
    
}
