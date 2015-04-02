package fi.helsinki.cs.tmc.langs.testrunner;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRunnerMain {

    private static final int DEFAULT_SUITE_TIMEOUT = 180;

    private String resultsFilename = null;
    private int suiteTimeout;

    private String testClassDir = null;

    public TestRunnerMain() {
    }

    public void run(String testDir, String resultsDir, TestCaseList cases) throws IOException {
        resultsFilename = resultsDir;
        testClassDir = testDir;
        
        runExercises(cases);
        writeResults(cases);
    }

    private String requireProperty(String name) {
        String prop = System.getProperty(name);
        if (prop != null) {
            return prop;
        } else {
            throw new IllegalArgumentException("Missing property: " + name);
        }
    }

    private Integer tryGetIntProperty(String name, Integer dflt) {
        String prop = System.getProperty(name);
        if (prop != null) {
            try {
                return Integer.parseInt(prop);
            } catch (NumberFormatException e) {
                return dflt;
            }
        } else {
            return dflt;
        }
    }

    private TestCaseList parseTestCases(String[] names) {
        TestCaseList result = new TestCaseList();

        Pattern regex = Pattern.compile("^([^{]*)\\.([^.{]*)(?:\\{(.*)\\})?$");

        for (String name : names) {
            Matcher matcher = regex.matcher(name);
            if (matcher.matches()) {
                String className = matcher.group(1);
                String methodName = matcher.group(2);
                String pointList = matcher.group(3);

                String[] pointNames;
                if (pointList != null && !pointList.isEmpty()) {
                    pointNames = pointList.split(",");
                } else {
                    pointNames = new String[0];
                }

                result.add(new TestCase(className, methodName, pointNames));
            } else {
                throw new IllegalArgumentException("Illegal test name: " + name);
            }
        }
        return result;
    }

    private void runExercises(TestCaseList cases) {
        TestRunner testRunner = new TestRunner(getTestClassLoader());
        testRunner.runTests(cases, suiteTimeout);
    }

    private ClassLoader getTestClassLoader() {
        try {
            return new URLClassLoader(new URL[]{
                    new File(testClassDir + "/test/").toURI().toURL(),
                    new File(testClassDir + "/build/test/classes").toURI().toURL(),
                    new File(testClassDir + "/build/classes").toURI().toURL()
            });
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid test class dir: " + testClassDir);
        }
    }

    private void writeResults(TestCaseList cases) throws IOException {
        cases.writeToJsonFile(new File(resultsFilename));
    }
}
