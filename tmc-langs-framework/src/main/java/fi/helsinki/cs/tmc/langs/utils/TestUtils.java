package fi.helsinki.cs.tmc.langs.utils;

import com.google.common.base.Throwables;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public final class TestUtils {

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

    public static void removeDirRecursively(Path path) throws IOException {
        if (path == null) {
            return;
        }

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
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

    public static void removeDirRecursively(Class clazz, String location) throws IOException {
        removeDirRecursively(getPath(clazz, location));
    }
}
