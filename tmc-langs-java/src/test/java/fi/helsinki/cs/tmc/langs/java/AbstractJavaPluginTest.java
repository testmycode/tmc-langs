package fi.helsinki.cs.tmc.langs.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.CompileResult;
import fi.helsinki.cs.tmc.langs.java.exception.TestRunnerException;
import fi.helsinki.cs.tmc.langs.java.exception.TestScannerException;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationError;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class AbstractJavaPluginTest {

    private class StubLanguagePlugin extends AbstractJavaPlugin {

        public StubLanguagePlugin(String testFolderPath) {
            super(testFolderPath);
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
        public String getLanguageName() {
            return null;
        }
    }

    private PluginImplLanguagePlugin pluginImpl;

    public AbstractJavaPluginTest() {
        pluginImpl = new PluginImplLanguagePlugin();
    }

    @Test
    public void findExercisesReturnsAListOfExerciseDirectories() {
        Path project = TestUtils.getPath(getClass(), "ant_project");
        ImmutableList<Path> dirs = pluginImpl.findExercises(project);
        Path pathOne = TestUtils.getPath(getClass(), "ant_project");
        Path pathTwo = TestUtils.getPath(getClass(), "ant_project/ant_sub_project");
        assertTrue(dirs.contains(pathOne) && dirs.contains(pathTwo));
    }

    @Test
    public void findExercisesReturnsAnEmptyListWhenInvalidPath() {
        Path buildFile = TestUtils.getPath(getClass(), "ant_project/build.xml");
        assertTrue(pluginImpl.findExercises(buildFile).isEmpty());
    }

    @Test
    public void findExercisesReturnsAnEmptyListWhenNoExercisesFound() {
        Path project = TestUtils.getPath(getClass(), "dummy_project");
        assertTrue(pluginImpl.findExercises(project).isEmpty());
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
        AbstractJavaPlugin plugin = new StubLanguagePlugin(path.toString()) {
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
        AbstractJavaPlugin plugin = new StubLanguagePlugin("") {
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
        AbstractJavaPlugin plugin = new StubLanguagePlugin("") {
            @Override
            protected File createRunResultFile(Path path)
                    throws TestRunnerException, TestScannerException {
                throw new TestScannerException();
            }
        };

        assertNull(plugin.runTests(null));
    }
}
