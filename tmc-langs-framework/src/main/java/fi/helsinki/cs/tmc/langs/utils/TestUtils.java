package fi.helsinki.cs.tmc.langs.utils;

import fi.helsinki.cs.tmc.langs.io.ConfigurableStudentFilePolicy;

import com.google.common.base.Throwables;

import org.junit.Assume;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public final class TestUtils {

    private static final Logger log = LoggerFactory.getLogger(TestUtils.class);

    /**
     * Returns a path to a resource residing in the ResourceDir of the given class.
     */
    public static Path getPath(Class<?> clazz, String location) {
        try {
            URL url = clazz.getResource("/" + location);

            if (url != null) {
                return Paths.get(url.toURI());
            }

            return null;
        } catch (URISyntaxException e) {
            log.error("Unable to get path for requested resource", e);
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

        Files.walkFileTree(
                path,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException ex)
                            throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException ex)
                            throws IOException {
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

    /**
     * Collects a list of paths that the provided {@link ConfigurableStudentFilePolicy}
     * considers student files.
     */
    public static void collectPaths(
            final Path path,
            final List<String> toBeMoved,
            final ConfigurableStudentFilePolicy fileMovingPolicy)
            throws IOException {
        Files.walkFileTree(
                path,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException {
                        if (fileMovingPolicy.isStudentSourceFile(path.relativize(file), path)) {
                            toBeMoved.add(path.relativize(file).toString());
                        }

                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exception)
                            throws IOException {
                        if (exception == null) {
                            return FileVisitResult.CONTINUE;
                        } else {
                            // directory iteration failed
                            throw exception;
                        }
                    }
                });
    }

    /**
     * Initializes a temporary file with content.
     */
    public static Path initTempFileWithContent(String prefix, String suffix, String content)
            throws IOException {
        return initTempFileWithContent(prefix, suffix, null, content);
    }

    /**
     * Initializes a temporary file in a specific directory with content.
     */
    public static Path initTempFileWithContent(
            String prefix, String suffix, File directory, String content) throws IOException {
        String suffixWithDot = "." + suffix;
        File file = File.createTempFile(prefix, suffixWithDot, directory);
        file.deleteOnExit();

        PrintWriter pw = new PrintWriter(file, "UTF-8");
        pw.println(content);
        pw.flush();
        pw.close();

        return file.toPath();
    }

    public static void skipIfOnWindowsCi() {
        Assume.assumeTrue(
                "This test is not supported on Windows CI.", System.getenv("APPVEYOR") == null);
    }

    public static void maybeSkipTests() {
        isFeatureAvailable("valgrind", "Skipping makefile tests since no valgring is found");
        isFeatureAvailable("rust", "Skipping rust tests since no rust installation is found");
    }

    public static void skipIfNotAvailable(String command) {
        isFeatureAvailable(command, "Skipping since " + command + " is not available");
    }

    public static void isFeatureAvailable(String cmd, String message) {
        try {
            Process process = Runtime.getRuntime().exec("which " + cmd);
            process.waitFor();
            process.destroy();
            Assume.assumeTrue(message, 0 == process.exitValue());
        } catch (IOException | InterruptedException ignore) {
        }
    }
}
