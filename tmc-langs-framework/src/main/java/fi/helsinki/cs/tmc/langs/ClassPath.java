package fi.helsinki.cs.tmc.langs;

import com.google.common.base.Throwables;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper class for generating ClassPath.
 */
public class ClassPath {

    private final List<Path> subPaths = new ArrayList<>();

    public ClassPath(Path... paths) {
        for (Path path : paths) {
            add(path);
        }
    }

    public void add(Path path) {
        if (!subPaths.contains(path)) {
            subPaths.add(path);
        }
    }

    public void add(ClassPath path) {
        for (Path subPath : path.subPaths) {
            add(subPath);
        }
    }

    public List<Path> getPaths() {
        return Collections.unmodifiableList(subPaths);
    }

    /**
     * Crawl through directories and add every directory and .jar file to the
     * classpath.
     *
     * @param basePath Directory where to begin the search.
     */
    public void addDirAndContents(Path basePath) {
        if (!basePath.toFile().isDirectory()) {
            return;
        }

        add(basePath);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(basePath)) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    addDirAndContents(path);
                }

                if (path.toString().endsWith("jar")) {
                    add(path);
                }
            }
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public String toString() {
        if (subPaths.isEmpty()) {
            return "";
        }

        String classPath = subPaths.get(0).toString();

        for (int i = 1; i < subPaths.size(); i++) {
            classPath += File.pathSeparatorChar + subPaths.get(i).toString();
        }

        return classPath;
    }
}
