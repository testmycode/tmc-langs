package fi.helsinki.cs.tmc.langs.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NoLanguagePluginFoundExceptionTest {

    @Test
    public void canBeConstructedWithAMessage() {
        NoLanguagePluginFoundException exception = new NoLanguagePluginFoundException("Test");
        assertEquals("Test", exception.getMessage());
    }

    @Test
    public void canBeConstructedWithAMessageAndThrowable() {
        Throwable throwable = new Throwable("Test2");
        NoLanguagePluginFoundException exception = new NoLanguagePluginFoundException("Test",
                throwable);
        assertEquals("Test", exception.getMessage());
        assertEquals(throwable, exception.getCause());
        assertEquals("Test2", exception.getCause().getMessage());
    }
}
