package fi.helsinki.cs.tmc.langs.java.maven;

import fi.helsinki.cs.tmc.langs.java.exception.MavenExecutorException;

import com.google.common.base.Preconditions;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.MavenInvocationException;

import org.codehaus.plexus.util.cli.CommandLineException;

import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class MavenInvokatorMavenTaskRunner implements MavenTaskRunner {

    private static final Logger log = LoggerFactory.getLogger(MavenInvokatorMavenTaskRunner.class);

    private static final String MAVEN_OPTS =
            "-Dmaven.compiler.source=1.8 -Dmaven.compiler.target=1.8";

    @Override
    public MavenExecutionResult exec(Path projectPath, String[] mavenArgs) {

        InvocationRequest request = new DefaultInvocationRequest();
        request.setMavenOpts(MAVEN_OPTS);

        DefaultInvoker invoker = new DefaultInvoker();
        String mavenHome = System.getenv("M3_HOME");
        if (mavenHome == null) {
            mavenHome = System.getenv("M2_HOME");
        }
        if (mavenHome == null) {
            mavenHome = System.getenv("MAVEN_HOME");
        }
        if (mavenHome == null) {
            mavenHome = System.getProperty("maven.home");
        }
        if (mavenHome == null) {
            mavenHome = useBundledMaven().toString();
        }

        log.info("Using maven at: {}", mavenHome);

        invoker.setMavenHome(new File(mavenHome));

        final ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        final PrintStream out = new PrintStream(outBuf);

        InvocationResult result = null;
        request.setPomFile(projectPath.resolve("pom.xml").toFile());
        request.setBaseDirectory(projectPath.toFile());
        request.setOutputHandler(
                new InvocationOutputHandler() {

                    @Override
                    public void consumeLine(String line) {
                        log.info("MavenInvokator: m{}", line);
                        out.append(line);
                    }
                });

        request.setGoals(Arrays.asList(mavenArgs));

        MavenExecutionResult compilationResult =
                new MavenExecutionResult().setStdOut(outBuf.toByteArray()).setStdErr(new byte[0]);
        try {
            result = invoker.execute(request);
            compilationResult.setExitCode(result.getExitCode());
            CommandLineException exp = result.getExecutionException();
            if (exp != null) {
                throw new MavenExecutorException(exp);
            }
            return compilationResult;
        } catch (MavenInvocationException e) {
            throw new MavenExecutorException(e);
        }
    }

    private Path useBundledMaven() {
        Path mavenHome = getConfigDirectory();
        if (Files.exists(mavenHome)) {
            log.info("Maven already extracted");

            // Add the name of the extracted folder to the path
            return mavenHome.resolve("apache-maven-3.3.9");
        }
        log.info("Maven bundle not previously extracted, extracting...");
        try {

            InputStream data = getClass().getResourceAsStream("apache-maven-3.3.9.zip");
            Preconditions.checkNotNull(data, "Couldn't load bundled maven from tmc-langs-java.jar.");
            Path tmpFile = File.createTempFile("tmc-maven", "zip").toPath();
            Files.copy(data, tmpFile, StandardCopyOption.REPLACE_EXISTING);
            Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.ZIP);
            archiver.extract(tmpFile.toFile(), mavenHome.toFile());

            // Add the name of the extracted folder to the path
            return mavenHome.resolve("apache-maven-3.3.9");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Path getConfigDirectory() {
        Path configPath;

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            String appdata = System.getenv("APPDATA");
            if (appdata == null) {
                configPath = Paths.get(System.getProperty("user.home"));
            } else {
                configPath = Paths.get(appdata);
            }
        } else {
            //Assume we're using Unix (Linux, Mac OS X or *BSD)
            String configEnv = System.getenv("XDG_CONFIG_HOME");

            if (configEnv != null && configEnv.length() > 0) {
                configPath = Paths.get(configEnv);
            } else {
                configPath = Paths.get(System.getProperty("user.home")).resolve(".config");
            }
        }
        return configPath.resolve("tmc");
    }

}
