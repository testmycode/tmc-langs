package fi.helsinki.cs.tmc.langs.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
    public void testParserWithSimpleOption() throws IOException {
        Path path = Files.createTempFile("temp", ".txt");
        FileUtils.writeStringToFile(path.toFile(), "fail_tests_on_valgrind_error: true");
        Map<String, ValueObject> options = tmcProjectYmlParser.parseOptions(path);

        assertTrue(!options.isEmpty());
        assertEquals(1, options.size());
        assertTrue(options.containsKey("fail_tests_on_valgrind_error"));
        assertEquals(true, options.get("fail_tests_on_valgrind_error").asBoolean());
    }
}
