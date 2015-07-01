package fi.helsinki.cs.tmc.langs.java;

import fi.helsinki.cs.tmc.langs.AbstractLanguagePlugin;
import fi.helsinki.cs.tmc.langs.CompileResult;
import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.langs.SpecialLogs;
import fi.helsinki.cs.tmc.langs.TestResult;
import fi.helsinki.cs.tmc.langs.java.exception.TestRunnerException;
import fi.helsinki.cs.tmc.langs.java.exception.TestScannerException;
import fi.helsinki.cs.tmc.langs.java.testscanner.TestScanner;
import fi.helsinki.cs.tmc.langs.sandbox.SubmissionProcessor;
import fi.helsinki.cs.tmc.langs.utils.SourceFiles;
import fi.helsinki.cs.tmc.stylerunner.CheckstyleRunner;
import fi.helsinki.cs.tmc.stylerunner.exception.TMCCheckstyleException;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;

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
    private final TestScanner testScanner;

    /**
     * Creates a new AbstractJavaPlugin.
     */
    public AbstractJavaPlugin(Path testFolderPath,
                              SubmissionProcessor submissionProcessor,
                              TestScanner testScanner) {
        super(submissionProcessor);
        this.testFolderPath = testFolderPath;
        this.testScanner = testScanner;
    }

    protected abstract ClassPath getProjectClassPath(Path path) throws IOException;

    protected abstract CompileResult build(Path projectRootPath);

    protected abstract File createRunResultFile(Path path)
            throws TestRunnerException, TestScannerException;

    @Override
    public ValidationResult checkCodeStyle(Path path) {
        try {
            CheckstyleRunner runner = new CheckstyleRunner(path.toFile(), new Locale("fi"));

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

        SourceFiles sourceFiles = new SourceFiles();
        sourceFiles.addSource(path.resolve(testFolderPath).toFile());

        ClassPath classPath;
        try {
            classPath = getProjectClassPath(path);
        } catch (IOException ex) {
            log.error("Unable to get classpath", ex);
            return Optional.absent();
        }

        return testScanner.findTests(classPath, sourceFiles, exerciseName);
    }

    @Override
    public RunResult runTests(Path projectRootPath) {
        CompileResult compileResult = build(projectRootPath);
        if (compileResult.getStatusCode() != 0) {
            return runResultFromFailedCompilation(compileResult);
        }

        File resultFile = null;
        try {
            resultFile = createRunResultFile(projectRootPath);
        } catch (TestRunnerException | TestScannerException ex) {
            log.error("Unable to create run result file", ex);
            return null;
        }

        RunResult result = resultParser.parseTestResult(resultFile);
        resultFile.delete();

        return result;
    }

    protected RunResult runResultFromFailedCompilation(CompileResult compileResult) {
        Map<String, byte[]> logs = new HashMap<>();
        logs.put(SpecialLogs.STDOUT, compileResult.getStdout());
        logs.put(SpecialLogs.STDERR, compileResult.getStderr());

        return new RunResult(RunResult.Status.COMPILE_FAILED,
                ImmutableList.copyOf(new ArrayList<TestResult>()),
                ImmutableMap.copyOf(logs));
    }
}
