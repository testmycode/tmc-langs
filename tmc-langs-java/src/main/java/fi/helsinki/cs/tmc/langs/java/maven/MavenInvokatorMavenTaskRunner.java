package fi.helsinki.cs.tmc.langs.java.maven;

import fi.helsinki.cs.tmc.langs.java.exception.MavenExecutorException;

import com.google.common.base.Preconditions;

import org.apache.commons.lang3.SystemUtils;
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
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

public class MavenInvokatorMavenTaskRunner implements MavenTaskRunner {

    private static final Logger log = LoggerFactory.getLogger(MavenInvokatorMavenTaskRunner.class);

    private static final String MAVEN_OPTS = "-Dmaven.compiler.source=1.8 "
            + " -Dmaven.compiler.target=1.8"
            + " -XX:+TieredCompilation -XX:TieredStopAtLevel=1";

    private static final Path WINDOWS_JAVA_DEFAULT_FOLDER = Paths.get("C:\\Program Files\\Java");

    @Override
    public MavenExecutionResult exec(Path projectPath, String[] mavenArgs) {

        InvocationRequest request = new DefaultInvocationRequest();
        request.setMavenOpts(MAVEN_OPTS);

        try {
            String javaHome = System.getenv("JAVA_HOME");
            if (SystemUtils.IS_OS_WINDOWS
                    && (javaHome == null || javaHome.trim().isEmpty()
                        || !Files.exists(Paths.get(javaHome.trim())))) {
                String currentJava = System.getProperty("java.version").trim();
                Optional<Path> foundHome = Files.list(WINDOWS_JAVA_DEFAULT_FOLDER)
                        .filter(Files::isDirectory)
                        .filter(path -> {
                            return path.getFileName().toString().contains(currentJava);
                        }).findAny();
                if (foundHome.isPresent()) {
                    Path home = foundHome.get();
                    request.setJavaHome(home.toFile());
                }
            }
        } catch (Exception e) {
            log.debug("Could not fix java home", e);
        }

        try {
            String jdkhome = System.getenv("jdkhome");
            if (jdkhome != null) {
                Path jdkhomePath = Paths.get(jdkhome);
                if (jdkhome != null && jdkhome.length() > 0 && Files.exists(jdkhomePath)) {
                    request.setJavaHome(jdkhomePath.toFile());
                }
            }
        } catch (Exception e) {
            log.debug("jdkhome variable not valid, skipping", e);
        }

        String mavenHome = getMavenHome();

        log.info("Using maven at: {}", mavenHome);

        DefaultInvoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(mavenHome));

        final ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        final ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
        final PrintStream out = new PrintStream(outBuf);
        final PrintStream err = new PrintStream(errBuf);

        InvocationResult result = null;
        request.setPomFile(projectPath.resolve("pom.xml").toFile());
        request.setBaseDirectory(projectPath.toFile());
        request.setOutputHandler(line -> {
            log.info("MavenInvokator: {}", line);
            out.println(line);
        });
        request.setErrorHandler(line -> {
            log.info("MavenInvokator: {}", line);
            err.println(line);
        });

        request.setGoals(Arrays.asList(mavenArgs));

        MavenExecutionResult compilationResult = new MavenExecutionResult();
        try {
            result = invoker.execute(request);
            compilationResult.setExitCode(result.getExitCode());
            // outBuf and errBuf are empty until invoker is executed
            compilationResult.setStdOut(outBuf.toByteArray());
            compilationResult.setStdErr(errBuf.toByteArray());
            CommandLineException exp = result.getExecutionException();
            if (exp != null) {
                throw new MavenExecutorException(exp);
            }
            return compilationResult;
        } catch (MavenInvocationException e) {
            throw new MavenExecutorException(e);
        }
    }

    public String getMavenHome() {
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
        return mavenHome;
    }

    private Path useBundledMaven() {
        Path mavenHome = getConfigDirectory();
        Path extractedMavenLocation = mavenHome.resolve("apache-maven-3.5.4");
        if (Files.exists(extractedMavenLocation)) {
            log.info("Maven already extracted");

            // Add the name of the extracted folder to the path
            return extractedMavenLocation;
        }
        log.info("Maven bundle not previously extracted, extracting...");
        try {
            InputStream data = getClass().getResourceAsStream("apache-maven-3.5.4.zip");
            Preconditions.checkNotNull(
                    data, "Couldn't load bundled maven from tmc-langs-java.jar.");
            Path tmpFile = Files.createTempFile("tmc-maven", "zip");
            Files.copy(data, tmpFile, StandardCopyOption.REPLACE_EXISTING);
            Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.ZIP);
            archiver.extract(tmpFile.toFile(), mavenHome.toFile());
            tryToChmod(extractedMavenLocation.resolve("bin").resolve("mvn"));
            try {
                Files.deleteIfExists(tmpFile);
            } catch (IOException e) {
                log.warn("Deleting tmp apache-maven.zip failed", e);
            }

            return extractedMavenLocation;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void tryToChmod(Path path) {
        try {
            Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path);
            if (!permissions.contains(PosixFilePermission.OWNER_EXECUTE)) {
                permissions.add(PosixFilePermission.OWNER_EXECUTE);
                Files.setPosixFilePermissions(path, permissions);
            }
        } catch (IOException ex) { }
    }

    static Path getConfigDirectory() {
        Path configPath;

        if (SystemUtils.IS_OS_WINDOWS) {
            String appdata = System.getenv("APPDATA");
            if (appdata == null) {
                configPath = Paths.get(System.getProperty("user.home"));
            } else {
                configPath = Paths.get(appdata);
            }
        } else {
            // Assume we're using Unix (Linux, Mac OS X or *BSD)
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
