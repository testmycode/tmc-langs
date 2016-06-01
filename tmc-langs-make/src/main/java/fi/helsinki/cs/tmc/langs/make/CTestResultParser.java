package fi.helsinki.cs.tmc.langs.make;

import fi.helsinki.cs.tmc.langs.domain.Configuration;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.RunResult.Status;
import fi.helsinki.cs.tmc.langs.domain.TestResult;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public final class CTestResultParser {

    private static final Path AVAILABLE_POINTS = Paths.get("tmc_available_points.txt");
    private static final Path TEST_DIR = Paths.get("test");

    private static final String DOC_NULL_ERROR_MESSAGE =
            "doc cannot be null " + "- can't parse test results :(";
    private static final String SAX_PARSER_ERROR = "SAX parser error occured";
    private static final String PARSING_DONE_MESSAGE = "C test cases parsed.";

    /**
     * Name of the configuration option that controls Valgrind strategy.
     * Defaults to true, should be set to false in .tmcproject.yml if
     * Valgrind failures are allowed.
     */
    private static final String VALGRIND_STRATEGY_OPTION = "fail_on_valgrind_error";

    private static final Logger log = LoggerFactory.getLogger(CTestResultParser.class);

    private Path projectDir;
    private Path testResults;
    private List<CTestCase> tests;
    private boolean failOnValgrindError;

    /**
     * Create a parser that will parse test results from a file.
     */
    public CTestResultParser(
            Path projectDir,
            Path testResults,
            Path valgrindOutput,
            Configuration configuration,
            boolean valgrindWasRun) {
        this.projectDir = projectDir;
        this.testResults = testResults;
        // These last three lines need to be in this exact order.
        this.failOnValgrindError = valgrindStrategy(configuration);
        this.tests = parseTestCases(testResults);
        if (valgrindWasRun && failOnValgrindError) {
            new ValgrindParser(valgrindOutput).addOutputs(tests);
        }
    }

    private boolean valgrindStrategy(Configuration configuration) {
        if (configuration.isSet(VALGRIND_STRATEGY_OPTION)) {
            return configuration.get(VALGRIND_STRATEGY_OPTION).asBoolean();
        }
        return true;
    }

    private List<CTestCase> parseTestCases(Path testOutput) {
        Document doc = null;
        try {
            doc = prepareDocument(testOutput);
        } catch (ParserConfigurationException | IOException e) {
            log.error("Unexpected exception, could not parse C testcases.", e);
            return new ArrayList<>();
        }

        Path availablePoints =
                projectDir.toAbsolutePath().resolve(TEST_DIR).resolve(AVAILABLE_POINTS);
        Map<String, List<String>> idsToPoints = new MakeUtils().mapIdsToPoints(availablePoints);
        NodeList nodeList = doc.getElementsByTagName("test");
        List<CTestCase> cases = createCTestCases(nodeList, idsToPoints);

        log.info(PARSING_DONE_MESSAGE);

        return cases;
    }

    private Document prepareDocument(Path testOutput)
            throws ParserConfigurationException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
        documentBuilder.setErrorHandler(null); // Silence logging
        dbFactory.setValidating(false);

        InputStream inputStream = new FileInputStream(testOutput.toFile());
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
            log.info(DOC_NULL_ERROR_MESSAGE);
            throw new IllegalStateException(DOC_NULL_ERROR_MESSAGE);
        }

        doc.getDocumentElement().normalize();

        return doc;
    }

    private List<CTestCase> createCTestCases(
            NodeList nodeList, Map<String, List<String>> idsToPoints) {
        List<CTestCase> cases = new ArrayList<>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Element node = (Element) nodeList.item(i);
            boolean passed = node.getAttribute("result").equals("success");
            String name = node.getElementsByTagName("description").item(0).getTextContent();
            String message = node.getElementsByTagName("message").item(0).getTextContent();
            String id = node.getElementsByTagName("id").item(0).getTextContent();
            List<String> points = idsToPoints.get(id);

            if (message.equals("Passed")) {
                message = "";
            }

            CTestCase testCase = new CTestCase(name, passed, message, points, failOnValgrindError);

            cases.add(testCase);
        }

        addSuiteCases(idsToPoints, cases);

        return cases;
    }

    private void addSuiteCases(Map<String, List<String>> idsToPoints, List<CTestCase> cases) {
        boolean passed = false;
        List<String> addedCases = addedCases(cases);
        String message = "Some tests failed";
        if (allCasesPassed(cases)) {
            passed = true;
            message = "";
        }

        for (String key : idsToPoints.keySet()) {
            if (!addedCases.contains(key)) {
                cases.add(
                        new CTestCase(
                                "suite." + key,
                                passed,
                                message,
                                idsToPoints.get(key),
                                failOnValgrindError));
            }
        }
    }

    private boolean allCasesPassed(List<CTestCase> cases) {
        for (CTestCase testCase : cases) {
            if (!testCase.getResult()) {
                return false;
            }
        }
        return true;
    }

    private List<String> addedCases(List<CTestCase> cases) {
        List<String> addedCases = new ArrayList<>();
        for (CTestCase testCase : cases) {
            addedCases.add(testCase.getName());
        }
        return addedCases;
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
        if (!Files.exists(testResults)) {
            return Status.COMPILE_FAILED;
        }

        for (TestResult result : getTestResults()) {
            if (!result.isSuccessful()) {
                return Status.TESTS_FAILED;
            }
        }

        return Status.PASSED;
    }

    /**
     * Returns the run result of this file.
     */
    public RunResult result() {
        return new RunResult(
                getResultStatus(),
                ImmutableList.copyOf(getTestResults()),
                new ImmutableMap.Builder<String, byte[]>().build());
    }
}
