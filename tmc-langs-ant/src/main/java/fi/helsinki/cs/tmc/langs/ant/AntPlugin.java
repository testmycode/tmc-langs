package fi.helsinki.cs.tmc.langs.ant;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.LanguagePluginAbstract;
import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.langs.TestDesc;
import fi.helsinki.cs.tmc.stylerunner.CheckstyleRunner;
import fi.helsinki.cs.tmc.stylerunner.exception.TMCCheckstyleException;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import fi.helsinki.cs.tmc.testscanner.TestScanner;
import java.util.Locale;
import java.util.logging.Level;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

public class AntPlugin extends LanguagePluginAbstract {

    private static final Logger log = Logger.getLogger(AntPlugin.class.getName());
    private final TestScanner scanner = new TestScanner();
    private final String testDir = File.separatorChar + "test";
    private final String resultsFile = File.separatorChar + "results.txt";

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
            String testName = parseTestName(test);
            JsonArray points = test.getAsJsonObject().get("points").getAsJsonArray();
            tests.add(generateTestDesc(testName, points));
        }

        return new ExerciseDesc(exerciseName, ImmutableList.<TestDesc>copyOf(tests));
    }

    private String parseTestName(JsonElement test) {
        String testName = test.getAsJsonObject().get("className").toString();
        int index = testName.indexOf('.') + 1;
        testName = testName.substring(index, testName.length() - 1);
        return testName + " " + test.getAsJsonObject().get("methodName").getAsString();
    }

    private TestDesc generateTestDesc(String name, JsonArray pointsArray) {
        List<String> points = new ArrayList<>();

        for (int i = 0; i < pointsArray.size(); i++) {
            points.add(pointsArray.get(i).getAsString());
        }

        ImmutableList<String> immutablePoints = ImmutableList.copyOf(points);
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
        List<String> runnerArgs = generateTestRunnerArgs(path);

        return null;
    }

    public void buildAntProject(Path path) {
        File buildFile = new File(path.toString() + File.separatorChar + "build.xml");
        Project buildProject = new Project();
        buildProject.setUserProperty("ant.file", buildFile.getAbsolutePath());
        DefaultLogger logger = new DefaultLogger();
        logger.setErrorPrintStream(System.err);
        logger.setOutputPrintStream(System.out);
        logger.setMessageOutputLevel(Project.MSG_ERR);
        buildProject.addBuildListener(logger);

        try {
            buildProject.fireBuildStarted();
            buildProject.init();
            ProjectHelper helper = ProjectHelper.getProjectHelper();
            buildProject.addReference("ant.projectHelper", helper);
            helper.parse(buildProject, buildFile);
            buildProject.executeTarget(buildProject.getDefaultTarget());
            buildProject.fireBuildFinished(null);
        } catch (BuildException e) {
            buildProject.fireBuildFinished(e);
        }
    }

    private List<String> generateTestRunnerArgs(Path path) {
        List<String> runnerArgs = new ArrayList<>();

        runnerArgs.add("-Dtmc.test_class_dir=" + path.toString() + testDir);
        runnerArgs.add("-Dtmc.results_file=" + path.toString() + resultsFile);
        //runnerArgs.add("-Dfi.helsinki.cs.tmc.edutestutils.defaultLocale=" + locale);

        String output;

        try {
            output = invokeTestScanner("--test-runner-format", path.toString());
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }

        runnerArgs.add(output);

        return runnerArgs;
    }

    private void generateClassPath(Path path) {

    }

    @Override
    public boolean isExerciseTypeCorrect(Path path) {
        return new File(path.toString() + File.separatorChar + "build.xml").exists();
    }

    @Override
    public ValidationResult checkCodeStyle(Path path) {
        try {
            CheckstyleRunner runner = new CheckstyleRunner(path.toFile(), new Locale("fi"));
            return runner.run();
        } catch (TMCCheckstyleException ex) {
            log.log(Level.SEVERE, "Error running checkstyle:", ex);
            return null;
        }
    }

    @Override
    public ImmutableList<Path> findExercises(Path basePath) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
