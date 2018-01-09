package fi.helsinki.cs.tmc.langs.make;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import fi.helsinki.cs.tmc.langs.domain.Configuration;
import fi.helsinki.cs.tmc.langs.domain.TestResult;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CTestResultParserTest {

    private ArrayList<CTestCase> oneOfEachTest;

    private Path tempDir;

    public CTestResultParserTest() {}

    @Before
    public void setUp() throws IOException {
        oneOfEachTest = new ArrayList<>();
        oneOfEachTest.add(new CTestCase("passing", true, "Passed", null));
        oneOfEachTest.add(new CTestCase("failing", false, "This test should've failed", null));
        this.tempDir = Files.createTempDirectory("temporary");
    }

    @After
    public void tearDown() throws IOException {
        Files.deleteIfExists(tempDir);
    }

    @Test(expected = IllegalStateException.class)
    public void testParsingWithNoTests() throws Exception {
        CTestResultParser cpar = null;
        Path tmp = mkTempFile("test_output", ".xml");
        try {
            cpar = new CTestResultParser(null, tmp, null, new Configuration(tempDir), true);
        } finally {
            Files.delete(tmp);
        }
        assertTrue(cpar.getTestResults().isEmpty());
    }

    @Test
    public void testParsingWithOneSuccessfulTest() {
        CTestResultParser cpar = null;
        try {
            ArrayList<CTestCase> testCases = new ArrayList<>();
            testCases.add(oneOfEachTest.get(0));
            Path tmp = constructTestOutput(testCases);
            cpar =
                    new CTestResultParser(tmpFolder(), tmp,
                            emptyValgrindOutput(), new Configuration(tempDir), true);
            Files.delete(tmp);
        } catch (Exception e) {
            fail("Error creating or parsing mock output file: " + e.getMessage());
        }
        List<TestResult> results = cpar.getTestResults();
        assertEquals("There should only be one test result", 1, results.size());
        TestResult result = results.get(0);
        assertTrue("The test should be successful", result.isSuccessful());
        assertEquals("The name of the test should be \"passing\"", "passing", result.getName());
    }

    @Test
    public void testParsingWithOneFailedTest() {
        CTestResultParser cpar = null;
        try {
            ArrayList<CTestCase> testCases = new ArrayList<>();
            testCases.add(oneOfEachTest.get(1));
            Path tmp = constructTestOutput(testCases);
            cpar = new CTestResultParser(tmpFolder(), tmp, null, new Configuration(tempDir), true);
            Files.delete(tmp);

        } catch (Exception e) {
            fail("Error creating or parsing mock output file: " + e.getMessage());
        }
        List<TestResult> results = cpar.getTestResults();
        assertEquals("There should only be one test result", 1, results.size());
        TestResult result = results.get(0);
        assertFalse("The test should not be successful", result.isSuccessful());
        assertEquals(
                "The test should contain the message: This test should've failed",
                "This test should've failed",
                result.getMessage());
        assertEquals("The name of the test should be \"failing\"", "failing", result.getName());
    }

    @Test
    public void testParsingWithOneFailingAndOnePassing() {
        CTestResultParser cpar = null;
        try {
            Path tmp = constructTestOutput(oneOfEachTest);
            cpar =
                    new CTestResultParser(tmpFolder(), tmp,
                            emptyValgrindOutput(), new Configuration(tempDir), true);
            tmp.toFile().delete();

        } catch (Exception e) {
            fail("Error creating or parsing mock output file: " + e.getMessage());
        }
        List<TestResult> results = cpar.getTestResults();
        assertEquals("There should be two test results", 2, results.size());
        assertTrue("The first should be passing", results.get(0).isSuccessful());
        assertFalse("The second should be failing", results.get(1).isSuccessful());
    }

    @Test
    public void testParsingWithEmptyValgrindOutput() {
        CTestResultParser cpar = null;
        try {
            ArrayList<CTestCase> testCases = new ArrayList<>();
            testCases.add(oneOfEachTest.get(1));
            Path ttmp = constructTestOutput(testCases);
            cpar = new CTestResultParser(tmpFolder(), ttmp, null, new Configuration(tempDir), true);
            ttmp.toFile().delete();
            Path vtmp = constructNotMemoryFailingValgrindOutput(testCases);
            vtmp.toFile().delete();

        } catch (Exception e) {
            fail("Error creating or parsing mock output file: " + e.getMessage());
        }
        List<TestResult> results = cpar.getTestResults();
        assertEquals("There should only be one test result", 1, results.size());
        TestResult result = results.get(0);
        assertFalse("The test should not be successful", result.isSuccessful());
        assertEquals(
                "The test should contain the message: This test should've failed",
                "This test should've failed",
                result.getMessage());
        assertEquals("The name of the test should be \"failing\"", "failing", result.getName());
    }

    @Test
    public void testParsingWithValgrindOutput() {
        CTestResultParser cpar = null;
        try {
            Path ttmp = constructTestOutput(oneOfEachTest);
            Path vtmp = constructMemoryFailingValgrindOutput();

            cpar = new CTestResultParser(tmpFolder(), ttmp, vtmp, new Configuration(tempDir), true);
            vtmp.toFile().delete();
            ttmp.toFile().delete();
        } catch (IOException e) {
            fail("Error creating or parsing mock output file: " + e.getMessage());
        }
        List<TestResult> results = cpar.getTestResults();
        assertEquals("There should be two test results", 2, results.size());
        assertNotNull("Valgrind errors should go in backtrace", results.get(0).getException());
        assertTrue(
                "Valgrind errors should go in backtrace",
                results.get(0).getException().contains("==1== 1"));
        assertEquals(
                "Valgrind output should go into backtrace if there were not errors",
                0,
                results.get(1).getException().size());
    }

    @Test
    public void testTestsPassWhenNoMemoryErrors() {
        CTestResultParser cpar = null;
        try {
            Path ttmp = constructTestOutput(oneOfEachTest);
            Path vtmp = constructNotMemoryFailingValgrindOutput(oneOfEachTest);

            cpar = new CTestResultParser(tmpFolder(), ttmp, vtmp, new Configuration(tempDir), true);
            vtmp.toFile().delete();
            ttmp.toFile().delete();
        } catch (Exception e) {
            fail("Error creating or parsing mock output file: " + e.getMessage());
        }
        List<TestResult> results = cpar.getTestResults();
        assertEquals("There should be two test results", 2, results.size());
        for (TestResult r : results) {
            assertEquals(
                    "Valgrind output should be empty when there was no error",
                    0,
                    r.getException().size());
        }
    }

    @Test
    public void testValgrindStrategyDefaultsToTrue() {
        CTestResultParser cpar = null;
        try {
            Path ttmp = constructTestOutput(oneOfEachTest);
            Path vtmp = constructMemoryFailingValgrindOutput();

            cpar = new CTestResultParser(tmpFolder(), ttmp, vtmp, new Configuration(tempDir), true);
            vtmp.toFile().delete();
            ttmp.toFile().delete();
        } catch (IOException e) {
            fail("Error creating or parsing mock output file: " + e.getMessage());
        }
        List<TestResult> results = cpar.getTestResults();

        assertFalse(results.get(0).isSuccessful());
        assertFalse(results.get(1).isSuccessful());
    }

    @Test
    public void testTestsPassWhenValgrindFailuresAllowed() throws IOException {
        Path folder = Files.createTempDirectory("tmc-config");
        Path file = folder.resolve(".tmcproject.yml");
        Files.createFile(file);
        Files.write(file, "fail_on_valgrind_error: false".getBytes());
        Configuration configuration = new Configuration(folder);

        CTestResultParser cpar = null;
        try {
            Path ttmp = constructTestOutput(oneOfEachTest);
            Path vtmp = constructMemoryFailingValgrindOutput();

            cpar = new CTestResultParser(tmpFolder(), ttmp, vtmp, configuration, true);
            vtmp.toFile().delete();
            ttmp.toFile().delete();
        } catch (IOException e) {
            fail("Error creating or parsing mock output file: " + e.getMessage());
        }
        List<TestResult> results = cpar.getTestResults();

        // The one with only Valgrind errors passes
        assertTrue(results.get(0).isSuccessful());
        // The one with failing tests still fails
        assertFalse(results.get(1).isSuccessful());
    }

    private Path constructTestOutput(ArrayList<CTestCase> testCases) throws IOException {
        Path tmp = mkTempFile("test_output", ".xml");
        PrintWriter pw = new PrintWriter(tmp.toFile(), "UTF-8");
        pw.println("<?xml version=\"1.0\"?>");
        pw.println("<testsuites xmlns=\"http://check.sourceforge.net/ns\">");
        pw.println("  <datetime>2013-02-14 14:57:08</datetime>");
        pw.println("  <suite>");
        pw.println("    <title>tests</title>");
        for (CTestCase t : testCases) {
            String result = t.getResult() ? "success" : "failure";
            pw.println("    <test result=\"" + result + "\">");
            pw.println("      <path>.</path>");
            pw.println("      <fn>test.c:1</fn>");
            pw.println("      <id>" + t.getName() + "</id>");
            pw.println("      <iteration>0</iteration>");
            pw.println("      <description>" + t.getName() + "</description>");
            pw.println("      <message>" + t.getMessage() + "</message>");
            pw.println("    </test>");
        }

        pw.println("  </suite>");
        pw.println("  <duration>0.000000</duration>");
        pw.println("</testsuites>");
        pw.flush();
        pw.close();
        return tmp;
    }

    private Path constructNotMemoryFailingValgrindOutput(ArrayList<CTestCase> testCases)
            throws IOException {
        Path tmp = mkTempFile("valgrind", ".log");
        PrintWriter pw = new PrintWriter(tmp.toFile());
        pw.println("==" + testCases.size() * 2 + 1 + "== Main process");
        int counter = 2;
        for (CTestCase t : testCases) {
            pw.println("==" + counter * 2 + "== " + (counter - 1));
            pw.println("Some crap that should be ignored");
            pw.println("==" + counter * 2 + "== ERROR SUMMARY: 0 errors from 0 contexts");
            pw.println("==" + counter * 2 + "== LEAK SUMMARY:");
            pw.println("==" + counter * 2 + "==   definitely lost: 0 bytes in 0 blocks");
            counter++;
        }
        pw.println("==" + testCases.size() * 2 + 1 + "== Done");

        pw.flush();
        pw.close();
        return tmp;
    }

    private Path constructMemoryFailingValgrindOutput() throws IOException {
        Path tmp = mkTempFile("valgrind", ".log");
        PrintWriter pw = new PrintWriter(tmp.toFile());
        pw.println("==10== Main process");
        pw.println("==1== 1");
        pw.println("Some crap that should be ignore");
        pw.println("==1== ERROR SUMMARY: 1 errors from 1 contexts");
        pw.println("==1== LEAK SUMMARY:");
        pw.println("==1==   definitely lost: 0 bytes in 0 blocks");
        pw.println("==1== HEAP SUMMARY:");
        pw.println("==1==   total heap usage: 624 allocs, 237 frees, 0 bytes allocated");
        pw.println("==2== 2");
        pw.println("Some crap that should be ignore");
        pw.println("==2== ERROR SUMMARY: 0 errors from 0 contexts");
        pw.println("==2== LEAK SUMMARY:");
        pw.println("==2==   definitely lost: 124 bytes in 3 blocks");
        pw.println("==2== HEAP SUMMARY:");
        pw.println("==2==   total heap usage: 624 allocs, 237 frees, 0 bytes allocated");
        pw.println("==3== 3");
        pw.println("Some crap that should be ignore");
        pw.println("==3== ERROR SUMMARY: 0 errors from 0 contexts");
        pw.println("==3== LEAK SUMMARY:");
        pw.println("==3==   definitely lost: 0 bytes in 0 blocks");
        pw.println("==3== HEAP SUMMARY:");
        pw.println("==3==   total heap usage: 624 allocs, 237 frees, 100 bytes allocated");
        pw.println("==10== Done");

        pw.flush();
        pw.close();
        return tmp;
    }

    private Path emptyValgrindOutput() throws IOException {
        Path tmp = mkTempFile("valgrind", ".log");
        PrintWriter pw = new PrintWriter(tmp.toFile());
        pw.println("Nothing");
        pw.flush();
        pw.close();
        return tmp;
    }

    private Path mkTempFile(String prefix, String suffix) throws IOException {
        File tmp = File.createTempFile(prefix, suffix);
        tmp.deleteOnExit();
        return tmp.toPath();
    }

    private Path tmpFolder() {
        return Paths.get(System.getProperty("java.io.tmpdir"));
    }
}
