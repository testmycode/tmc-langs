package fi.helsinki.cs.tmc.langs.java.maven;

import com.google.common.base.Strings;
import fi.helsinki.cs.tmc.langs.java.maven.MavenTaskRunner.MavenExecutionResult;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenExecutors {
    private static final Logger log = LoggerFactory.getLogger(MavenExecutors.class);

    public static final MavenExecutionResult tryAndExec(Path directory, String[] mavenArgs) {
        MavenTaskRunner runner;
        if (!Strings.isNullOrEmpty(System.getenv("M3_HOME"))) {
            log.info("Selected MavenInvokator");
            runner = new MavenInvokatorMavenTaskRunner();
        } else {
            log.info("Selected MvnCli");
            runner = new MvnCliMavenRunner();
        }
        return runner.exec(directory, mavenArgs);
    }
}
