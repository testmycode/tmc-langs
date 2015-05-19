package fi.helsinki.cs.tmc.langs.maven;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import fi.helsinki.cs.tmc.langs.*;
import fi.helsinki.cs.tmc.langs.testrunner.TestCaseList;
import fi.helsinki.cs.tmc.langs.testrunner.TestRunnerMain;
import fi.helsinki.cs.tmc.langs.testscanner.TestScanner;
import fi.helsinki.cs.tmc.langs.utils.SourceFiles;
import fi.helsinki.cs.tmc.langs.utils.TestResultParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

public class MavenPlugin extends AbstractLanguagePlugin {

    private static final String RESULT_FILE = File.separatorChar + "results.txt";
    private static final String TEST_FOLDER = File.separatorChar + "src";
    private static final String POM_LOCATION = File.separatorChar + "pom.xml";

    private final TestResultParser resultParser = new TestResultParser();

    @Override
    protected boolean isExerciseTypeCorrect(Path path) {
        return new File(path.toString() + POM_LOCATION).exists();
    }

    @Override
    public String getLanguageName() {
        return "apache-maven";
    }

    @Override
    public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName) {
        if (!isExerciseTypeCorrect(path)) {
            return Optional.absent();
        }

        SourceFiles sourceFiles = new SourceFiles();
        sourceFiles.addSource(new File(path.toString() + TEST_FOLDER));

        ClassPath classPath;
        try {
            classPath = MavenClassPathBuilder.fromProjectBasePath(path);
        } catch (MavenInvocationException | IOException ex) {
            Logger.getLogger(MavenPlugin.class.getName()).log(Level.SEVERE, null, ex);
            return Optional.absent();
        }

        TestScanner scanner = new TestScanner();
        return scanner.findTests(classPath, sourceFiles, exerciseName);
    }

    @Override
    public RunResult runTests(Path projectRootPath) {
        try {
            CompileResult compileResult = buildMaven(projectRootPath);
            if (compileResult.getStatusCode() != 0) {
                return runResultFromFailedCompilation(compileResult);
            }
        } catch (MavenInvocationException ex) {
            Logger.getLogger(MavenPlugin.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

        TestCaseList cases = runTestscanner(projectRootPath);
        RunResult runResult = runTestrunner(projectRootPath, cases);

        return runResult;
    }

    protected RunResult runTestrunner(Path projectRootPath, TestCaseList cases) {
        File resultFile = new File(projectRootPath.toString() + RESULT_FILE);
        RunResult result;
        try {

            TestRunnerMain runner = new TestRunnerMain();
            runner.run(projectRootPath.toString(),
                    getTestClassPathForProject(projectRootPath),
                    resultFile.getAbsolutePath(),
                    cases);
            cases.writeToJsonFile(resultFile);

        } catch (IOException | MavenInvocationException ex) {
            Logger.getLogger(MavenPlugin.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            result = resultParser.parseTestResult(resultFile);
            resultFile.delete();
        }
        return result;
    }

    protected TestCaseList runTestscanner(Path projectRoot) {
        Optional<ExerciseDesc> exercise = scanExercise(projectRoot, "");
        return TestCaseList.fromExerciseDesc(exercise);
    }

    protected ClassPath getTestClassPathForProject(Path projectRoot) throws IOException, MavenInvocationException {
        ClassPath testClassPath = MavenClassPathBuilder.fromProjectBasePath(projectRoot);
        testClassPath.add(Paths.get(projectRoot.toString() + File.separatorChar + "target" + File.separatorChar + "classes"));
        testClassPath.add(Paths.get(projectRoot.toString() + File.separatorChar + "target" + File.separatorChar + "test-classes"));
        return testClassPath;
    }

    protected RunResult runResultFromFailedCompilation(CompileResult compileResult) {
        Map<String, byte[]> logs = new HashMap<>();
        logs.put(SpecialLogs.STDOUT, compileResult.getStdout());
        logs.put(SpecialLogs.STDERR, compileResult.getStderr());

        return new RunResult(RunResult.Status.COMPILE_FAILED, ImmutableList.copyOf(new ArrayList<TestResult>()),
                ImmutableMap.copyOf(logs));
    }

    protected CompileResult buildMaven(Path path) throws MavenInvocationException {
        MavenOutputLogger err = new MavenOutputLogger();
        MavenOutputLogger out = new MavenOutputLogger();

        int result = runMaven(path, out, err, "clean", "compile", "test-compile");

        return new CompileResult(result, out.toByteArray(), err.toByteArray());
    }

    protected int runMaven(Path projectRoot, MavenOutputLogger outputLogger, MavenOutputLogger errorLogger, String... goals) throws MavenInvocationException {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(projectRoot.toString() + POM_LOCATION));
        request.setGoals(new ArrayList<>(Arrays.asList(goals)));
        request.setErrorHandler(errorLogger);
        request.setOutputHandler(outputLogger);
        request.setShowErrors(true);

        Invoker invoker = new DefaultInvoker();
        InvocationResult result = invoker.execute(request);

        return result.getExitCode();
    }
}
