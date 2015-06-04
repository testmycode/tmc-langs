package fi.helsinki.cs.tmc.langs.java.maven;

import fi.helsinki.cs.tmc.langs.java.ClassPath;

import org.apache.maven.cli.MavenCli;

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

    /**
     * Create a complete ClassPath for the given Maven project.
     */
    public static ClassPath fromProjectBasePath(Path projectPath) throws IOException {

        File outputFile = File.createTempFile("tmc-classpath", ".tmp");

        String outputParameter = OUTPUT_FILE_PARAM_PREFIX + outputFile.getAbsolutePath();

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
            throw new IOException("Class path  output file is empty");
        } finally {
            outputFile.delete();
        }

        ClassPath classPath = new ClassPath();
        for (String part : classPathString.split(File.pathSeparator)) {
            classPath.add(Paths.get(part));
        }

        return classPath;
    }
}
