package fi.helsinki.cs.tmc.langs.make;

import static java.util.logging.Level.INFO;

import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.langs.RunResult.Status;
import fi.helsinki.cs.tmc.langs.TestResult;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class CTestResultParser {

    private static final String AVAILABLE_POINTS = File.separatorChar + "tmc_available_points.txt";
    private static final String TEST_DIR = File.separatorChar + "test";
    private static final String VALGRIND_PID_WARNING = "Valgrind output has more PIDs than "
            + "the expected (# of test cases + 1).";
    private static final String VALGRIND_ERROR_SUMMARY_PATTERN =
            "==[0-9]+== ERROR SUMMARY: ([0-9]+)";
    private static final String LINUX_VALGRIND_WARNING = "Please install valgrind. "
            + "For Debian-based distributions, run `sudo apt-get install valgrind`.";
    private static final String OSX_VALGRIND_WARNING = "Please install valgrind. For OS X "
            + "we recommend using homebrew (http://mxcl.github.com/homebrew/) and `brew install valgrind`.";
    private static final String WINDOWS_VALGRIND_WARNING = "Windows doesn't support valgrind yet.";
    private static final String OTHER_VALGRIND_WARNING = "Please install valgrind if possible.";
    private static final String NO_VALGRIND_MESSAGE = "Warning, valgrind not available - "
            + "unable to run local memory tests\n";
    private static final String VALGRIND_SUBMIT_MESSAGE = "\nYou may also submit the exercise "
            + "to the server to have it memory-tested.";
    private static final String DOC_NULL_ERROR_MESSAGE = "doc cannot be null "
            + "- can't parse test results :(";
    private static final String SAX_PARSER_ERROR = "SAX parser error occured";
    private static final String PARSING_DONE_MESSAGE = "C test cases parsed.";

    protected static final Logger log = Logger.getLogger(CTestResultParser.class.getName());

    private File projectDir;
    private File testResults;
    private File valgrindOutput;
    private List<CTestCase> tests;

    /**
    * Create a parser that will parse test results from a file.
    */
    public CTestResultParser(File projectDir, File testResults, File valgrindOutput) {
        this.projectDir = projectDir;
        this.testResults = testResults;
        this.valgrindOutput = valgrindOutput;
        this.tests = new ArrayList<>();
        parseTestOutput();
    }

    /**
    * Parse the output of tests.
    */
    public void parseTestOutput() {
        parseTestCases();
        addValgrindOutputs();
    }

    private void parseTestCases() {
        try {
            this.tests = parseTestCases(testResults);
        } catch (ParserConfigurationException | IOException e) {
            e.printStackTrace();
        }
    }

    private List<CTestCase> parseTestCases(File testOutput)
            throws IOException, ParserConfigurationException {
        Document doc = prepareDocument(testOutput);

        File availablePoints = new File(projectDir.getAbsolutePath() + TEST_DIR
                + AVAILABLE_POINTS);
        Map<String, List<String>> idsToPoints = new MakeUtils().mapIdsToPoints(availablePoints);

        NodeList nodeList = doc.getElementsByTagName("test");
        List<CTestCase> cases = createCTestCases(nodeList, idsToPoints);

        log.log(INFO, PARSING_DONE_MESSAGE);
        return cases;
    }

    private Document prepareDocument(File testOutput)
            throws ParserConfigurationException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

        DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
        documentBuilder.setErrorHandler(null); // Silence logging
        dbFactory.setValidating(false);

        InputStream inputStream = new FileInputStream(testOutput);
        Reader reader = new InputStreamReader(inputStream, "UTF-8");
        InputSource is = new InputSource(reader);
        is.setEncoding("UTF-8");

        Document doc = null;
        try {
            doc = documentBuilder.parse(is);
        } catch (SAXException ex) {
            log.info(SAX_PARSER_ERROR);
            log.info(ex.toString());
        }

        if (doc == null) {
            log.log(INFO, DOC_NULL_ERROR_MESSAGE);
            throw new IllegalStateException(DOC_NULL_ERROR_MESSAGE);
        }

        doc.getDocumentElement().normalize();

        return doc;
    }

    private List<CTestCase> createCTestCases(NodeList nodeList,
                                             Map<String, List<String>> idsToPoints) {
        List<CTestCase> cases = new ArrayList<>();
        List<String> addedCases = new ArrayList<>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Element node = (Element) nodeList.item(i);
            String result = node.getAttribute("result");
            String name = node.getElementsByTagName("description").item(0).getTextContent();
            String message = node.getElementsByTagName("message").item(0).getTextContent();
            String id = node.getElementsByTagName("id").item(0).getTextContent();
            List<String> points = new ArrayList<>();

            if (message.equals("Passed")) {
                message = "";
                points = idsToPoints.get(id);
            }

            addedCases.add(id);

            CTestCase testCase = new CTestCase(name, result, message, points);

            cases.add(testCase);
        }

        cases.addAll(suiteCases(idsToPoints, addedCases));

        return cases;
    }

    private List<CTestCase> suiteCases(Map<String, List<String>> idsToPoints,
                                       List<String> addedCases) {
        List<CTestCase> suiteCases = new ArrayList<>();

        for (String key : idsToPoints.keySet()) {
            if (!addedCases.contains(key)) {
                suiteCases.add(new CTestCase("suite", "success", "", idsToPoints.get(key)));
            }
        }
        return suiteCases;
    }

    /**
    * Returns the test results of the tests in this file.
    */
    public List<TestResult> getTestResults() {
        ArrayList<TestResult> testResults = new ArrayList<>();
        for (CTestCase testCase : this.tests) {
            testResults.add(testCase.getTestResult());
        }
        return testResults;
    }

    /**
    * Returns the combined status of the tests in this file.
    */
    public Status getResultStatus() {
        if (!testResults.exists()) {
            return Status.COMPILE_FAILED;
        }
        for (TestResult result : getTestResults()) {
            if (!result.passed) {
                return Status.TESTS_FAILED;
            }
        }
        return Status.PASSED;
    }

    private void addValgrindOutputs() {
        if (valgrindOutput != null) {
            try {
                addValgrindOutput();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            addWarningToValgrindOutput();
        }
    }

    private void addWarningToValgrindOutput() {
        String message;
        String platform = System.getProperty("os.name").toLowerCase();

        if (platform.contains("linux")) {
            message = LINUX_VALGRIND_WARNING;
        } else if (platform.contains("mac")) {
            message = OSX_VALGRIND_WARNING;
        } else if (platform.contains("windows")) {
            message = WINDOWS_VALGRIND_WARNING;
        } else {
            message = OTHER_VALGRIND_WARNING;
        }

        for (CTestCase test : tests) {
            test.setValgrindTrace(NO_VALGRIND_MESSAGE + message + VALGRIND_SUBMIT_MESSAGE);
        }
    }

    private void addValgrindOutput() throws FileNotFoundException {
        Scanner scanner = new Scanner(valgrindOutput, "UTF-8");
        // Contains total amount of memory used and such things.
        // Useful if we later want to add support for testing memory usage
        String parentOutput = "";
        String[] outputs = new String[tests.size()];
        int[] pids = new int[tests.size()];
        int[] errors = new int[tests.size()];
        for (int i = 0; i < outputs.length; i++) {
            outputs[i] = "";
        }

        Pattern errorPattern = Pattern.compile(VALGRIND_ERROR_SUMMARY_PATTERN);

        String line = scanner.nextLine();
        int firstPid = parsePid(line);
        parentOutput += "\n" + line;
        boolean warningLogged = false;
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            int pid = parsePid(line);
            if (pid == -1) {
                continue;
            }
            if (pid == firstPid) {
                parentOutput += "\n" + line;
            } else {
                int outputIndex = findIndex(pid, pids);
                if (outputIndex == -1) {
                    if (!warningLogged) {
                        log.warning(VALGRIND_PID_WARNING);
                        warningLogged = true;
                    }
                    continue;
                }
                outputs[outputIndex] += "\n" + line;
                Matcher matcher = errorPattern.matcher(line);
                if (matcher.find()) {
                    errors[outputIndex] = Integer.parseInt(matcher.group(1));
                }
            }
        }
        scanner.close();

        for (int i = 0; i < outputs.length; i++) {
            if (errors[i] == 0) {
                // Workaround for a bug where any valgrind output is considered a potential error.
                outputs[i] = null;
            }
            tests.get(i).setValgrindTrace(outputs[i]);
        }
    }

    private int findIndex(int pid, int[] pids) {
        for (int i = 0; i < pids.length; i++) {
            if (pids[i] == pid) {
                return i;
            }
            if (pids[i] == 0) {
                pids[i] = pid;
                return i;
            }
        }
        return -1;
    }

    private int parsePid(String line) {
        try {
            return Integer.parseInt(line.split(" ")[0].replaceAll("(==|--)", ""));
        } catch (Exception e) {
            return -1;
        }
    }

    /**
    * Returns the run result of this file.
    */
    public RunResult result() {
        return new RunResult(getResultStatus(), ImmutableList.copyOf(getTestResults()),
                new ImmutableMap.Builder<String, byte[]>().build());
    }
}
