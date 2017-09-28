package fi.helsinki.cs.tmc.langs.r;

import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.TestResult;
import fi.helsinki.cs.tmc.langs.utils.ProcessRunner;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TestMain {

    /**
     * Just testing.
     *
     * @param args Nothing.
     */
    public static void main(String[] args) {
        //For now, add the path you want to test here fully,
        //for example: pathToGithubFolder/tmc-r/example_projects/example_project1
        String exampleProjectLocation = "/example_projects/example_project1";
        Path path = Paths.get(exampleProjectLocation);
        RunResult runRes = runTests(path);
        printTestResult(runRes);
        RunResult rr;
    }

    public static void printTestResult(RunResult rr) {
        for (TestResult tr : rr.testResults) {
            System.out.println(tr.toString());
        }
    }


    public static RunResult runTests(Path path) {

        ProcessRunner runner = new ProcessRunner(getTestCommand(), path);
        try {
            runner.call();
        } catch (Exception e) {
            System.out.println("Something wrong: " + e.getMessage());
        }

        try {
            return new RTestResultParser(path).parse();
        } catch (IOException e) {
            System.out.println("Something wrong: " + e.getMessage());
        }
        return null;
    }

    private static String[] getTestCommand() {
        String[] rscr = new String[]{"Rscript", "-e"};
        String[] command;
        if (SystemUtils.IS_OS_WINDOWS) {
            command = new String[] {"\"library('tmcRtestrunner');runTestsWithDefault(TRUE)\""};
        } else {
            command = new String[] {"\"library(tmcRtestrunner);runTests(\"$PWD\", print=TRUE)\""};
        }
        return ArrayUtils.addAll(rscr, command);
    }
}
