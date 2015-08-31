package fi.helsinki.cs.tmc.langs.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.io.FileUtils;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigurationTest {

    private Configuration configuration;

    @Before
    public void setUp() throws IOException {
        Path path = Files.createTempFile("temp", ".txt");
        FileUtils.writeStringToFile(path.toFile(), "simple_option: true");
        this.configuration = new Configuration(path);
    }

    @Test
    public void testIsSetWithCorrectOption() throws IOException {
        assertTrue(configuration.isSet("simple_option"));
    }

    @Test
    public void testIsSetWithFaultyOption() {
        assertFalse(configuration.isSet("not_set_option"));
    }

    @Test
    public void testGetWithCorrectOption() {
        assertNotNull(configuration.get("simple_option"));
        assertEquals(true, configuration.get("simple_option").asBoolean());
    }

    @Test
    public void testGetWithFaultyOption() {
        assertNull(configuration.get("not_set_option"));
    }

    @Test
    public void testParseOptions() throws IOException {
        Path path = Files.createTempFile("options", ".txt");
        FileUtils.writeStringToFile(path.toFile(), "option: true");

        this.configuration = new Configuration();

        assertFalse(configuration.isSet("option"));

        configuration.parseOptions(path);

        assertTrue(configuration.isSet("option"));
        assertEquals(true, configuration.get("option").asBoolean());
    }
}
