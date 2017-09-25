package fi.helsinki.cs.tmc.langs.r;

import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.TestResult;

import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TestMain {

    /**
     * Just testing.
     * @param args Nothing.
     */
    public static void main(String[] args) {

        Path path = Paths.get(".");

        RunResult rr;

        try {
            rr = new RTestResultParser(path).parse();
            for (TestResult tr : rr.testResults) {
                System.out.println(tr.toString());
            }
        } catch (IOException e) {
            System.out.println("Something wrong: " + e.getMessage());
        }
    }
}
