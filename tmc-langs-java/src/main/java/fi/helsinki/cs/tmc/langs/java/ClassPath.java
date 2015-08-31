package fi.helsinki.cs.tmc.langs.java;

import com.google.common.base.Throwables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public final class ClassPath {

    private static final Logger log = LoggerFactory.getLogger(ClassPath.class);

    private final List<Path> subPaths = new ArrayList<>();

    /**
     * Create a ClassPath where each provided Path is a component.
     */
    public ClassPath(Path... paths) {
        for (Path path : paths) {
            add(path);
        }
    }

    /**
     * Add a given Path to the ClassPath.
     */
    public void add(Path path) {
        if (!subPaths.contains(path)) {
            subPaths.add(path);
        }
    }

    /**
     * Add the contents of a given ClassPath to this ClassPath.
     */
    public void add(ClassPath path) {
        for (Path subPath : path.subPaths) {
            add(subPath);
        }
    }

    /**
     * Returns the contents of this ClassPath as Path objects.
     */
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
            log.error("IOException while adding a file from directory {} to classpath", basePath);
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
