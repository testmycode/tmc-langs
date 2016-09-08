package fi.helsinki.cs.tmc.langs.java.maven;

import fi.helsinki.cs.tmc.langs.java.maven.MavenTaskRunner.MavenExecutionResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class MavenExecutors {

    private static final Logger log = LoggerFactory.getLogger(MavenExecutors.class);

    public static final MavenExecutionResult tryAndExec(Path directory, String[] mavenArgs) {
        log.info("Defaulting to MavenInvokator");
        return new MavenInvokatorMavenTaskRunner().exec(directory, mavenArgs);
    }
}
