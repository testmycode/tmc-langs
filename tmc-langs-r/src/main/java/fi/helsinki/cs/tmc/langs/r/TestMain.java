package fi.helsinki.cs.tmc.langs.r;

import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.TestResult;
import fi.helsinki.cs.tmc.langs.utils.ProcessResult;
import fi.helsinki.cs.tmc.langs.utils.ProcessRunner;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestMain {

    /**
     * Just testing.
     *
     * @param args Nothing.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        //For now, add the path you want to test here fully,
        //for example: pathToGithubFolder/tmc-r/example_projects/example_project1
        String exampleProjectLocation = "/home/antti/rtmc/tmc-r-tester/tmcRtestrunner/tests/testthat/resources/simple_all_tests_pass";


        Path path = Paths.get(exampleProjectLocation);
        RPlugin rplugin = new RPlugin();
        RunResult runRes = rplugin.runTests(path);
        printTestResult(runRes);
    }

    public static void printTestResult(RunResult rr) {
        for (TestResult tr : rr.testResults) {
            System.out.println(tr.toString());
        }
    }


}
