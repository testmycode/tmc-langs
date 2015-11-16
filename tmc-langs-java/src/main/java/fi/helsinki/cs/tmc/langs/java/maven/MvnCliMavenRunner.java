package fi.helsinki.cs.tmc.langs.java.maven;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import org.apache.maven.cli.MavenCli;

public class MvnCliMavenRunner implements MavenTaskRunner {

    @Override
    public MavenExecutionResult exec(Path projectPath, String[] mavenArgs) {
        // for MavenCli 3.3.3 update
        //        String multimoduleProjectDirectory =
        //                System.getProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY);
        //        System.setProperty(
        //                MavenCli.MULTIMODULE_PROJECT_DIRECTORY, projectPath.toAbsolutePath().toString());

        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuf = new ByteArrayOutputStream();

        MavenCli maven = new MavenCli();

        int compileResult =
                maven.doMain(
                        mavenArgs,
                        projectPath.toAbsolutePath().toString(),
                        new PrintStream(outBuf),
                        new PrintStream(errBuf));

        //        if (multimoduleProjectDirectory != null) {
        //            System.setProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY, multimoduleProjectDirectory);
        //        }
        return new MavenTaskRunner.MavenExecutionResult()
                .setExitCode(compileResult)
                .setStdOut(outBuf.toByteArray())
                .setStdErr(errBuf.toByteArray());
    }
}
