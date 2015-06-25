package fi.helsinki.cs.tmc.langs.utils;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * Used to run subprocesses with a timeout and capture their output.
 *
 * <p>
 * TODO: make cancellable
 */
public class ProcessRunner implements Callable<ProcessResult> {

    private final String[] command;
    private final Path workDir;

    public ProcessRunner(String[] command, Path workDir) {
        this.command = command;
        this.workDir = workDir;
    }

    @Override
    public ProcessResult call() throws Exception {
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
                process.destroy();
            }
        }
    }

    private Thread startReadingThread(InputStream inputStream, StringWriter stringWriter) {
        Thread thread = new Thread(new ProcessOutputReader(inputStream, stringWriter));
        thread.run();
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
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
