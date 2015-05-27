package fi.helsinki.cs.tmc.langs.testrunner;

import fi.helsinki.cs.tmc.langs.ClassPath;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class TestRunnerMain {

    private static final int DEFAULT_SUITE_TIMEOUT = 180;

    private String resultsFilename = null;
    private String testClassFilename = null;
    private ClassPath testClassPath;

    public TestRunnerMain() {
    }

    public void run(String testDir, ClassPath classPath,
            String resultsDir, TestCaseList cases) throws IOException {

        resultsFilename = resultsDir;
        testClassFilename = testDir;
        testClassPath = classPath;

        runExercises(cases);
        writeResults(cases);
    }

    private void runExercises(TestCaseList cases) {
        TestRunner testRunner = new TestRunner(getTestClassLoader());
        testRunner.runTests(cases, DEFAULT_SUITE_TIMEOUT);
    }

    private ClassLoader getTestClassLoader() {
        try {
            URL[] urls = new URL[testClassPath.getPaths().size()];
            int index = 0;
            for (Path path : testClassPath.getPaths()) {
                urls[index] = new File(path.toString()).toURI().toURL();
                index++;
            }

            return new URLClassLoader(urls);

        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid test class dir: " + testClassFilename);
        }
    }

    private void writeResults(TestCaseList cases) throws IOException {
        cases.writeToJsonFile(new File(resultsFilename));
    }
}
