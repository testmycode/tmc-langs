package fi.helsinki.cs.tmc.langs.make;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.helsinki.cs.tmc.langs.AbstractLanguagePlugin;
import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.langs.TestResult;
import fi.helsinki.cs.tmc.langs.util.ProcessResult;
import fi.helsinki.cs.tmc.langs.util.ProcessRunner;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MakePlugin extends AbstractLanguagePlugin {

    private static final Logger log = Logger.getLogger(MakePlugin.class.getName());

    private final String testDir = File.separatorChar + "test";

    @Override
    public String getLanguageName() {
        return "make";
    }

    @Override
    public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName) {
        if (!isExerciseTypeCorrect(path)) {
            return Optional.absent();
        }

//        TestScanner scanner = new TestScanner();
//        SourceFiles sourceFiles = new SourceFiles();
//        sourceFiles.addSource(createPath(path.toAbsolutePath(), testDir).toFile());
        return null;
    }

    @Override
    protected boolean isExerciseTypeCorrect(Path path) {
        return new File(path.toString() + File.separatorChar + "Makefile").exists();
    }

    @Override
    public RunResult runTests(Path path) {
        final File projectDir = new File(String.valueOf(path));
        boolean withValgrind = true;

        if (!builds(projectDir)) {
            return new RunResult(RunResult.Status.COMPILE_FAILED, ImmutableList.copyOf(new ArrayList<TestResult>()),
                    new ImmutableMap.Builder<String, byte[]>().build());
        }

        try {
            runTests(projectDir, withValgrind);
        } catch (Exception e) {
            withValgrind = false;

            try {
                runTests(projectDir, withValgrind);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        File valgrindLog = withValgrind ? new File(projectDir.getAbsolutePath() + File.separatorChar + "test" +
                File.separatorChar + "valgrind.log") : null;
        File resultsFile = new File(projectDir.getAbsolutePath() + File.separatorChar + "test" + File.separatorChar +
                "tmc_test_results.xml");

        log.info("Locating exercise");

        return new CTestResultParser(resultsFile, valgrindLog, projectDir).result();
    }

    @Override
    public ValidationResult checkCodeStyle(Path path) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private boolean builds(File dir) {
        String[] command;
        command = new String[]{"make", "test"};
        ProcessRunner runner = new ProcessRunner(command, dir);

        try {
            ProcessResult result = runner.call();
            int ret = result.statusCode;
            if (ret != 0) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void runTests(File dir, boolean withValgrind) throws Exception {
        String[] command;

        String target = withValgrind ? "run-test-with-valgrind" : "run-test";
        command = new String[]{"make", target};

        log.log(Level.INFO, "Running tests with command {0}",
                new Object[]{Arrays.deepToString(command)});

        ProcessRunner runner = new ProcessRunner(command, dir);

        runner.call();
    }
}
