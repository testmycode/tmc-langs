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
import fi.helsinki.cs.tmc.langs.utils.SourceFiles;
import fi.helsinki.cs.tmc.stylerunner.CheckstyleRunner;
import fi.helsinki.cs.tmc.stylerunner.exception.TMCCheckstyleException;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractJavaPlugin extends AbstractLanguagePlugin {

    private static final Logger log = Logger.getLogger(AbstractJavaPlugin.class.getName());

    private final TestResultParser resultParser = new TestResultParser();
    private final String testFolderPath;

    public AbstractJavaPlugin(String testFolderPath) {
        this.testFolderPath = testFolderPath;
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
            log.log(Level.SEVERE, "Error running checkstyle:", ex);
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
            log.log(Level.SEVERE, null, ex);
            return Optional.absent();
        }

        TestScanner scanner = new TestScanner();
        return scanner.findTests(classPath, sourceFiles, exerciseName);
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
            log.log(Level.WARNING, null, ex);
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
