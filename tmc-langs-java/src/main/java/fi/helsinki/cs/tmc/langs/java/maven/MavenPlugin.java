package fi.helsinki.cs.tmc.langs.java.maven;

import fi.helsinki.cs.tmc.langs.domain.CompileResult;
import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.io.sandbox.StudentFileAwareSubmissionProcessor;
import fi.helsinki.cs.tmc.langs.java.AbstractJavaPlugin;
import fi.helsinki.cs.tmc.langs.java.ClassPath;
import fi.helsinki.cs.tmc.langs.java.exception.TestRunnerException;
import fi.helsinki.cs.tmc.langs.java.exception.TestScannerException;
import fi.helsinki.cs.tmc.langs.java.testscanner.TestScanner;

import org.apache.maven.cli.MavenCli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
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
        super(TEST_FOLDER, new StudentFileAwareSubmissionProcessor(), new TestScanner());
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

        String multimoduleProjectDirectory =
                System.getProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY);
        System.setProperty(
                MavenCli.MULTIMODULE_PROJECT_DIRECTORY, path.toAbsolutePath().toString());

        MavenCli maven = new MavenCli();

        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuf = new ByteArrayOutputStream();

        int compileResult =
                maven.doMain(
                        new String[] {"clean", "compile", "test-compile"},
                        path.toAbsolutePath().toString(),
                        new PrintStream(outBuf),
                        new PrintStream(errBuf));

        if (multimoduleProjectDirectory != null) {
            System.setProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY, multimoduleProjectDirectory);
        }

        if (compileResult == 0) {
            log.info("Built maven project at {}", path);
        } else {
            log.info("Failed to build maven project at {}", path);
        }

        return new CompileResult(compileResult, outBuf.toByteArray(), errBuf.toByteArray());
    }

    @Override
    protected File createRunResultFile(Path path) throws TestRunnerException, TestScannerException {

        log.info("Running tests for maven project at {}", path);

        String multimoduleProjectDirectory =
                System.getProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY);
        System.setProperty(
                MavenCli.MULTIMODULE_PROJECT_DIRECTORY, path.toAbsolutePath().toString());

        MavenCli maven = new MavenCli();

        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuf = new ByteArrayOutputStream();

        int compileResult =
                maven.doMain(
                        new String[] {TEST_RUNNER_GOAL},
                        path.toAbsolutePath().toString(),
                        new PrintStream(outBuf),
                        new PrintStream(errBuf));

        if (multimoduleProjectDirectory != null) {
            System.setProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY, multimoduleProjectDirectory);
        }

        if (compileResult != 0) {
            log.error("Could not run tests for maven project at {}", path);
            throw new TestRunnerException();
        }

        log.info("Successfully ran tests for maven project at {}", path);
        return path.toAbsolutePath().resolve(RESULT_FILE).toFile();
    }
}
