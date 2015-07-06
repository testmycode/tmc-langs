package fi.helsinki.cs.tmc.langs.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.domain.ValueObject;

import org.apache.commons.io.FileUtils;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class TmcProjectYmlParserTest {

    private TmcProjectYmlParser tmcProjectYmlParser;

    @Before
    public void setUp() {
        this.tmcProjectYmlParser = new TmcProjectYmlParser();
    }

    @Test
    public void testParserParsesOptions() throws IOException {
        Path path = Files.createTempFile("temp", ".txt");
        FileUtils.writeStringToFile(path.toFile(), "simple_option: true");
        Map<String, ValueObject> options = tmcProjectYmlParser.parseOptions(path);

        assertTrue(!options.isEmpty());
        assertEquals(1, options.size());
        assertTrue(options.containsKey("simple_option"));
    }

    @Test
    public void testParserWithSimpleBooleanOption() throws IOException {
        Path path = Files.createTempFile("temp", ".txt");
        FileUtils.writeStringToFile(path.toFile(), "simple_option: true");
        Map<String, ValueObject> options = tmcProjectYmlParser.parseOptions(path);

        assertEquals(true, options.get("simple_option").asBoolean());
    }

    @Test
    public void testParserWithSimpleFalseBooleanOption() throws IOException {
        Path path = Files.createTempFile("temp", ".txt");
        FileUtils.writeStringToFile(path.toFile(), "simple_option: false");
        Map<String, ValueObject> options = tmcProjectYmlParser.parseOptions(path);

        assertEquals(false, options.get("simple_option").asBoolean());
    }

    @Test
    public void testParserWithSimpleStringOption() throws IOException {
        Path path = Files.createTempFile("temp", ".txt");
        FileUtils.writeStringToFile(path.toFile(), "simple_option: option");
        Map<String, ValueObject> options = tmcProjectYmlParser.parseOptions(path);

        assertEquals("option", options.get("simple_option").asString());
    }
}
