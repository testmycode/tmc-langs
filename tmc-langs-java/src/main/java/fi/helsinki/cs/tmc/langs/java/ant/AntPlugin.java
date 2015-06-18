package fi.helsinki.cs.tmc.langs.java.ant;

import fi.helsinki.cs.tmc.langs.CompileResult;
import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.java.AbstractJavaPlugin;
import fi.helsinki.cs.tmc.langs.java.ClassPath;
import fi.helsinki.cs.tmc.langs.java.exception.TestRunnerException;
import fi.helsinki.cs.tmc.langs.java.exception.TestScannerException;
import fi.helsinki.cs.tmc.langs.java.testscanner.TestScanner;
import fi.helsinki.cs.tmc.langs.sandbox.SubmissionProcessor;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;

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
import java.util.List;

public class AntPlugin extends AbstractJavaPlugin {

    private static final String TEST_DIR = "test";
    private static final String RESULT_FILE = "results.txt";
    private static final String BUILD_FILE = "build.xml";
    private static final String BUILD_LOG_FILE = "build_log.txt";
    private static final String BUILD_ERROR_LOG_FILE = "build_errors.txt";

    private static final String ANT_BUILD_FILE_PROPERTY = "ant.file";
    private static final String ANT_JAVAC_FORK_PROPERTY = "javac.fork";
    private static final String ANT_JAVAC_FORK_VALUE = "true";
    private static final String ANT_PROJECT_HELPER_PROPERTY = "ant.projectHelper";
    private static final String ANT_COMPILE_TEST_TARGET = "compile-test";

    private static final int STATUS_CODE_SUCCESS = 0;
    private static final int STATUS_CODE_ERROR = 1;

    /**
     * Create a new AntPlugin.
     */
    public AntPlugin() {
        super(TEST_DIR,
                new SubmissionProcessor(new AntFileMovingPolicy()),
                new TestScanner());
    }

    @Override
    public String getLanguageName() {
        return "apache-ant";
    }

    @Override
    protected boolean isExerciseTypeCorrect(Path path) {
        return path.resolve(BUILD_FILE).toFile().exists();
    }

    /**
     * Runs the build.xml file for the the given exercise.
     *
     * @param path The file path of the exercise directory.
     * @return true if build success, else return false.
     */
    @Override
    protected CompileResult build(Path path) {
        File buildFile = path.resolve(BUILD_FILE).toFile();
        Project buildProject = new Project();
        buildProject.setUserProperty(ANT_BUILD_FILE_PROPERTY, buildFile.getAbsolutePath());
        buildProject.setProperty(ANT_JAVAC_FORK_PROPERTY, ANT_JAVAC_FORK_VALUE);
        buildProject.init();
        buildProject.setBaseDir(path.toAbsolutePath().toFile());

        File buildLog = path.resolve(BUILD_LOG_FILE).toFile();
        File errorLog = path.resolve(BUILD_ERROR_LOG_FILE).toFile();

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
            buildProject.addReference(ANT_PROJECT_HELPER_PROPERTY, helper);
            helper.parse(buildProject, buildFile);
            buildProject.executeTarget(ANT_COMPILE_TEST_TARGET);
            buildProject.fireBuildFinished(null);

            return new CompileResult(STATUS_CODE_SUCCESS,
                    Files.readAllBytes(buildLog.toPath()),
                    Files.readAllBytes(errorLog.toPath()));

        } catch (BuildException buildException) {
            try {
                buildProject.fireBuildFinished(buildException);

                return new CompileResult(STATUS_CODE_ERROR,
                        Files.readAllBytes(buildLog.toPath()),
                        Files.readAllBytes(errorLog.toPath()));
            } catch (IOException ioException) {
                throw Throwables.propagate(buildException);
            }

        } catch (IOException ioException) {
            throw Throwables.propagate(ioException);
        }
    }


    @Override
    protected ClassPath getProjectClassPath(Path path) {
        ClassPath classPath = new ClassPath(path.toAbsolutePath());
        classPath.addDirAndContents(path.resolve("lib"));
        classPath.add(path.resolve(Paths.get("build", "test", "classes")));
        classPath.add(path.resolve(Paths.get("build", "classes")));

        return classPath;
    }

    @Override
    protected File createRunResultFile(Path projectBasePath)
            throws TestRunnerException, TestScannerException {
        Optional<ExerciseDesc> exercise = scanExercise(projectBasePath,
                                                       projectBasePath.toString() + TEST_DIR);
        if (!exercise.isPresent()) {
            throw new TestScannerException();
        }

        Path testDir = projectBasePath.resolve(TEST_DIR);
        Path resultFile = projectBasePath.resolve(RESULT_FILE);
        ClassPath classPath = getProjectClassPath(projectBasePath);
        TestRunnerArgumentBuilder argumentBuilder =  new TestRunnerArgumentBuilder(
                projectBasePath,
                testDir,
                resultFile,
                classPath,
                exercise.get());
        List<String> testRunnerArguments = argumentBuilder.getArguments();

        try {
            Process process = new ProcessBuilder(testRunnerArguments).start();
            process.waitFor();
        } catch (InterruptedException | IOException e) {
            throw new TestRunnerException(e);
        }

        return resultFile.toFile();
    }
}
