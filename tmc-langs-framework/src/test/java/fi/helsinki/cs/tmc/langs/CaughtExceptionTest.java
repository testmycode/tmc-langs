package fi.helsinki.cs.tmc.langs;

import static org.junit.Assert.assertEquals;

import fi.helsinki.cs.tmc.langs.CaughtException;

import org.junit.Test;

public class CaughtExceptionTest {

    @Test
    public void testCloneCreatesIdenticalClass() {
        try {
            ArrayIndexOutOfBoundsException exception = new ArrayIndexOutOfBoundsException("test");
        } catch (Exception e) {
            CaughtException ce = new CaughtException(e);
            CaughtException ceClone = ce.clone();

            assertEquals(ceClone.toString(), ce.toString());
        }

    }
}
