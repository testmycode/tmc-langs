package fi.helsinki.cs.tmc.langs.utils;

import fi.helsinki.cs.tmc.langs.domain.Configuration;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Used to run subprocesses with a timeout and capture their output.
 *
 * <p>TODO: make cancellable
 */
public final class ProcessRunner implements Callable<ProcessResult> {

    private static final Logger log = LoggerFactory.getLogger(ProcessRunner.class);

    private final String[] command;
    private final Path workDir;
    private Configuration configuration;

    public ProcessRunner(String[] command, Path workDir) {
        this.command = command;
        this.workDir = workDir;
        this.configuration = new Configuration(workDir);
    }

    @Override
    public ProcessResult call() throws IOException, InterruptedException {
        Process process = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(workDir.toFile());
            process = processBuilder.start();

            StringWriter stdoutWriter = new StringWriter();
            StringWriter stderrWriter = new StringWriter();

            final Thread stdoutReaderThread = startReadingThread(process.getInputStream(),
                    stdoutWriter);
            final Thread stderrReaderThread = startReadingThread(process.getErrorStream(),
                    stderrWriter);

            boolean status = false;
            if (this.configuration.isSet("tests_timeout_ms")) {
                status = process.waitFor(this.configuration.get("tests_timeout_ms").asInteger(),
                        TimeUnit.MILLISECONDS);
            } else {
                status = process.waitFor(5, TimeUnit.MINUTES);
            }
            if (!status) {
                process.destroy();
            }

            /* We have to wait for process.destroy() to finish before calling process.exitValue().
            Otherwise, process.exitValue() will throw an IllegalThreadStateException. */
            Thread.sleep(5000);
            int statusCode = process.exitValue();

            stdoutReaderThread.join();
            stderrReaderThread.join();

            return new ProcessResult(statusCode, stdoutWriter.toString(), stderrWriter.toString());
        } finally {
            if (process != null) {
                process.getOutputStream().close();
                process.destroy();
            }
        }
    }

    private Thread startReadingThread(InputStream inputStream, StringWriter stringWriter) {
        Thread thread = new Thread(new ProcessOutputReader(inputStream, stringWriter));
        thread.start();
        return thread;
    }

    private class ProcessOutputReader implements Runnable {

        private InputStream inputStream;
        private StringWriter stringWriter;

        public ProcessOutputReader(InputStream inputStream, StringWriter stringWriter) {
            this.inputStream = inputStream;
            this.stringWriter = stringWriter;
        }

        @Override
        public void run() {
            try {
                IOUtils.copy(inputStream, stringWriter, "UTF-8");
            } catch (IOException e) {
                log.error(e.toString());
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error(e.toString());
            }
        }
    }
}
