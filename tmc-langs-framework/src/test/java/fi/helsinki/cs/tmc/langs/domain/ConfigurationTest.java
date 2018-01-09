package fi.helsinki.cs.tmc.langs.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigurationTest {

    private Configuration configuration;
    private Path folder;
    private Path file;

    @Before
    public void setUp() throws IOException {
        folder = Files.createTempDirectory("tmc-config");
        file = folder.resolve(".tmcproject.yml");
        Files.createFile(file);
        Files.write(file, "simple_option: true".getBytes());
        this.configuration = new Configuration(folder);
    }

    @After
    public void tearDown() throws IOException {
        Files.deleteIfExists(file);
        Files.deleteIfExists(folder);
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
        Files.write(file, "option: true".getBytes());

        this.configuration = new Configuration(folder);

        configuration.parseOptions(folder);

        assertTrue(configuration.isSet("option"));
        assertEquals(true, configuration.get("option").asBoolean());
    }
}
