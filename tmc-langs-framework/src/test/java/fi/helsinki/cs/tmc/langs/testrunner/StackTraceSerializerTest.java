package fi.helsinki.cs.tmc.langs.testrunner;

import com.google.gson.JsonElement;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class StackTraceSerializerTest {

    private StackTraceSerializer serializer;

    @Before
    public void setUp() {
        serializer = new StackTraceSerializer();
    }

    @Test
    public void testSerializingAndDeserializing() {
        StackTraceElement original = new StackTraceElement("Cls", "method", "Cls.java", 123);
        JsonElement serialized = serializer.serialize(original, StackTraceElement.class, null);
        StackTraceElement result = serializer.deserialize(serialized, StackTraceElement.class, null);

        assertEquals(original.getClassName(), result.getClassName());
        assertEquals(original.getMethodName(), result.getMethodName());
        assertEquals(original.getFileName(), result.getFileName());
        assertEquals(original.getLineNumber(), result.getLineNumber());
    }

    @Test
    public void testSerializingAndDeserializingWithNullFileName() {
        StackTraceElement original = new StackTraceElement("Cls", "method", null, 123);
        JsonElement serialized = serializer.serialize(original, StackTraceElement.class, null);
        StackTraceElement result = serializer.deserialize(serialized, StackTraceElement.class, null);

        assertNull(result.getFileName());
    }

    @Test
    public void testDeserializingWithMissingFileName() {
        StackTraceElement original = new StackTraceElement("Cls", "method", null, 123);
        JsonElement serialized = serializer.serialize(original, StackTraceElement.class, null);
        serialized.getAsJsonObject().remove("fileName");
        StackTraceElement result = serializer.deserialize(serialized, StackTraceElement.class, null);

        assertNull(result.getFileName());
    }
}
