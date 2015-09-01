package fi.helsinki.cs.tmc.langs.make;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class MakeUtilsTest {

    private MakeUtils makeUtils;
    private Path correctFile;
    private Path incorrectFile;

    public MakeUtilsTest() {
        this.makeUtils = new MakeUtils();
    }

    @Before
    public void setup() throws IOException {
        correctFile =
                TestUtils.initTempFileWithContent(
                        "tmc_available_points", "txt", "[test] [test_one] 1.1");

        incorrectFile = Paths.get("some", "path", "that", "does", "not", "exist");
        incorrectFile.toFile().deleteOnExit();
    }

    @Test
    public void testMapIdsToPointsWithCorrectFile() {
        Map<String, List<String>> testMap = this.makeUtils.mapIdsToPoints(correctFile);

        assertEquals(1, testMap.size());
        assertTrue(testMap.containsKey("test_one"));
        assertEquals("1.1", testMap.get("test_one").get(0));
    }

    @Test
    public void testMapIdsToPointsWithCorrectFileAndMultiplePoints() throws IOException {
        correctFile =
                TestUtils.initTempFileWithContent(
                        "tmc_available_points",
                        "txt",
                        "[test] [test_one] 1.1\n[test] [test_one] 1.2");
        Map<String, List<String>> testMap = this.makeUtils.mapIdsToPoints(correctFile);

        assertTrue(testMap.containsKey("test_one"));
        assertEquals(2, testMap.get("test_one").size());

        assertEquals("1.1", testMap.get("test_one").get(0));
        assertEquals("1.2", testMap.get("test_one").get(1));
    }

    @Test
    public void testMapIdsToPointsWithCorrectFileAndMultipleTestMethods() throws IOException {
        correctFile =
                TestUtils.initTempFileWithContent(
                        "tmc_available_points",
                        "txt",
                        "[test] [test_one] 1.1\n[test] [test_two] 1.2");
        Map<String, List<String>> testMap = this.makeUtils.mapIdsToPoints(correctFile);

        assertEquals(2, testMap.size());

        assertTrue(testMap.containsKey("test_one"));
        assertEquals(1, testMap.get("test_one").size());

        assertTrue(testMap.containsKey("test_two"));
        assertEquals(1, testMap.get("test_two").size());

        assertEquals("1.1", testMap.get("test_one").get(0));
        assertEquals("1.2", testMap.get("test_two").get(0));
    }

    @Test
    public void testMapIdsToPointsWithIncorrectFile() {
        Map<String, List<String>> testMap = this.makeUtils.mapIdsToPoints(incorrectFile);

        assertEquals(0, testMap.size());
    }

    @Test
    public void testInitFileScannerWithCorrectFile() {
        Scanner scanner = this.makeUtils.initFileScanner(correctFile);
        assertNotNull(scanner);
    }

    @Test
    public void testInitFileScannerWithIncorrectFile() {
        Scanner scanner = this.makeUtils.initFileScanner(incorrectFile);
        assertNull(scanner);
    }

    @Test
    public void testRowPartsWithCorrectScanner() {
        Scanner scanner = this.makeUtils.initFileScanner(correctFile);
        String[] rowParts = this.makeUtils.rowParts(scanner);

        assertEquals(3, rowParts.length);
        assertEquals("test", rowParts[0]);
        assertEquals("test_one", rowParts[1]);
        assertEquals("1.1", rowParts[2]);
    }

    @Test
    public void testRowPartsWithIncorrectScanner() {
        Scanner scanner = this.makeUtils.initFileScanner(incorrectFile);
        String[] rowParts = this.makeUtils.rowParts(scanner);

        assertEquals(0, rowParts.length);
    }
}
