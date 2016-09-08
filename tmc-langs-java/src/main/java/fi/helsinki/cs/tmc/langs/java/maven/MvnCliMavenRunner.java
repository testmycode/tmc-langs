package fi.helsinki.cs.tmc.langs.java.maven;

import org.apache.maven.cli.Hack;
import org.apache.maven.cli.MavenCli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class MvnCliMavenRunner implements MavenTaskRunner {

    @Override
    public MavenExecutionResult exec(Path projectPath, String[] mavenArgs) {
//         System.setProperty(
//         MavenCli.MULTIMODULE_PROJECT_DIRECTORY, projectPath.toAbsolutePath().toString());

        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuf = new ByteArrayOutputStream();

        System.out.println(((URLClassLoader) (Thread.currentThread().getContextClassLoader())).getURLs());
        try {
            Class aaa = Class.forName("org.apache.maven.eventspy.internal.EventSpyDispatcher");
            System.out.println("CLASS: " + aaa);
        } catch (ClassNotFoundException e) {
            System.out.println("ERROR: " + e);
            throw new RuntimeException(e);
        }
        System.out.println(System.getProperty("java.class.path", "."));
        MavenCli maven = new MavenCli();
        System.out.println("11111111111");
        maven.doMain(new Hack().magic(mavenArgs, projectPath));
        System.out.println("222222222222222222");
        int compileResult =
                maven.doMain(
                        mavenArgs,
                        projectPath.toAbsolutePath().toString(),
                        new PrintStream(outBuf),
                        new PrintStream(errBuf));

        return new MavenTaskRunner.MavenExecutionResult()
                .setExitCode(compileResult)
                .setStdOut(outBuf.toByteArray())
                .setStdErr(errBuf.toByteArray());
    }
}
