package fi.helsinki.cs.tmc.langs.maven;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import fi.helsinki.cs.tmc.langs.AbstractLanguagePlugin;
import fi.helsinki.cs.tmc.langs.ClassPath;
import fi.helsinki.cs.tmc.langs.CompileResult;
import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.langs.SpecialLogs;
import fi.helsinki.cs.tmc.langs.TestResult;
import fi.helsinki.cs.tmc.langs.testrunner.TestCaseList;
import fi.helsinki.cs.tmc.langs.testrunner.TestRunnerMain;
import fi.helsinki.cs.tmc.langs.testscanner.TestScanner;
import fi.helsinki.cs.tmc.langs.utils.SourceFiles;
import fi.helsinki.cs.tmc.langs.utils.TestResultParser;

import org.apache.maven.cli.MavenCli;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        } catch (IOException ex) {
            Logger.getLogger(MavenPlugin.class.getName()).log(Level.SEVERE, null, ex);
            return Optional.absent();
        }

        TestScanner scanner = new TestScanner();
        return scanner.findTests(classPath, sourceFiles, exerciseName);
    }

    @Override
    public RunResult runTests(Path projectRootPath) {
        CompileResult compileResult = buildMaven(projectRootPath);
        if (compileResult.getStatusCode() != 0) {
            return runResultFromFailedCompilation(compileResult);
        }

        TestCaseList cases = runTestscanner(projectRootPath);

        return runTestrunner(projectRootPath, cases);
    }

    private RunResult runTestrunner(Path projectRootPath, TestCaseList cases) {
        File resultFile = new File(projectRootPath.toString() + RESULT_FILE);
        RunResult result;
        try {

            TestRunnerMain runner = new TestRunnerMain();
            runner.run(projectRootPath.toString(),
                    getTestClassPathForProject(projectRootPath),
                    resultFile.getAbsolutePath(),
                    cases);
            cases.writeToJsonFile(resultFile);

        } catch (IOException ex) {
            Logger.getLogger(MavenPlugin.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            result = resultParser.parseTestResult(resultFile);
            resultFile.delete();
        }
        return result;
    }

    private TestCaseList runTestscanner(Path projectRoot) {
        Optional<ExerciseDesc> exercise = scanExercise(projectRoot, "");
        return TestCaseList.fromExerciseDesc(exercise);
    }

    protected ClassPath getTestClassPathForProject(Path projectRoot) throws IOException {
        ClassPath testClassPath = MavenClassPathBuilder.fromProjectBasePath(projectRoot);
        testClassPath.add(Paths.get(projectRoot.toString() + File.separatorChar + "target" + File.separatorChar + "classes"));
        testClassPath.add(Paths.get(projectRoot.toString() + File.separatorChar + "target" + File.separatorChar + "test-classes"));
        return testClassPath;
    }

    private RunResult runResultFromFailedCompilation(CompileResult compileResult) {
        Map<String, byte[]> logs = new HashMap<>();
        logs.put(SpecialLogs.STDOUT, compileResult.getStdout());
        logs.put(SpecialLogs.STDERR, compileResult.getStderr());

        return new RunResult(RunResult.Status.COMPILE_FAILED, ImmutableList.copyOf(new ArrayList<TestResult>()),
                ImmutableMap.copyOf(logs));
    }

    protected CompileResult buildMaven(Path path) {
        MavenCli maven = new MavenCli();

        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuf = new ByteArrayOutputStream();

        int compileResult = maven.doMain(new String[]{"clean", "compile", "test-compile"}, path.toAbsolutePath().toString(),
                new PrintStream(outBuf), new PrintStream(errBuf));

        return new CompileResult(compileResult, outBuf.toByteArray(), errBuf.toByteArray());
    }
}
