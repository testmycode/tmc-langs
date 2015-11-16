package fi.helsinki.cs.tmc.langs.java.maven;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.MavenInvocationException;

import org.codehaus.plexus.util.cli.CommandLineException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;

public class MavenInvokatorMavenTaskRunner implements MavenTaskRunner {

    private static final Logger log = LoggerFactory.getLogger(MavenInvokatorMavenTaskRunner.class);

    @Override
    public MavenExecutionResult exec(Path projectPath, String[] mavenArgs) {

        InvocationRequest request = new DefaultInvocationRequest();
        DefaultInvoker invoker = new DefaultInvoker();

        invoker.setMavenHome(new File(System.getenv("M3_HOME")));

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
                throw new RuntimeException(exp);
            }
            return compilationResult;
        } catch (MavenInvocationException e) {
            throw new RuntimeException(e);
        }
    }
}
