package fi.helsinki.cs.tmc.langs.java.maven;

import fi.helsinki.cs.tmc.langs.java.ClassPath;
import java.io.ByteArrayOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;
import org.apache.maven.cli.MavenCli;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.MavenInvocationException;

/**
 * Creates a complete ClassPath for a Maven project by calling the
 * dependency:build-classpath goal.
 */
public final class MavenClassPathBuilder {

    private static final String CLASS_PATH_GOAL
            = "org.apache.maven.plugins:maven-dependency-plugin:2.10:build-classpath";
    private static final String OUTPUT_FILE_PARAM_PREFIX = "-Dmdep.outputFile=";

    private static final Logger log = LoggerFactory.getLogger(MavenClassPathBuilder.class);

    /**
     * Create a complete ClassPath for the given Maven project.
     */
    public static ClassPath fromProjectBasePath(Path projectPath) throws IOException {

        log.info("Building classpath for maven project at {}", projectPath);

        File outputFile = File.createTempFile("tmc-classpath", ".tmp");

//        InvocationRequest request = new DefaultInvocationRequest();
//        DefaultInvoker invoker = new DefaultInvoker();
//        invoker.setMavenHome(new File("/usr/local/Cellar/maven/3.3.3/"));
//
//        InvocationResult result = null;
//        request.setPomFile(projectPath.resolve("pom.xml").toFile());
//        request.setBaseDirectory(projectPath.toFile());
//
//        request.setDebug(true);
//
        String outputParameter = OUTPUT_FILE_PARAM_PREFIX + outputFile.getAbsolutePath();
//        request.setGoals(Arrays.asList(new String[]{CLASS_PATH_GOAL, outputParameter, "-e"}));
//
//        try {
//            result = invoker.execute(request);
//        } catch (MavenInvocationException e) {
//            // TODO Auto-generated catch block
//            throw new RuntimeException(e);
//        }


        String multimoduleProjectDirectory =
                System.getProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY);
        System.setProperty(
                MavenCli.MULTIMODULE_PROJECT_DIRECTORY, projectPath.toAbsolutePath().toString());

        MavenCli maven = new MavenCli();

        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuf = new ByteArrayOutputStream();

        System.out.println("CLASS_PATH_GOAL: " + CLASS_PATH_GOAL);
        System.out.println("outputParameter: " + outputParameter);
        System.out.println("projectPath.toAbsolutePath().toString(): " + projectPath.toAbsolutePath().toString());

        int compileResult =
                maven.doMain(
                        new String[] {CLASS_PATH_GOAL, outputParameter, "-e"},
                        projectPath.toAbsolutePath().toString(),
                        new PrintStream(outBuf),
                        new PrintStream(errBuf));

        if (multimoduleProjectDirectory != null) {
            System.setProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY, multimoduleProjectDirectory);
        }

        System.out.println("output: " + outBuf.toString());
        System.out.println("err: " + errBuf.toString());
        System.out.println(compileResult);

        Scanner scanner = new Scanner(outputFile);

        String classPathString;
        try {
            classPathString = scanner.nextLine();
        } catch (NoSuchElementException exception) {
            log.error(
                    "Class path output file at {} was empty",
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
//        for (String part : classPathString.split(File.pathSeparator)) {
//            classPath.add(Paths.get(part));
//        }

        log.info("Successfully built class path for maven project at {}", projectPath);

        return classPath;
    }
}
