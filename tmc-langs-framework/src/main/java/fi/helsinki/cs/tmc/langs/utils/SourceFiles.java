package fi.helsinki.cs.tmc.langs.utils;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SourceFiles {

    private List<File> sourceFiles;

    public SourceFiles(Path... paths) {
        sourceFiles = new ArrayList<>();
        for (Path path : paths) {
            addSource(path.toFile());
        }
    }

    public void addSource(File fileOrDir) {
        if (fileOrDir.isDirectory()) {
            for (File entry : fileOrDir.listFiles()) {
                addSource(entry);
            }
        } else {
            if (fileOrDir.getPath().endsWith(".java") || fileOrDir.getPath().endsWith(".jar")) {
                sourceFiles.add(fileOrDir);
            }
        }
    }

    public void clearSources() {
        sourceFiles.clear();
    }

    public List<File> getSources() {
        return Collections.unmodifiableList(sourceFiles);
    }

    public boolean isEmpty() {
        return sourceFiles.isEmpty();
    }
}
