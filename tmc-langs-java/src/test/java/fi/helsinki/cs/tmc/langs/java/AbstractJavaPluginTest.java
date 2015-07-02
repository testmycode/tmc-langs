package fi.helsinki.cs.tmc.langs.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import fi.helsinki.cs.tmc.langs.domain.CompileResult;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.io.sandbox.StudentFileAwareSubmissionProcessor;
import fi.helsinki.cs.tmc.langs.io.sandbox.SubmissionProcessor;
import fi.helsinki.cs.tmc.langs.java.exception.TestRunnerException;
import fi.helsinki.cs.tmc.langs.java.exception.TestScannerException;
import fi.helsinki.cs.tmc.langs.java.testscanner.TestScanner;
import fi.helsinki.cs.tmc.langs.utils.SourceFiles;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationError;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;

import com.google.common.base.Optional;

import org.junit.Test;

import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class AbstractJavaPluginTest {

    private class StubLanguagePlugin extends AbstractJavaPlugin {

        public StubLanguagePlugin(Path testFolderPath,
                                  SubmissionProcessor submissionProcessor,
                                  TestScanner testScanner) {
            super(testFolderPath, submissionProcessor, testScanner);
        }

        public StubLanguagePlugin(Path testFolderPath) {
            super(testFolderPath, new StudentFileAwareSubmissionProcessor(), new TestScanner());
        }

        @Override
        protected ClassPath getProjectClassPath(Path path) throws IOException {
            return null;
        }

        @Override
        protected CompileResult build(Path projectRootPath) {
            return new CompileResult(0, new byte[0], new byte[0]);
        }

        @Override
        protected File createRunResultFile(Path path)
                throws TestRunnerException, TestScannerException {
            return null;
        }

        @Override
        protected boolean isExerciseTypeCorrect(Path path) {
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
    }

    private PluginImplLanguagePlugin pluginImpl;

    public AbstractJavaPluginTest() {
        pluginImpl = new PluginImplLanguagePlugin();
    }

    @Test
    public void testCheckCodeStyle() {
        Path project = TestUtils.getPath(getClass(), "most_errors");
        ValidationResult result = pluginImpl.checkCodeStyle(project);
        Map<File, List<ValidationError>> res = result.getValidationErrors();
        assertEquals("Should be one erroneous file", 1, res.size());
        for (File file : res.keySet()) {
            List<ValidationError> errors = res.get(file);
            assertEquals("Should return the right amount of errors", 23, errors.size());
        }
    }

    @Test
    public void testCheckCodeStyleWithUntestableProject() {
        Path project = TestUtils.getPath(getClass(), "dummy_project");
        ValidationResult result = pluginImpl.checkCodeStyle(project);
        assertNull(result);
    }

    @Test
    public void exceptionOnGetClassPathReturnsAbsentDuringScanExercise() {
        Path path = TestUtils.getPath(getClass(), "trivial");
        AbstractJavaPlugin plugin = new StubLanguagePlugin(path) {
            @Override
            protected boolean isExerciseTypeCorrect(Path path) {
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
    public void testRunnerExceptionDuringRunTestsReturnsNull() {
        AbstractJavaPlugin plugin = new StubLanguagePlugin(Paths.get("")) {
            @Override
            protected File createRunResultFile(Path path)
                    throws TestRunnerException, TestScannerException {
                throw new TestRunnerException();
            }
        };

        assertNull(plugin.runTests(null));
    }

    @Test
    public void testScannerExceptionDuringRunTestsReturnsNull() {
        AbstractJavaPlugin plugin = new StubLanguagePlugin(Paths.get("")) {
            @Override
            protected File createRunResultFile(Path path)
                    throws TestRunnerException, TestScannerException {
                throw new TestScannerException();
            }
        };

        assertNull(plugin.runTests(null));
    }

    @Test
    public void scanExerciseReturnsAbsentOnInvalidProject() {
        AbstractJavaPlugin plugin = new StubLanguagePlugin(Paths.get(""));
        Optional<ExerciseDesc> result = plugin.scanExercise(null, "");
        assertFalse(result.isPresent());
    }

    @Test
    public void scanExerciseAddsSourceFilesFromProject() {
        Path path = TestUtils.getPath(getClass(), "trivial");
        TestScanner scanner = mock(TestScanner.class);
        AbstractJavaPlugin plugin = new StubLanguagePlugin(
                Paths.get(""), new StudentFileAwareSubmissionProcessor(), scanner) {
            protected boolean isExerciseTypeCorrect(Path path) {
                return true;
            }
        };
        ArgumentCaptor<SourceFiles> sourceFilesCaptor = ArgumentCaptor.forClass(SourceFiles.class);

        plugin.scanExercise(path, "trivial");

        verify(scanner).findTests(any(ClassPath.class), sourceFilesCaptor.capture(), anyString());

        SourceFiles sourceFiles = sourceFilesCaptor.getValue();
        File expected = TestUtils.getPath(getClass(), "trivial/test/TrivialTest.java").toFile();

        assertFalse(sourceFiles.getSources().isEmpty());
        assertTrue(sourceFiles.getSources().contains(expected));
    }
}
