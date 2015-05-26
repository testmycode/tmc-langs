package fi.helsinki.cs.tmc.langs.ant;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import fi.helsinki.cs.tmc.langs.*;
import fi.helsinki.cs.tmc.langs.RunResult.Status;
import fi.helsinki.cs.tmc.langs.testrunner.TestCaseList;
import fi.helsinki.cs.tmc.langs.testrunner.TestRunnerMain;
import fi.helsinki.cs.tmc.langs.testscanner.TestScanner;
import fi.helsinki.cs.tmc.langs.utils.SourceFiles;
import fi.helsinki.cs.tmc.langs.utils.TestResultParser;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AntPlugin extends AbstractLanguagePlugin {

    private final String testDir = File.separatorChar + "test";
    private final String resultsFile = File.separatorChar + "results.txt";
    private TestResultParser resultParser = new TestResultParser();

    @Override
    public String getLanguageName() {
        return "apache-ant";
    }

    @Override
    public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName) {
        if (!isExerciseTypeCorrect(path)) {
            return Optional.absent();
        }

        TestScanner scanner = new TestScanner();
        SourceFiles sourceFiles = new SourceFiles();
        sourceFiles.addSource(createPath(path.toAbsolutePath(), testDir).toFile());
        return scanner.findTests(generateClassPath(path), sourceFiles, exerciseName);
    }

    @Override
    protected boolean isExerciseTypeCorrect(Path path) {
        return new File(path.toString() + File.separatorChar + "build.xml").exists();
    }

    private RunResult getResults(Status statusCode, CompileResult compileResult) {
        return new RunResult(statusCode, ImmutableList.copyOf(new ArrayList<TestResult>()),
                new ImmutableMap.Builder<String, byte[]>()
                .put(SpecialLogs.STDOUT, compileResult.getStdout())
                .put(SpecialLogs.STDERR, compileResult.getStderr()).build());
    }

    @Override
    public RunResult runTests(Path path) {

        CompileResult compileResult = buildAntProject(path);
        if (compileResult.getStatusCode() != 0) {
            return getResults(Status.COMPILE_FAILED, compileResult);
        }
        TestCaseList cases = TestCaseList.fromExerciseDesc(scanExercise(path, ""));

        RunResult result;
        File resultFile = new File(path.toString() + resultsFile);
        try {
            TestRunnerMain runner = new TestRunnerMain();
            runner.run(path.toString(),
                    generateClassPath(path),
                    path.toString() + resultsFile,
                    cases);
            cases.writeToJsonFile(resultFile);
        } catch (IOException ex) {
            Logger.getLogger(AntPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }

        result = resultParser.parseTestResult(resultFile);
        resultFile.delete();
        return result;
    }

    /**
     * Runs the build.xml file for the the given exercise.
     *
     * @param path The file path of the exercise directory.
     * @return true if build success, else return false.
     */
    protected CompileResult buildAntProject(Path path) {
        File buildFile = new File(path.toString() + File.separatorChar + "build.xml");
        Project buildProject = new Project();
        buildProject.setUserProperty("ant.file", buildFile.getAbsolutePath());
        buildProject.setProperty("javac.fork", "true");
        buildProject.init();
        buildProject.setBaseDir(path.toAbsolutePath().toFile());

        File buildLog = new File(path.toString(), "build_log.txt");
        File errorLog = new File(path.toString(), "build_errors.txt");

        DefaultLogger logger = new DefaultLogger();
        try {

            PrintStream stdOut = new PrintStream(buildLog);
            PrintStream stdErr = new PrintStream(errorLog);

            logger.setErrorPrintStream(stdErr);
            logger.setOutputPrintStream(stdOut);
            logger.setMessageOutputLevel(Project.MSG_INFO);

            buildProject.addBuildListener(logger);
            buildProject.fireBuildStarted();
            ProjectHelper helper = ProjectHelper.getProjectHelper();
            buildProject.addReference("ant.projectHelper", helper);
            helper.parse(buildProject, buildFile);
            buildProject.executeTarget("compile-test");
            buildProject.fireBuildFinished(null);

            return new CompileResult(0,
                    Files.readAllBytes(buildLog.toPath()),
                    Files.readAllBytes(errorLog.toPath()));

        } catch (BuildException e) {
            try {
                buildProject.fireBuildFinished(e);

                return new CompileResult(1,
                        Files.readAllBytes(buildLog.toPath()),
                        Files.readAllBytes(errorLog.toPath()));
            } catch (IOException ex) {
                throw Throwables.propagate(e);
            }

        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private ClassPath generateClassPath(Path path) {
        ClassPath classPath = new ClassPath(path.toAbsolutePath());
        classPath.addDirAndContents(createPath(path, "lib"));
        classPath.add(createPath(path, "build", "test", "classes"));
        classPath.add(createPath(path, "build", "classes"));

        return classPath;
    }

    private boolean endorsedLibsExists(Path path) {
        File endorsedDir = createPath(path, "lib", "endorsed").toFile();
        return endorsedDir.exists() && endorsedDir.isDirectory();
    }

    private Path createPath(Path basePath, String... subDirs) {
        String path = basePath.toAbsolutePath().toString();

        for (String subDir : subDirs) {
            path += File.separatorChar + subDir;
        }

        return Paths.get(path);
    }
}
