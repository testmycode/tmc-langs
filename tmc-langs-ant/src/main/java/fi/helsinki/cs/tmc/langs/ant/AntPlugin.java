package fi.helsinki.cs.tmc.langs.ant;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import fi.helsinki.cs.tmc.langs.*;

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
    public ImmutableList<Path> findExercises(Path basePath) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        return new TestDesc(name,  immutablePoints);
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
        if (!isExerciseTypeCorrect(path)) {
            throw new RuntimeException("Project has no build.xml");
        } else {
            build(path);
            String name = path.getFileName().toString().split(".")[0];
            File file = path.toFile();
            File dir = file.getParentFile();
            URLClassLoader classLoader = null;
            try {
                URL url = new URL("file://" + dir.getAbsolutePath());
                classLoader = new URLClassLoader(new URL[]{url});
            } catch (MalformedURLException ex) {
                Logger.getLogger(AntPlugin.class.getName()).log(Level.SEVERE, null, ex);
            }

            Class<?> clazz = null;
            try {
                clazz = classLoader.loadClass(name);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(AntPlugin.class.getName()).log(Level.SEVERE, null, ex);
            }

            Result testResults = JUnitCore.runClasses(clazz);

            return parseAndConvertTestResults(testResults);

        }
    }

    private void build(Path path) {
        Project project = new Project();
        File buildFile = new File(path.toString() + File.separatorChar + "build.xml");
        project.setUserProperty("ant.file", buildFile.getAbsolutePath());
        project.init();

        ProjectHelper helper = ProjectHelper.getProjectHelper();
        project.addReference("ant.projectHelper", helper);
        helper.parse(project, buildFile);

        project.executeTarget(project.getDefaultTarget());
    }

    /**
     * Check if the exercise's project type corresponds with the language plugin type.
     *
     * @param path The path to the exercise directory.
     * @return True if given path is valid directory for this language  plugin
     */
    private boolean isExerciseTypeCorrect(Path path) {
        return new File(path.toString() + File.separatorChar + "build.xml").exists();
    }

    private RunResult parseAndConvertTestResults(Result testResults) {
        return null;
    }
}
