package fi.helsinki.cs.tmc.langs.make;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.langs.RunResult.Status;
import fi.helsinki.cs.tmc.langs.TestResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.logging.Level.INFO;

public class CTestResultParser {
    protected static final Logger log = Logger.getLogger(CTestResultParser.class.getName());

    private File testResults;
    private File valgrindOutput;
    private Exercise.ValgrindStrategy valgrindStrategy;
    private ArrayList<CTestCase> tests;
    private File projectDir;

    public CTestResultParser(File testResults, File valgrindOutput, Exercise.ValgrindStrategy valgrindStrategy, File projectDir) {
        this.testResults = testResults;
        this.valgrindOutput = valgrindOutput;
        this.valgrindStrategy = valgrindStrategy;
        this.tests = new ArrayList<CTestCase>();
        this.projectDir = projectDir;
        parseTestOutput();
    }

    public void parseTestOutput() {
        try {
            this.tests = parseTestCases(testResults);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public List<CTestCase> getTestCases() {
        return this.tests;
    }

    public List<TestResult> getTestResults() {
        ArrayList<TestResult> testResults = new ArrayList<>();
        for (CTestCase testCase : this.tests) {
            testResults.add(testCase.getTestResult());
        }
        return testResults;
    }

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


    private ArrayList<CTestCase> parseTestCases(File testOutput) throws ParserConfigurationException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        dBuilder.setErrorHandler(null); // Silence logging
        dbFactory.setValidating(false);
        Document doc = null;

        InputStream inputStream = new FileInputStream(testOutput);
        Reader reader = new InputStreamReader(inputStream, "UTF-8");
        InputSource is = new InputSource(reader);
        is.setEncoding("UTF-8");

        try {
            doc = dBuilder.parse(is);
        } catch (SAXException ex) {
            log.info("SAX parser error ocurred");
            log.info(ex.toString());
        }

        if (doc == null) {
            log.log(INFO, "doc cannot be null - can't parse test results :(");
            throw new IllegalStateException("doc cannot be null - can't parse test results :(");
        }

        doc.getDocumentElement().normalize();

        Map<String, List<String>> idsToPoints = mapIdsToPoints();
        NodeList nodeList = doc.getElementsByTagName("test");
        ArrayList<CTestCase> cases = new ArrayList<CTestCase>();
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

            CTestCase testCase = new CTestCase(name, result, message, points, valgrindStrategy);

            cases.add(testCase);
        }
        log.log(INFO, "C testcases parsed.");
        return cases;
    }

    private Map<String, List<String>> mapIdsToPoints() {
        File availablePoints = new File(projectDir.getAbsolutePath() + File.separatorChar + "test" + File
                .separatorChar + "tmc_available_points.txt");
        Scanner scanner;
        try {
            scanner = new Scanner(availablePoints);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new HashMap<>();
        }

        Map<String, List<String>> idsToPoints = new HashMap<>();
        while(scanner.hasNextLine()) {
            String row = scanner.nextLine();
            String[] parts = row.split("\\[|\\]| ");

            String key = parts[parts.length - 3];
            String value = parts[parts.length - 1];
            addPointsToId(idsToPoints, key, value);
        }

        return idsToPoints;
    }

    private void addPointsToId(Map<String, List<String>> idsToPoints, String key, String value) {
        if (!idsToPoints.containsKey(key)) {
            idsToPoints.put(key, new ArrayList<String>());
        }
        idsToPoints.get(key).add(value);
    }

    private void addWarningToValgrindOutput() {
        String message;
        String platform = System.getProperty("os.name").toLowerCase();
        if (platform.contains("linux")) {
            message = "Please install valgrind. For Debian-based distributions, run `sudo apt-get install valgrind`.";
        } else if (platform.contains("mac")) {
            message = "Please install valgrind. For OS X we recommend using homebrew (http://mxcl.github.com/homebrew/) and `brew install valgrind`.";
        } else if (platform.contains("windows")) {
            message = "Windows doesn't support valgrind yet.";
        } else {
            message = "Please install valgrind if possible.";
        }
        for (int i = 0; i < tests.size(); i++) {
            tests.get(i).setValgrindTrace(
                    "Warning, valgrind not available - unable to run local memory tests\n"
                            + message
                            + "\nYou may also submit the exercise to the server to have it memory-tested.");
        }
    }

    private void addValgrindOutput() throws FileNotFoundException {
        Scanner scanner = new Scanner(valgrindOutput, "UTF-8");
        String parentOutput = ""; // Contains total amount of memory used and such things. Useful if we later want to add support for testing memory usage
        String[] outputs = new String[tests.size()];
        int[] pids = new int[tests.size()];
        int[] errors = new int[tests.size()];
        for (int i = 0; i < outputs.length; i++) {
            outputs[i] = "";
        }

        Pattern errorPattern = Pattern.compile("==[0-9]+== ERROR SUMMARY: ([0-9]+)");

        String line = scanner.nextLine();
        int firstPID = parsePID(line);
        parentOutput += "\n" + line;
        boolean warningLogged = false;
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            int pid = parsePID(line);
            if (pid == -1) {
                continue;
            }
            if (pid == firstPID) {
                parentOutput += "\n" + line;
            } else {
                int outputIndex = findIndex(pid, pids);
                if (outputIndex == -1) {
                    if (!warningLogged) {
                        log.warning("Valgrind output has more PIDs than the expected (# of test cases + 1).");
                        warningLogged = true;
                    }
                    continue;
                }
                outputs[outputIndex] += "\n" + line;
                Matcher m = errorPattern.matcher(line);
                if (m.find()) {
                    errors[outputIndex] = Integer.parseInt(m.group(1));
                }
            }
        }
        scanner.close();

        for (int i = 0; i < outputs.length; i++) {
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

    private int parsePID(String line) {
        try {
            return Integer.parseInt(line.split(" ")[0].replaceAll("(==|--)", ""));
        } catch (Exception e) {
            return -1;
        }
    }

    public RunResult result() {

        return new RunResult(getResultStatus(), ImmutableList.copyOf(getTestResults()),
                new ImmutableMap.Builder<String, byte[]>().build());
    }
}
