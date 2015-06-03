package fi.helsinki.cs.tmc.langs.utils;

import com.google.common.base.Throwables;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public final class TestUtils {

    /**
     * Returns a path to a resource residing in the ResourceDir of the given class.
     */
    public static Path getPath(Class clazz, String location) {
        try {
            URL url = clazz.getResource("/" + location);

            if (url != null) {
                return Paths.get(url.toURI());
            }

            return null;
        } catch (URISyntaxException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Removes a directory and all its files recursively.
     */
    public static void removeDirRecursively(Path path) throws IOException {
        if (path == null) {
            return;
        }

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException ex) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException ex) throws IOException {
                if (ex == null) {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                } else {
                    throw ex;
                }
            }
        });
    }

    /**
     * Removes class Resource directory recursively.
     */
    public static void removeDirRecursively(Class clazz, String location) throws IOException {
        removeDirRecursively(getPath(clazz, location));
    }
}
