package fi.helsinki.cs.tmc.langs.util;

import org.openide.filesystems.FileUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Used to run subprocesses with a timeout and capture their output.
 *
 * <p>
 * TODO: make cancellable
 */
public class ProcessRunner implements Callable<ProcessResult> {

    private final String[] command;
    private final File workDir;

    public ProcessRunner(String[] command, File workDir) {
        this.command = command;
        this.workDir = workDir;
    }

    @Override
    public ProcessResult call() throws Exception {
        @SuppressWarnings("unchecked")
        String[] envp = makeEnvp(System.getenv());

        Process process = Runtime.getRuntime().exec(command, envp, workDir);

        int statusCode;

        OutputStream out = new ByteArrayOutputStream();
        OutputStream err = new ByteArrayOutputStream();

        try {
            startReaderThread(process.getInputStream(), out);
            startReaderThread(process.getErrorStream(), err);

            statusCode = process.waitFor();
        } catch (InterruptedException e) {
            process.destroy();
            throw e;
        }

        String output = out.toString();
        String errorOutput = err.toString();
        return new ProcessResult(statusCode, output, errorOutput);
    }

    private String[] makeEnvp(Map<String, String>... envs) {
        int totalEntries = 0;
        for (Map<String, String> env : envs) {
            totalEntries += env.size();
        }

        String[] envp = new String[totalEntries];
        int i = 0;
        for (Map<String, String> env : envs) {
            for (Map.Entry<String, String> envEntry : env.entrySet()) {
                envp[i++] = envEntry.getKey() + "=" + envEntry.getValue();
            }
        }

        return envp;
    }

    private Thread startReaderThread(final InputStream is, final OutputStream os) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    FileUtil.copy(is, os);
                } catch (IOException e) {
                }
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

}
