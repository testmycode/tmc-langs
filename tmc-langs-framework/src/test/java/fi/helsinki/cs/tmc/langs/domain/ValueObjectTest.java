package fi.helsinki.cs.tmc.langs.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.IOException;

public class ValueObjectTest {

    @Test
    public void testValueObjectWithBoolean() throws IOException {
        ValueObject valueObject = new ValueObject(true);

        assertEquals(true, valueObject.get());
        assertTrue(valueObject.asBoolean());
        assertNull(valueObject.asString());
    }

    @Test
    public void testValueObjectWithString() throws IOException {
        ValueObject valueObject = new ValueObject("String");

        assertEquals("String", valueObject.get());
        assertEquals("String", valueObject.asString());
        assertNull(valueObject.asBoolean());
    }
}
