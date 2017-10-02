package fi.helsinki.cs.tmc.langs.r;

import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.TestResult;

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
        String exampleProjectLocation = "/home/janne/R-projects/tmc-r"
                + "/example_projects/example_project1";
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
