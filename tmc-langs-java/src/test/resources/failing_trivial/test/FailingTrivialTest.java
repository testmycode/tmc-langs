
import fi.helsinki.cs.tmc.edutestutils.Points;
import org.junit.Test;
import static org.junit.Assert.*;

@Points("trivial")
public class FailingTrivialTest {
    @Test
    public void testF() {
        FailingTrivial t = new FailingTrivial();
        assertEquals(1, t.f());
    }
}
