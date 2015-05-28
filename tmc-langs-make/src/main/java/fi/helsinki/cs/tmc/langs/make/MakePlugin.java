package fi.helsinki.cs.tmc.langs.make;

import com.google.common.base.Optional;
import fi.helsinki.cs.tmc.langs.AbstractLanguagePlugin;
import fi.helsinki.cs.tmc.langs.ClassPath;
import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.RunResult;
import fi.helsinki.cs.tmc.langs.testscanner.TestScanner;
import fi.helsinki.cs.tmc.langs.util.ProcessRunner;
import fi.helsinki.cs.tmc.langs.utils.SourceFiles;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
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

        TestScanner scanner = new TestScanner();
        SourceFiles sourceFiles = new SourceFiles();
        sourceFiles.addSource(createPath(path.toAbsolutePath(), testDir).toFile());
        return scanner.findTests(generateClassPath(path), sourceFiles, exerciseName);
    }

    @Override
    protected boolean isExerciseTypeCorrect(Path path) {
        return new File(path.toString() + File.separatorChar + "Makefile").exists();
    }

    @Override
    public RunResult runTests(Path path) {
        final File projectDir = new File(testDir);

        String[] command;

        command = new String[]{projectDir.getAbsolutePath() + File.separatorChar +
                "test" + File.separatorChar +
                "test"};
        log.log(Level.INFO, "Running tests with command {0}",
                new Object[]{Arrays.deepToString(command)});

        ProcessRunner runner = new ProcessRunner(command, projectDir);

        try {
            log.info("Preparing to run tests");
            runner.call();
            log.info("Running tests completed");
        } catch (Exception e) {
            log.log(Level.INFO, "Exception while running tests, kinda wanted. {0}", e.getMessage());
        }

        File resultsFile = new File(projectDir.getAbsolutePath() + "/tmc_test_results.xml");

        log.info("Locating exercise");

        return new CTestResultParser(resultsFile, null, null).result();
    }

    private ClassPath generateClassPath(Path path) {
        ClassPath classPath = new ClassPath(path.toAbsolutePath());
        classPath.addDirAndContents(createPath(path, "lib"));
        classPath.add(createPath(path, "build", "test", "classes"));
        classPath.add(createPath(path, "build", "classes"));

        return classPath;
    }

    private Path createPath(Path basePath, String... subDirs) {
        String path = basePath.toAbsolutePath().toString();

        for (String subDir : subDirs) {
            path += File.separatorChar + subDir;
        }

        return Paths.get(path);
    }
}
