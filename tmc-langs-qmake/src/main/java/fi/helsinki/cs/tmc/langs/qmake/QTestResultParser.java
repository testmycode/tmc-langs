package fi.helsinki.cs.tmc.langs.qmake;

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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public final class QTestResultParser {

    private static final String DOC_NULL_ERROR_MESSAGE = "Failed to parse test results";
    private static final String SAX_PARSER_ERROR = "SAX parser error occured";
    private static final String PARSING_DONE_MESSAGE = "Qt test cases parsed.";

    private static final Logger log = LoggerFactory.getLogger(QTestResultParser.class);

    private List<TestResult> tests;

    public QTestResultParser() {
    }

    public void loadTests(Path testResult) {
        this.tests = parseTestCases(testResult);
    }

    private List<TestResult> parseTestCases(Path testOutput) {
        Document doc;
        try {
            doc = prepareDocument(testOutput);
        } catch (ParserConfigurationException | IOException e) {
            log.error("Unexpected exception, could not parse Qt testcases.", e);
            return new ArrayList<>();
        }

        NodeList nodeList = doc.getElementsByTagName("TestFunction");
        List<TestResult> cases = createQtTestResults(nodeList);

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
            log.info(SAX_PARSER_ERROR, ex);
        }

        if (doc == null) {
            log.info(DOC_NULL_ERROR_MESSAGE);
            throw new IllegalStateException(DOC_NULL_ERROR_MESSAGE);
        }

        doc.getDocumentElement().normalize();

        return doc;
    }

    /**
     * Parses Qt testlib XML output, as generated with -o filename.xml,xml.
     * <p>
     * Points are mapped to test cases with Message node type 'qinfo'. These
     * messages contain: prefix "TMC:" test case name: "test_function_name"
     * points separated by period: ".1"
     *
     * With failing testcase assertions, there will be an Incident node with
     * type "fail" and Description node with the failed assertion message.
     *
     * When the testcase assertion(s) has passed, there will be an Incident node
     * with type "pass" and no Description node.
     * </p>
     * <TestFunction name="test_function_name">
     * <Message type="qinfo" file="" line="0">
     * <Description><![CDATA[TMC:test_function_name.1]]></Description>
     * </Message>
     * <Incident type="fail" file="test_source.cpp" line="420">
     * <Description>
     * <![CDATA['!strcmp(hello_msg(), "Helo, world!" )' returned FALSE. ()]]>
     * </Description>
     * </Incident>
     * <Duration msecs="0.135260"/>
     * </TestFunction>
     *
     */
    private List<TestResult> createQtTestResults(NodeList nodeList) {
        List<TestResult> cases = new ArrayList<>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Element testcase = (Element) nodeList.item(i);
            List<String> points = parsePoints(testcase);

            if (points.isEmpty()) {
                // No points == not a TMC testcase
                continue;
            }

            Element incident = (Element) testcase.getElementsByTagName("Incident").item(0);

            String id = testcase.getAttribute("name");
            boolean passed = incident.getAttribute("type").equals("pass");
            String msg = "";

            // Get the assertion error if testcase failed
            if (!passed) {
                Element desc = (Element) incident.getElementsByTagName("Description").item(0);
                msg = desc.getTextContent();
            }

            ImmutableList<String> trace = ImmutableList.of();
            cases.add(new TestResult(id, passed, ImmutableList.copyOf(points), msg, trace));
        }

        return cases;
    }

    /**
     * <p>
     * Parse potential points from testcase.
     * </p>
     */
    private List<String> parsePoints(Element testcase) {
        List<String> points = new ArrayList<>();
        NodeList messages = testcase.getElementsByTagName("Message");
        for (int i = 0; i < messages.getLength(); i++) {
            // Convert node to element, messages.item(i) returns a node
            Element message = (Element) messages.item(i);
            Element desc = (Element) message.getElementsByTagName("Description").item(0);
            String text = desc.getTextContent();
            if (text.matches("^(TMC:.*)")) {
                String result = text.substring(text.indexOf(".") + 1);
                points.add(result);
            }
        }

        return points;
    }

    public List<TestResult> getTestResults() {
        return this.tests;
    }

    private Status getResultStatus() {
        for (TestResult result : getTestResults()) {
            if (!result.isSuccessful()) {
                return Status.TESTS_FAILED;
            }
        }

        return Status.PASSED;
    }

    /**
     * Returns the run result of this file.
     *
     * @return Runresults
     */
    public RunResult result() {
        return new RunResult(
                getResultStatus(),
                ImmutableList.copyOf(getTestResults()),
                new ImmutableMap.Builder<String, byte[]>().build());
    }
}
