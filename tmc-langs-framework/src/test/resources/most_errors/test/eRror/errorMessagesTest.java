package eRror;

import fi.helsinki.cs.tmc.edutestutils.Points;
import static org.junit.Assert.*;
import org.junit.Test;

public class errorMessagesTest {

    public errorMessagesTest() {
    }

    @Test
    @Points("validations")
    public void itShallWork() {
        assertTrue(new errorMessages().itShallReturnOne() == 1);
    }
}
