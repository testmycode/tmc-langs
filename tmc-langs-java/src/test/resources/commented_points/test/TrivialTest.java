
import fi.helsinki.cs.tmc.edutestutils.Points;
import org.junit.Test;
import static org.junit.Assert.*;

public class TrivialTest {
    @Points("1")
    @Test
    public void testF() {
        Trivial t = new Trivial();
        assertEquals(1, t.f());
    }

    @Test
   // @Points("2")
    public void testF() {
        Trivial t = new Trivial();
        assertEquals(1, t.f());
    }
    /*
    @Test
    @Points("3ö")
    public void testF() {
        Trivial t = new Trivial();
        assertEquals(1, t.f());*** ..
    }
    */

    @Test
    @Points("//4")
    public void testF() {
        Trivial t = new Trivial();
        assertEquals(1, t.f());
    }

    @Test @Points("/*5")
    public void testF() {
        /*
        Trivial t = new Trivial();
        assertEquals(1, t.f());
        */
    }

    @Test // @Points("6")
    public void testF() {
        Trivial t = new Trivial();
        assertEquals(1, t.f());
    }
}
