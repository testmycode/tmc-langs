package fi.helsinki.cs.maventest;

import fi.helsinki.cs.tmc.edutestutils.MockStdio;
import fi.helsinki.cs.tmc.edutestutils.Points;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;

@Points("class-point")
public class AppTest {
    @Rule
    public MockStdio mio = new MockStdio();

    @Test
    @Points("same-point")
    public void trol() {
        App.main(null);
        assertTrue(true);
    }

    @Test
    @Points("same-point")
    public void trolfail() {
        App.main(null);
        assertTrue(false);
    }
}
