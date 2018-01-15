package fi.helsinki.cs.tmc.langs.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import fi.helsinki.cs.tmc.langs.abstraction.ValidationError;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.CompileResult;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.io.sandbox.StudentFileAwareSubmissionProcessor;
import fi.helsinki.cs.tmc.langs.io.sandbox.SubmissionProcessor;
import fi.helsinki.cs.tmc.langs.java.exception.TestRunnerException;
import fi.helsinki.cs.tmc.langs.java.exception.TestScannerException;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import com.google.common.base.Optional;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AbstractJavaPluginTest {

    private class StubLanguagePlugin extends AbstractJavaPlugin {

        public StubLanguagePlugin(
                Path testFolderPath,
                SubmissionProcessor submissionProcessor,
                LazyTestScanner testScanner) {
            super(testFolderPath, submissionProcessor, testScanner);
        }

        public StubLanguagePlugin(Path testFolderPath) {
            super(testFolderPath, new StudentFileAwareSubmissionProcessor(), new LazyTestScanner());
        }

        @Override
        protected ClassPath getProjectClassPath(Path path) throws IOException {
            return new ClassPath(Paths.get(""));
        }

        @Override
        protected CompileResult build(Path projectRootPath) {
            return new CompileResult(0, new byte[0], new byte[0]);
        }

        @Override
        protected TestRunFileAndLogs createRunResultFile(Path path, CompileResult compileResult)
                throws TestRunnerException, TestScannerException {
            return null;
        }

        @Override
        public boolean isExerciseTypeCorrect(Path path) {
            return false;
        }

        @Override
        protected StudentFilePolicy getStudentFilePolicy(Path projectPath) {
            return null;
        }

        @Override
        public String getLanguageName() {
            return null;
        }

        @Override
        public String getPluginName() {
            return null;
        }

        @Override
        public void clean(Path path) {
            // noop
        }
    }

    private PluginImplLanguagePlugin pluginImpl;

    public AbstractJavaPluginTest() {
        pluginImpl = new PluginImplLanguagePlugin();
    }

    @Test
    public void testCheckCodeStyle() {
        Path project = TestUtils.getPath(getClass(), "most_errors");
        ValidationResult result = pluginImpl.checkCodeStyle(project, new Locale("en"));
        Map<File, List<ValidationError>> res = result.getValidationErrors();
        assertEquals("Should be one erroneous file", 1, res.size());
        for (File file : res.keySet()) {
            List<ValidationError> errors = res.get(file);
            assertEquals("Should return the right amount of errors", 24, errors.size());
        }
    }

    @Test
    public void testCheckCodeStyleWithUntestableProject() {
        Path project = TestUtils.getPath(getClass(), "dummy_project");
        ValidationResult result = pluginImpl.checkCodeStyle(project, new Locale("en"));
        assertNull(result);
    }

    @Test
    public void exceptionOnGetClassPathReturnsAbsentDuringScanExercise() {
        Path path = TestUtils.getPath(getClass(), "trivial");
        AbstractJavaPlugin plugin =
                new StubLanguagePlugin(path) {
                    @Override
                    public boolean isExerciseTypeCorrect(Path path) {
                        return true;
                    }

                    @Override
                    protected ClassPath getProjectClassPath(Path path) throws IOException {
                        throw new IOException();
                    }
                };

        assertFalse(plugin.scanExercise(path, "").isPresent());
    }

    @Test
    public void testRunnerExceptionDuringRunTestsReturnsStatusTestRunInterrupted() {
        AbstractJavaPlugin plugin =
                new StubLanguagePlugin(Paths.get("")) {
                    @Override
                    protected TestRunFileAndLogs createRunResultFile(Path path,
                                                                     CompileResult compileResult)
                            throws TestRunnerException, TestScannerException {
                        throw new TestRunnerException();
                    }
                };

        assertEquals(plugin.runTests(null).status, RunResult.Status.TESTRUN_INTERRUPTED);
    }

    @Test
    public void testScannerExceptionDuringRunTestsReturnsStatusCompileFailed() {
        AbstractJavaPlugin plugin =
                new StubLanguagePlugin(Paths.get("")) {
                    @Override
                    protected TestRunFileAndLogs createRunResultFile(Path path,
                                                                     CompileResult compileResult)
                            throws TestRunnerException, TestScannerException {
                        throw new TestScannerException();
                    }
                };

        assertEquals(plugin.runTests(null).status, RunResult.Status.COMPILE_FAILED);
    }

    @Test
    public void scanExerciseReturnsAbsentOnInvalidProject() {
        AbstractJavaPlugin plugin = new StubLanguagePlugin(Paths.get(""));
        Optional<ExerciseDesc> result = plugin.scanExercise(null, "");
        assertFalse(result.isPresent());
    }
}
