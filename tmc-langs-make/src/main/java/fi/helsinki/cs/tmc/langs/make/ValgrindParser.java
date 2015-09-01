package fi.helsinki.cs.tmc.langs.make;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ValgrindParser {

    private static final String VALGRIND_PID_WARNING =
            "Valgrind output has more PIDs than the expected (# of test cases + 1).";
    private static final String VALGRIND_ERROR_SUMMARY_PATTERN =
            "==[0-9]+== ERROR SUMMARY: ([0-9]+)";
    private static final String LINUX_VALGRIND_WARNING =
            "Please install valgrind. "
                    + "For Debian-based distributions, run `sudo apt-get install valgrind`.";
    private static final String OSX_VALGRIND_WARNING =
            "Please install valgrind. For OS X we recommend using homebrew"
                    + "(http://mxcl.github.com/homebrew/) and `brew install valgrind`.";
    private static final String WINDOWS_VALGRIND_WARNING = "Windows doesn't support valgrind yet.";
    private static final String OTHER_VALGRIND_WARNING = "Please install valgrind if possible.";
    private static final String NO_VALGRIND_MESSAGE =
            "Warning, valgrind not available - unable to run local memory tests\n";
    private static final String VALGRIND_SUBMIT_MESSAGE =
            "\nYou may also submit the exercise " + "to the server to have it memory-tested.";
    private static final String CANT_PARSE_PID = "Couldn't parse PID from Valgrind log";

    private Path output;
    private Logger log = LoggerFactory.getLogger(ValgrindParser.class);

    public ValgrindParser(Path output) {
        this.output = output;
    }

    /**
     * Adds valgrinds outputs to test cases.
     */
    public void addOutputs(List<CTestCase> tests) {
        if (output != null) {
            try {
                addValgrindOutput(tests);
            } catch (IOException e) {
                log.error(e.toString());
            }
        } else {
            addWarningToValgrindOutput(tests);
        }
    }

    private void addWarningToValgrindOutput(List<CTestCase> tests) {
        String message;
        String platform = System.getProperty("os.name").toLowerCase();

        if (platform.contains("linux")) {
            message = LINUX_VALGRIND_WARNING;
        } else if (platform.contains("mac")) {
            message = OSX_VALGRIND_WARNING;
        } else if (platform.contains("windows")) {
            message = WINDOWS_VALGRIND_WARNING;
        } else {
            message = OTHER_VALGRIND_WARNING;
        }

        for (CTestCase test : tests) {
            test.setValgrindTrace(NO_VALGRIND_MESSAGE + message + VALGRIND_SUBMIT_MESSAGE);
        }
    }

    private void addValgrindOutput(List<CTestCase> tests) throws IOException {
        Scanner scanner = new Scanner(output, "UTF-8");
        // Contains total amount of memory used and such things.
        // Useful if we later want to add support for testing memory usage
        String parentOutput = "";
        String[] outputs = new String[tests.size()];
        int[] pids = new int[tests.size()];
        int[] errors = new int[tests.size()];
        for (int i = 0; i < outputs.length; i++) {
            outputs[i] = "";
        }

        Pattern errorPattern = Pattern.compile(VALGRIND_ERROR_SUMMARY_PATTERN);

        String line = scanner.nextLine();
        int firstPid = parsePid(line);
        parentOutput += "\n" + line;
        boolean warningLogged = false;
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            int pid = parsePid(line);
            if (pid == -1) {
                continue;
            }
            if (pid == firstPid) {
                parentOutput += "\n" + line;
            } else {
                int outputIndex = findIndex(pid, pids);
                if (outputIndex == -1) {
                    if (!warningLogged) {
                        log.warn(VALGRIND_PID_WARNING);
                        warningLogged = true;
                    }
                    continue;
                }
                outputs[outputIndex] += "\n" + line;
                Matcher matcher = errorPattern.matcher(line);
                if (matcher.find()) {
                    errors[outputIndex] = Integer.parseInt(matcher.group(1));
                }
            }
        }
        scanner.close();

        for (int i = 0; i < outputs.length; i++) {
            if (errors[i] == 0) {
                // Workaround for a bug where any valgrind output is considered a potential error.
                outputs[i] = null;
            }
            tests.get(i).setValgrindTrace(outputs[i]);
        }
    }

    private int findIndex(int pid, int[] pids) {
        for (int i = 0; i < pids.length; i++) {
            if (pids[i] == pid) {
                return i;
            }
            if (pids[i] == 0) {
                pids[i] = pid;
                return i;
            }
        }
        return -1;
    }

    private int parsePid(String line) {
        try {
            return Integer.parseInt(line.split(" ")[0].replaceAll("(==|--)", ""));
        } catch (Exception e) {
            log.error(CANT_PARSE_PID);
            return -1;
        }
    }
}
