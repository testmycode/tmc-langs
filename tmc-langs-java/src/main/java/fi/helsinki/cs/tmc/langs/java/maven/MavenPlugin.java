package fi.helsinki.cs.tmc.langs.java.maven;

import fi.helsinki.cs.tmc.langs.CompileResult;
import fi.helsinki.cs.tmc.langs.java.AbstractJavaPlugin;
import fi.helsinki.cs.tmc.langs.java.ClassPath;
import fi.helsinki.cs.tmc.langs.java.exception.TestRunnerException;
import fi.helsinki.cs.tmc.langs.java.exception.TestScannerException;
import fi.helsinki.cs.tmc.langs.java.testscanner.TestScanner;
import fi.helsinki.cs.tmc.langs.sandbox.SubmissionProcessor;

import org.apache.maven.cli.MavenCli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A {@link fi.helsinki.cs.tmc.langs.LanguagePlugin} that defines the behaviour for Java projects
 * that use Apache Maven.
 */
public class MavenPlugin extends AbstractJavaPlugin {

    private static final String POM_LOCATION = File.separatorChar + "pom.xml";
    private static final String RESULT_FILE = File.separatorChar + "target" + File.separatorChar
            + "test_output.txt";
    private static final String TEST_FOLDER = File.separatorChar + "src";
    private static final String TEST_RUNNER_GOAL = "fi.helsinki.cs.tmc:tmc-maven-plugin:1.6:test";

    private Logger log = LoggerFactory.getLogger(MavenPlugin.class);

    /**
     * Creates a new MavenPlugin.
     */
    public MavenPlugin() {
        super(TEST_FOLDER,
                new SubmissionProcessor(new MavenFileMovingPolicy()),
                new TestScanner());
    }

    @Override
    protected boolean isExerciseTypeCorrect(Path path) {
        return new File(path.toString() + POM_LOCATION).exists();
    }

    @Override
    public String getLanguageName() {
        return "apache-maven";
    }

    @Override
    protected ClassPath getProjectClassPath(Path projectRoot) throws IOException {
        ClassPath testClassPath = MavenClassPathBuilder.fromProjectBasePath(projectRoot);
        testClassPath.add(Paths.get(projectRoot.toString() + File.separatorChar + "target"
                + File.separatorChar + "classes"));
        testClassPath.add(Paths.get(projectRoot.toString() + File.separatorChar + "target"
                + File.separatorChar + "test-classes"));
        return testClassPath;
    }

    @Override
    protected CompileResult build(Path path) {

        log.info("Building maven project at {}", path);

        MavenCli maven = new MavenCli();

        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuf = new ByteArrayOutputStream();

        int compileResult = maven.doMain(new String[]{"clean", "compile", "test-compile"},
                path.toAbsolutePath().toString(),
                new PrintStream(outBuf),
                new PrintStream(errBuf));

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

        MavenCli maven = new MavenCli();

        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuf = new ByteArrayOutputStream();

        int compileResult = maven.doMain(new String[]{TEST_RUNNER_GOAL},
                path.toAbsolutePath().toString(),
                new PrintStream(outBuf),
                new PrintStream(errBuf));

        if (compileResult != 0) {
            log.error("Could not run tests for maven project at {}", path);
            throw new TestRunnerException();
        }

        log.info("Successfully ran tests for maven project at {}", path);
        return new File(path.toString() + RESULT_FILE);
    }
}
