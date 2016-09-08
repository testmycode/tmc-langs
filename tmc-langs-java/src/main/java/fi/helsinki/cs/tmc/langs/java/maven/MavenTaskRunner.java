package fi.helsinki.cs.tmc.langs.java.maven;

import org.apache.maven.cli.MavenCli;

import java.nio.file.Path;

public interface MavenTaskRunner {

    MavenExecutionResult exec(Path directory, String[] mavenArgs);

    final class MavenExecutionResult {

        private int exitCode;

        private byte[] stdOut;
        private byte[] stdErr;

        public MavenExecutionResult setExitCode(int exitCode) {
            this.exitCode = exitCode;
            return this;
        }

        public MavenExecutionResult setStdErr(byte[] stdErr) {
            this.stdErr = stdErr;
            return this;
        }

        public MavenExecutionResult setStdOut(byte[] stdOut) {
            this.stdOut = stdOut;
            return this;
        }

        public byte[] getStdOut() {
            return stdOut;
        }

        public byte[] getStdErr() {
            return stdErr;
        }

        public int getExitCode() {
            return exitCode;
        }
    }
}
