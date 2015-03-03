package fi.helsinki.cs.tmc.langs.ant;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import fi.helsinki.cs.tmc.langs.*;
import fi.helsinki.cs.tmc.stylerunner.CheckstyleRunner;
import fi.helsinki.cs.tmc.stylerunner.exception.TMCCheckstyleException;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            path = path.toAbsolutePath();
            String classPath = generateClassPath(path).toString();
            String testDir = path.toString() + File.separatorChar + "test";
            output = startProcess("java", "-cp", classPath, "fi.helsinki.cs.tmc.testscanner.TestScanner", testDir);
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
        testName = testName.substring(1, testName.length() - 1);
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
    private String startProcess(String... args) throws Exception {
        Process process = new ProcessBuilder(args).start();
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line, results = "";
        while ((line = br.readLine()) != null && !line.equals("")) {
            results += line;
        }

        return results.length() > 0 ? results : null;
    }

    @Override
    public RunResult runTests(Path path) {
        List<String> runnerArgs = generateTestRunnerArgs(path);

        return null;
    }

    /**
     * Runs the build.xml file for the the given exercise.
     *
     * @param path The file path of the exercise directory.
     */
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
            buildProject.executeTarget("compile-test");
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
            output = startProcess("--test-runner-format", path.toString());
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }

        runnerArgs.add(output);

        return runnerArgs;
    }

    private ClassPath generateClassPath(Path path) {
        ClassPath classPath = new ClassPath(path.toAbsolutePath());
        classPath.addDirAndContents(createPath(path, "lib"));
        classPath.add(createPath(path, "build", "test", "classes"));
        classPath.add(createPath(path, "build", "classes"));

        return classPath;
    }

    private Path createPath(Path basePath, String... subDirs) {
        String path = basePath.toAbsolutePath().toString();

        for (String subDir : subDirs) {
            path += File.separatorChar + subDir;
        }

        return Paths.get(path);
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
}
