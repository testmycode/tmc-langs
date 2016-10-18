package fi.helsinki.cs.tmc.langs.utils;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * Used to run subprocesses with a timeout and capture their output.
 *
 * <p>TODO: make cancellable
 */
public final class ProcessRunner implements Callable<ProcessResult> {

    private static final Logger log = LoggerFactory.getLogger(ProcessRunner.class);

    private final String[] command;
    private final Path workDir;

    public ProcessRunner(String[] command, Path workDir) {
        this.command = command;
        this.workDir = workDir;
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

            Thread stdoutReaderThread = startReadingThread(process.getInputStream(), stdoutWriter);
            Thread stderrReaderThread = startReadingThread(process.getErrorStream(), stderrWriter);

            int statusCode = process.waitFor();
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
