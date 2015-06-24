package fi.helsinki.cs.tmc.langs.sandbox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Moves files from a directory containing an unzipped student submission to a TMC-sandbox.
 */
public class SubmissionProcessor {

    private static final Logger log = Logger.getLogger(SubmissionProcessor.class.getName());
    private FileMovingPolicy fileMovingPolicy;

    /**
     * Creates a new SubmissionProcessor that moves all files.
     */
    public SubmissionProcessor() {
        this(new DefaultFileMovingPolicy());
    }

    /**
     * Creates a new SubmissionProcessor that uses a provided FileMovingPolicy to decide which
     * files to move.
     */
    public SubmissionProcessor(FileMovingPolicy fileMovingPolicy) {
        this.fileMovingPolicy = fileMovingPolicy;
    }

    /**
     * Moves some of the contents of <tt>source</tt> to <tt>target</tt> based on the decisions
     * of the {@link FileMovingPolicy} that was given when constructing this SubmissionProcessor.
     *
     * <p>As an end result, a file with the path <tt>source/foo.java</tt> will be in path
     * <tt>target/foo.java</tt>.
     *
     * @param source    Directory from which the contents are moved. The directory itself is not
     *                  moved.
     * @param target    Directory to which the source files are moved to.
     */
    public void moveFiles(final Path source, final Path target) {
        try {
            Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (fileMovingPolicy.shouldMove(file, source, target)) {
                        Path absoluteTargetPath = getAbsoluteTargetPath(source, target, file);
                        try {
                            moveFile(source, file.toAbsolutePath(), absoluteTargetPath);
                        } catch (IOException exception) {
                            log.log(Level.WARNING, null, exception);
                        }
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
                        log.log(Level.WARNING, null, exception);
                        throw exception;
                    }
                }
            });
        } catch (IOException exception) {
            log.log(Level.WARNING, null, exception);
            return;
        }
    }

    protected Path getAbsoluteTargetPath(Path sourceRootPath,
                                         Path targetRootPath,
                                         Path sourceFilePath) {
        Path relativeFilePath = sourceRootPath.relativize(sourceFilePath.toAbsolutePath());
        return targetRootPath.resolve(relativeFilePath);
    }

    protected void moveFile(Path sourceRoot, Path sourceFile, Path target) throws IOException {
        Path relative = sourceRoot.relativize(sourceFile);
        Path targetFile = target.resolve(relative);
        Files.createDirectories(targetFile.getParent());
        Files.move(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
    }
}
