package fi.helsinki.cs.tmc.langs.java;

import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.java.ant.AntPlugin;
import fi.helsinki.cs.tmc.langs.java.testscanner.TestScanner;
import fi.helsinki.cs.tmc.langs.utils.SourceFiles;

import com.google.common.base.Optional;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PluginImplLanguagePlugin extends AntPlugin {

    @Override
    public boolean isExerciseTypeCorrect(Path path) {
        return new File(path.toString() + File.separatorChar + "build.xml").exists();
    }

    @Override
    public String getLanguageName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName) {
        if (!isExerciseTypeCorrect(path)) {
            return null;
        }

        TestScanner scanner = new TestScanner();
        SourceFiles sourceFiles = new SourceFiles();
        Path testDir = createPath(path.toAbsolutePath(), File.separatorChar + "test");
        sourceFiles.addSource(testDir.toFile());
        return scanner.findTests(generateClassPath(path), sourceFiles, exerciseName);
    }

    @Override
    public RunResult runTests(Path path) {
        throw new UnsupportedOperationException();
    }

    private ClassPath generateClassPath(Path path) {
        ClassPath classPath = new ClassPath(path.toAbsolutePath());
        classPath.addDirAndContents(createPath(path, "lib"));
        classPath.add(createPath(path, "build", "test", "classes"));
        classPath.add(createPath(path, "build", "classes"));

        return classPath;
    }

    protected Path createPath(Path basePath, String... subDirs) {
        String path = basePath.toAbsolutePath().toString();

        for (String subDir : subDirs) {
            path += File.separatorChar + subDir;
        }

        return Paths.get(path);
    }
}
