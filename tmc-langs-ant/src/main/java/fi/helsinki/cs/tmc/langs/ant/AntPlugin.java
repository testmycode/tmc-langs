package fi.helsinki.cs.tmc.langs.ant;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import fi.helsinki.cs.tmc.langs.*;
import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.LanguagePluginAbstract;
import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fi.helsinki.cs.tmc.testscanner.TestScanner;
import java.util.Stack;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import javax.tools.ToolProvider;

public class AntPlugin extends LanguagePluginAbstract {

    private static final Logger log = Logger.getLogger(AntPlugin.class.getName());
    TestScanner scanner = new TestScanner(ToolProvider.getSystemJavaCompiler());

    @Override
    public String getLanguageName() {
        return "apache-ant";
    }

    @Override
    public ExerciseDesc scanExercise(Path path, String exerciseName) {
        if (!isExerciseTypeCorrect(path)) {
            return null;
        }

        String output;
        try {
            output = invokeTestScanner(path.toString());
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }

        return parseAndConvertScannerOutput(output, exerciseName);
    }

    /**
     * Parse and convert tmc-testscanner output into ExerciseDescription.
     *
     * @param output Output from the tmc-testscanner.
     * @param exerciseName The name of the exercise.
     * @return Parsed exercise description.
     */
    private ExerciseDesc parseAndConvertScannerOutput(String output, String exerciseName) {

        List<TestDesc> tests = new ArrayList<>();
        JsonElement data = new JsonParser().parse(output);

        for (JsonElement test : data.getAsJsonArray()) {
            String testName = test.getAsJsonObject().get("methodName").getAsString();
            String[] points = test.getAsJsonObject().get("points").toString().replaceAll("\\\"|\\]|\\[", "").split(",");
            tests.add(createTestDesc(testName, points));
        }

        return new ExerciseDesc(exerciseName, ImmutableList.<TestDesc>copyOf(tests));
    }

    private TestDesc createTestDesc(String name, String[] points) {
        ImmutableList<String> immutablePoints = ImmutableList.copyOf(Arrays.asList(points));
        return new TestDesc(name, immutablePoints);
    }

    /**
     * Scan for tests for given project path using tmc-testscanner.
     *
     * @param args Arguments for starting tmc-testscanner.
     * @return Output from tmc-testscanner.
     * @throws Exception
     */
    private String invokeTestScanner(String... args) throws Exception {
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;

        try {
            System.setOut(new PrintStream(outBuf, true, "UTF-8"));
            scanner.main(args);
        } finally {
            System.setOut(oldOut);
        }

        return outBuf.toString("UTF-8");
    }

    @Override
    public RunResult runTests(Path path) {
        return null;
    }

    @Override
    protected boolean isExerciseTypeCorrect(Path path) {
        return new File(path.toString() + File.separatorChar + "build.xml").exists();
    }
    
    @Override
    public ValidationResult checkCodeStyle(Path path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
