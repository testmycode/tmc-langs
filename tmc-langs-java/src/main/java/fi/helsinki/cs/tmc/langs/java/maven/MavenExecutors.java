package fi.helsinki.cs.tmc.langs.java.maven;

import fi.helsinki.cs.tmc.langs.java.exception.MavenExecutorException;
import fi.helsinki.cs.tmc.langs.java.maven.MavenTaskRunner.MavenExecutionResult;

import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class MavenExecutors {

    private static final Logger log = LoggerFactory.getLogger(MavenExecutors.class);

    public static final MavenExecutionResult tryAndExec(Path directory, String[] mavenArgs) {
//        if (!Strings.isNullOrEmpty(System.getenv("M3_HOME"))
//                || !Strings.isNullOrEmpty(System.getenv("M2_HOME"))) {
//            log.info("Selected MavenInvokator");
//            try {
//                return new MavenInvokatorMavenTaskRunner().exec(directory, mavenArgs);
//            } catch (MavenExecutorException e) {
//                log.info("trying with MvnCli, MavenInvokatorMavenTaskRunner failed with {}", e);
//                return new MvnCliMavenRunner().exec(directory, mavenArgs);
//            }
//        }
        log.info("Selected MvnCli");
        return new MvnCliMavenRunner().exec(directory, mavenArgs);
    }
}
