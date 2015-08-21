package fi.helsinki.cs.tmc.langs.java.maven;

import fi.helsinki.cs.tmc.langs.java.ClassPath;

import org.apache.maven.cli.MavenCli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Creates a complete ClassPath for a Maven project by calling the dependency:build-classpath goal.
 */
public class MavenClassPathBuilder {

    private static final String CLASS_PATH_GOAL =
            "org.apache.maven.plugins:maven-dependency-plugin:2.10:build-classpath";
    private static final String OUTPUT_FILE_PARAM_PREFIX = "-Dmdep.outputFile=";

    private static Logger log = LoggerFactory.getLogger(MavenClassPathBuilder.class);

    /**
     * Create a complete ClassPath for the given Maven project.
     */
    public static ClassPath fromProjectBasePath(Path projectPath) throws IOException {

        log.info("Building classpath for maven project at {}", projectPath);

        File outputFile = File.createTempFile("tmc-classpath", ".tmp");

        String outputParameter = OUTPUT_FILE_PARAM_PREFIX + outputFile.getAbsolutePath();

        System.setProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY, projectPath.toAbsolutePath().toString());
        MavenCli maven = new MavenCli();

        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuf = new ByteArrayOutputStream();

        int compileResult = maven.doMain(new String[]{CLASS_PATH_GOAL, outputParameter, "-e"},
                                         projectPath.toAbsolutePath().toString(),
                                         new PrintStream(outBuf),
                                         new PrintStream(errBuf));

        Scanner scanner = new Scanner(outputFile);

        String classPathString;
        try {
            classPathString = scanner.nextLine();
        } catch (NoSuchElementException exception) {
            log.error("Class path output file at {} was empty",
                    outputFile.getAbsolutePath(),
                    exception);
            throw new IOException("Class path output file is empty", exception);
        } finally {
            if (outputFile.delete()) {
                log.info("Deleted temporary output file {}", outputFile);
            } else {
                log.warn("Failed to delete temporary output file {}", outputFile);
            }
        }

        ClassPath classPath = new ClassPath();
        for (String part : classPathString.split(File.pathSeparator)) {
            classPath.add(Paths.get(part));
        }

        log.info("Successfully built class path for maven project at {}", projectPath);

        return classPath;
    }
}
