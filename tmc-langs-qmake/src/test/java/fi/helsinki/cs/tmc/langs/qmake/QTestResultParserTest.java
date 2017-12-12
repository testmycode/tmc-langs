package fi.helsinki.cs.tmc.langs.qmake;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.TestResult;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QTestResultParserTest {

    private final QTestCase passing;
    private final QTestCase failing;
    private final QTestCase another;

    public QTestResultParserTest() {
        passing = new QTestCase("passing", true, "Passed", Arrays.asList(new String[]{"1"}));
        failing = new QTestCase("failing", false, "This test should've failed", 
                                 Arrays.asList(new String[]{"2"}));
        another = new QTestCase("another", true, "Passed", Arrays.asList(new String[]{"3"}));
    }

    @Test(expected = IllegalStateException.class)
    public void testParsingWithNoTests() throws Exception {
        Path emptyTestOutput = TestUtils.initTempFileWithContent("empty_test_output", "xml", "");
        QTestResultParser qtparser = new QTestResultParser();
        qtparser.loadTests(emptyTestOutput);
        assertTrue(qtparser.getTestResults().isEmpty());
    }

    @Test
    public void testParsingWithSuccessfulTest() {
        List<QTestCase> pass = new ArrayList<>();
        pass.add(passing);
        QTestResultParser qtparser = createResultParser(pass);
        List<TestResult> results = qtparser.getTestResults();
        assertEquals("There should only be one test result", 1, results.size());
        TestResult result = results.get(0);
        assertTrue("The test should be successful", result.isSuccessful());
        assertEquals("The name of the test should be \"passing\"",
                "passing",
                result.getName());
        assertEquals("The message should not contain any assertions",
                "",
                result.getMessage());
    }

    @Test
    public void testParsingWithFailingTest() {
        List<QTestCase> fail = new ArrayList<>();
        fail.add(failing);
        QTestResultParser qtparser = createResultParser(fail);
        List<TestResult> results = qtparser.getTestResults();
        assertEquals("There should only be one test result", 1, results.size());
        TestResult result = results.get(0);
        assertFalse("The test should be failing", result.isSuccessful());
        assertEquals("The name of the test should be \"failing\"", "failing",
                result.getName());
        assertEquals("The message should contain the failed assertion",
                "This test should've failed",
                result.getMessage());
    }

    @Test
    public void testParsingWithPassingAndFailingTest() {
        List<QTestCase> failAndPass = new ArrayList<>();
        failAndPass.add(failing);
        failAndPass.add(passing);
        QTestResultParser qtparser = createResultParser(failAndPass);
        List<TestResult> results = qtparser.getTestResults();
        assertEquals("There should only be two test results", 2, results.size());
        TestResult result = results.get(0);
        assertFalse("The test should be failing", result.isSuccessful());
        assertEquals("The name of the test should be \"failing\"", "failing",
                result.getName());
        assertEquals("The message should contain the failed assertion",
                "This test should've failed",
                result.getMessage());
        result = results.get(1);
        assertTrue("The test should be passing", result.isSuccessful());
        assertEquals("The name of the test should be \"passing\"", "passing",
                result.getName());
        assertEquals("The message should not contain any assertions",
                "",
                result.getMessage());
    }

    @Test
    public void testTestRunWithOneFailingTestIsFailed() {
        List<QTestCase> failAndPass = new ArrayList<>();
        failAndPass.add(failing);
        failAndPass.add(passing);
        QTestResultParser qtparser = createResultParser(failAndPass);
        RunResult result = qtparser.result();
        assertEquals("Run result should be failed if one failing test",
                result.status,
                RunResult.Status.TESTS_FAILED);
    }

    @Test
    public void testTestrunWithPassingTestsIsPassed() {
        List<QTestCase> allPass = new ArrayList<>();
        allPass.add(passing);
        allPass.add(passing);
        QTestResultParser qtparser = createResultParser(allPass);
        RunResult result = qtparser.result();
        assertEquals("Run result should be passed if all tests are passing",
                result.status,
                RunResult.Status.PASSED);
    }

    @Test
    public void testParsedPointsMapToCorrectTests() {
        List<QTestCase> allPass = new ArrayList<>();
        allPass.add(passing);
        allPass.add(another);

        QTestResultParser qtparser = createResultParser(allPass);
        List<TestResult> results = qtparser.result().testResults;
        assertEquals(2, results.size());
        assertEquals("Point for passing should be 1",
                "1",
                results.get(0).points.get(0));
        assertEquals("Point for another should be 3",
                "3",
                results.get(1).points.get(0));
    }

    private QTestResultParser createResultParser(List<QTestCase> testCases) {
        Path testPath = null;
        try {
            testPath = constructTestOutput(testCases);
        } catch (Exception e) {
            fail("Error creating or parsing output file: " + e.getMessage());
        }
        QTestResultParser qtparser = new QTestResultParser();
        qtparser.loadTests(testPath);
        
        return qtparser;
    }

    private Path constructTestOutput(List<QTestCase> testCases) throws IOException {
        Path tmp = TestUtils.initTempFileWithContent("test_output", "xml", "");
        PrintWriter pw = new PrintWriter(tmp.toFile(), "UTF-8");
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pw.println("<TestCase name=\"test_runner\">");
        for (QTestCase t : testCases) {
            pw.println("<TestFunction name=\"" + t.getName() + "\">");
            for (String point : t.getPoints()) {
                pw.println("<Message type=\"qinfo\" file=\"\" line=\"0\">");
                pw.println("<Description><![CDATA[TMC:" + t.getName() 
                        + "." + point 
                        + "]]></Description>");
                pw.println("</Message>");
            }

            if (t.getResult()) {
                pw.println("<Incident type=\"pass\" file=\"\" line=\"0\" />");
            } else {
                pw.println("<Incident type=\"fail\" file=\"test_runner.cpp\" line=\"420\">");
                pw.println("    <Description>" + t.getMessage() + "</Description>");
                pw.println("</Incident>");
            }

            pw.println("</TestFunction>");
        }

        pw.println("</TestCase>");

        pw.flush();
        pw.close();
        return tmp;
    }

}
