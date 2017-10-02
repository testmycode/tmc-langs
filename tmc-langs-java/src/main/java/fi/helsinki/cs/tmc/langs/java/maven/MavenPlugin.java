package fi.helsinki.cs.tmc.langs.java.maven;

import fi.helsinki.cs.tmc.langs.domain.CompileResult;
import fi.helsinki.cs.tmc.langs.domain.ExercisePackagingConfiguration;
import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.io.sandbox.StudentFileAwareSubmissionProcessor;
import fi.helsinki.cs.tmc.langs.java.AbstractJavaPlugin;
import fi.helsinki.cs.tmc.langs.java.ClassPath;
import fi.helsinki.cs.tmc.langs.java.LazyTestScanner;
import fi.helsinki.cs.tmc.langs.java.TestRunFileAndLogs;
import fi.helsinki.cs.tmc.langs.java.exception.TestRunnerException;
import fi.helsinki.cs.tmc.langs.java.exception.TestScannerException;
import fi.helsinki.cs.tmc.langs.java.maven.MavenTaskRunner.MavenExecutionResult;
import fi.helsinki.cs.tmc.testscanner.TestScanner;

import com.google.common.collect.ImmutableList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A {@link fi.helsinki.cs.tmc.langs.LanguagePlugin} that defines the behaviour
 * for Java projects that use Apache Maven.
 */
public final class MavenPlugin extends AbstractJavaPlugin {

    private static final Logger log = LoggerFactory.getLogger(MavenPlugin.class);

    private static final Path POM_FILE = Paths.get("pom.xml");
    private static final Path RESULT_FILE = Paths.get("target", "test_output.txt");
    private static final Path TEST_FOLDER = Paths.get("src");

    private static final String TEST_RUNNER_GOAL = "fi.helsinki.cs.tmc:tmc-maven-plugin:1.6:test";

    /**
     * Creates a new MavenPlugin.
     */
    public MavenPlugin() {
        super(TEST_FOLDER, new StudentFileAwareSubmissionProcessor(), new LazyTestScanner());
    }

    @Override
    public boolean isExerciseTypeCorrect(Path path) {
        return Files.exists(path.toAbsolutePath().resolve(POM_FILE));
    }

    @Override
    public String getPluginName() {
        return "apache-maven";
    }

    @Override
    protected StudentFilePolicy getStudentFilePolicy(Path path) {
        return new MavenStudentFilePolicy(path);
    }

    @Override
    protected ClassPath getProjectClassPath(Path projectRoot) throws IOException {
        ClassPath testClassPath = MavenClassPathBuilder.fromProjectBasePath(projectRoot);
        testClassPath.add(projectRoot.resolve("target").resolve("classes"));
        testClassPath.add(projectRoot.resolve("target").resolve("test-classes"));
        return testClassPath;
    }

    @Override
    protected CompileResult build(Path path) {

        log.info("Building maven project at {}", path);

        MavenExecutionResult compilationResult =
                MavenExecutors.tryAndExec(path, new String[] {"clean", "compile", "test-compile"});

        if (compilationResult.getExitCode() == 0) {
            log.info("Built maven project at {}", path);
        } else {
            log.info("Failed to build maven project at {}", path);
        }

        return new CompileResult(
                compilationResult.getExitCode(),
                compilationResult.getStdOut(),
                compilationResult.getStdErr());
    }

    @Override
    protected TestRunFileAndLogs createRunResultFile(Path path)
            throws TestRunnerException, TestScannerException {

        log.info("Running tests for maven project at {}", path);

        MavenExecutionResult result =
                MavenExecutors.tryAndExec(path, new String[] {TEST_RUNNER_GOAL});

        if (result.getExitCode() != 0) {
            log.error("Could not run tests for maven project at {}", path);
            throw new TestRunnerException();
        }

        log.info("Successfully ran tests for maven project at {}", path);
        return new TestRunFileAndLogs(
                path.toAbsolutePath().resolve(RESULT_FILE).toFile(),
                result.getStdOut(),
                result.getStdErr());
    }

    // TODO: ADD extra student file support to here too
    @Override
    public ExercisePackagingConfiguration getExercisePackagingConfiguration(Path path) {
        return new ExercisePackagingConfiguration(
                ImmutableList.of("src/main"), ImmutableList.of("src/test"));
    }

    @Override
    public void clean(Path path) {
        log.info("Cleaning maven project at {}", path);

        MavenExecutionResult compilationResult =
                MavenExecutors.tryAndExec(path, new String[] {"clean"});

        if (compilationResult.getExitCode() == 0) {
            log.info("Cleaned maven project at {}", path);
        } else {
            log.info("Failed to cleaning maven project at {}", path);
        }
    }
}
