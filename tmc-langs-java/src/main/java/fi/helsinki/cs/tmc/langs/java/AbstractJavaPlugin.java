package fi.helsinki.cs.tmc.langs.java;

import fi.helsinki.cs.tmc.langs.AbstractLanguagePlugin;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.CompileResult;
import fi.helsinki.cs.tmc.langs.domain.ExerciseBuilder;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.SpecialLogs;
import fi.helsinki.cs.tmc.langs.domain.TestResult;
import fi.helsinki.cs.tmc.langs.io.sandbox.SubmissionProcessor;
import fi.helsinki.cs.tmc.langs.io.zip.StudentFileAwareUnzipper;
import fi.helsinki.cs.tmc.langs.io.zip.StudentFileAwareZipper;
import fi.helsinki.cs.tmc.langs.java.exception.TestRunnerException;
import fi.helsinki.cs.tmc.langs.java.exception.TestScannerException;
import fi.helsinki.cs.tmc.langs.utils.SourceFiles;

import fi.helsinki.cs.tmc.stylerunner.CheckstyleRunner;
import fi.helsinki.cs.tmc.stylerunner.exception.TMCCheckstyleException;
import fi.helsinki.cs.tmc.testscanner.TestMethod;
import fi.helsinki.cs.tmc.testscanner.TestScanner;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * An abstract extension of {@link AbstractLanguagePlugin} that implements features common to all
 * Java language plugins.
 */
public abstract class AbstractJavaPlugin extends AbstractLanguagePlugin {

    private static final Logger log = LoggerFactory.getLogger(AbstractJavaPlugin.class);

    private final TestResultParser resultParser = new TestResultParser();
    private final Path testFolderPath;
    private final LazyTestScanner testScanner;

    /**
     * Creates a new AbstractJavaPlugin.
     */
    public AbstractJavaPlugin(
            Path testFolderPath,
            SubmissionProcessor submissionProcessor,
            LazyTestScanner testScanner) {
        super(
                new ExerciseBuilder(),
                submissionProcessor,
                new StudentFileAwareZipper(),
                new StudentFileAwareUnzipper());
        this.testFolderPath = testFolderPath;
        this.testScanner = testScanner;
    }

    protected abstract ClassPath getProjectClassPath(Path path) throws IOException;

    protected abstract CompileResult build(Path projectRootPath);

    protected abstract TestRunFileAndLogs createRunResultFile(Path path,
                                                              CompileResult compileResult)
            throws TestRunnerException, TestScannerException;

    @Override
    public ValidationResult checkCodeStyle(Path path, Locale msgLocale) {
        try {
            CheckstyleRunner runner = new CheckstyleRunner(path.toFile(), msgLocale);

            return runner.run();
        } catch (TMCCheckstyleException ex) {
            log.error("Unable to run checkCodeStyle", ex);
            return null;
        }
    }

    @Override
    public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName) {
        if (!isExerciseTypeCorrect(path)) {
            return Optional.absent();
        }

        CompileResult compileResult = build(path);
        return scanExercise(path, exerciseName, compileResult);

    }

    public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName,
                                               CompileResult compileResult) {
        if (!isExerciseTypeCorrect(path)) {
            return Optional.absent();
        }
        if (compileResult.getStatusCode() != 0) {
            return Optional.absent();
        }

        SourceFiles sourceFiles = new SourceFiles();
        sourceFiles.addSource(path.resolve(testFolderPath).toFile());

        ClassPath classPath;
        try {
            classPath = getProjectClassPath(path);

        } catch (IOException ex) {
            log.error("Unable to get classpath", ex);
            return Optional.absent();
        }

        log.info("Determined classpath as {}", classPath.toString());
        log.info("Found following source files: {}", sourceFiles.toString());

        TestScanner scanner = testScanner.get();
        scanner.setClassPath(classPath.toString());
        for (File sourceFile : sourceFiles.getSources()) {
            scanner.addSource(sourceFile);
        }

        List<TestMethod> tests = scanner.findTests();
        scanner.clearSources();

        return Optional.of(ExerciseDesc.from(exerciseName, tests));
    }

    @Override
    public RunResult runTests(Path projectRootPath) {
        CompileResult compileResult = build(projectRootPath);
        if (compileResult.getStatusCode() != 0) {
            return runResultFromFailedCompilation(compileResult);
        }

        try {
            TestRunFileAndLogs results = createRunResultFile(projectRootPath, compileResult);
            RunResult result = resultParser.parseTestResult(results);
            results.getTestResultsFile().delete();
            return result;
        } catch (TestRunnerException ex) {
            log.error("Unable to create run result file", ex);
            return new RunResult(RunResult.Status.TESTRUN_INTERRUPTED,
                    ImmutableList.<TestResult>of(), ImmutableMap.<String, byte[]>of());
        } catch (TestScannerException ex) {
            log.error("Unable to create run result file", ex);
            return new RunResult(RunResult.Status.COMPILE_FAILED, ImmutableList.<TestResult>of(),
                    ImmutableMap.<String, byte[]>of());
        }
    }

    protected RunResult runResultFromFailedCompilation(CompileResult compileResult) {
        Map<String, byte[]> logs = new HashMap<>();
        logs.put(SpecialLogs.STDOUT, compileResult.getStdout());
        logs.put(SpecialLogs.STDERR, compileResult.getStderr());

        return new RunResult(
                RunResult.Status.COMPILE_FAILED,
                ImmutableList.copyOf(new ArrayList<TestResult>()),
                ImmutableMap.copyOf(logs));
    }
}
