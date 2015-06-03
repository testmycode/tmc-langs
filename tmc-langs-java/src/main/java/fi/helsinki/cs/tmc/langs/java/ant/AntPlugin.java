package fi.helsinki.cs.tmc.langs.java.ant;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;

import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.java.AbstractJavaPlugin;
import fi.helsinki.cs.tmc.langs.java.ClassPath;
import fi.helsinki.cs.tmc.langs.CompileResult;
import fi.helsinki.cs.tmc.langs.java.exception.TestRunnerException;
import fi.helsinki.cs.tmc.langs.java.exception.TestScannerException;

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

    private static final String TEST_DIR = File.separatorChar + "test";
    private static final String RESULT_FILE = File.separatorChar + "results.txt";

    public AntPlugin() {
        super(TEST_DIR);
    }

    @Override
    public String getLanguageName() {
        return "apache-ant";
    }

    @Override
    protected boolean isExerciseTypeCorrect(Path path) {
        return new File(path.toString() + File.separatorChar + "build.xml").exists();
    }

    /**
     * Runs the build.xml file for the the given exercise.
     *
     * @param path The file path of the exercise directory.
     * @return true if build success, else return false.
     */
    @Override
    protected CompileResult build(Path path) {
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


    @Override
    protected ClassPath getProjectClassPath(Path path) {
        ClassPath classPath = new ClassPath(path.toAbsolutePath());
        classPath.addDirAndContents(Paths.get(path.toString(), "lib"));
        classPath.add(Paths.get(path.toString(), "build", "test", "classes"));
        classPath.add(Paths.get(path.toString(), "build", "classes"));

        return classPath;
    }

    @Override
    protected File createRunResultFile(Path projectBasePath) throws TestRunnerException, TestScannerException {
        Optional<ExerciseDesc> exercise = scanExercise(projectBasePath, projectBasePath.toString() + TEST_DIR);
        if (!exercise.isPresent()) {
            throw new TestScannerException();
        }

        Path testDir = Paths.get(projectBasePath.toString() + TEST_DIR);
        Path resultFile = Paths.get(projectBasePath.toString() + RESULT_FILE);
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

        return new File(projectBasePath.toString() + RESULT_FILE);
    }
}
