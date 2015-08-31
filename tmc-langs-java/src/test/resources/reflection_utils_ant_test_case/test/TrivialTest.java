
import fi.helsinki.cs.tmc.edutestutils.Points;
import fi.helsinki.cs.tmc.edutestutils.ReflectionUtils;
import org.junit.Test;
import static org.junit.Assert.*;

@Points("trivial")
public class TrivialTest {
    @Test
    public void testF() {
        ReflectionUtils.newInstanceOfClass("Trivial");
        assertTrue(true);
    }
}
