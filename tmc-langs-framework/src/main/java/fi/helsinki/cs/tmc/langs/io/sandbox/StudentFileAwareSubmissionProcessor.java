package fi.helsinki.cs.tmc.langs.io.sandbox;

import fi.helsinki.cs.tmc.langs.io.EverythingIsStudentFileStudentFilePolicy;
import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Moves files from a directory containing an unzipped student submission to a TMC-sandbox.
 */
public class StudentFileAwareSubmissionProcessor implements SubmissionProcessor {

    private static final Logger log =
            LoggerFactory.getLogger(StudentFileAwareSubmissionProcessor.class);
    private StudentFilePolicy studentFilePolicy;

    /**
     * Creates a new SubmissionProcessor that moves all files.
     */
    public StudentFileAwareSubmissionProcessor() {
        this(new EverythingIsStudentFileStudentFilePolicy());
    }

    /**
     * Creates a new SubmissionProcessor that uses a provided StudentFilePolicy to decide which
     * files to move.
     */
    public StudentFileAwareSubmissionProcessor(StudentFilePolicy studentFilePolicy) {
        this.studentFilePolicy = studentFilePolicy;
    }

    @Override
    public void setStudentFilePolicy(StudentFilePolicy studentFilePolicy) {
        this.studentFilePolicy = studentFilePolicy;
    }

    /**
     * Moves some of the contents of <tt>source</tt> to <tt>target</tt> based on the decisions
     * of the {@link StudentFilePolicy} that was given when constructing this SubmissionProcessor.
     *
     * <p>As an end result, a file with the path <tt>source/foo.java</tt> will be in path
     * <tt>target/foo.java</tt>.
     *
     * @param source    directory from which the contents are moved. The directory itself is not
     *                  moved
     * @param target    directory to which the source files are moved to
     */
    @Override
    public void moveFiles(final Path source, final Path target) {
        try {
            Files.walkFileTree(
                    source,
                    new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            if (studentFilePolicy.isStudentFile(file, source)) {
                                try {
                                    moveFile(source, file, target);
                                } catch (IOException exception) {
                                    log.info("{}", exception);
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
                                log.info("{}", exception);
                                throw exception;
                            }
                        }
                    });
        } catch (IOException exception) {
            log.info("{}", exception);
        }
    }

    protected void moveFile(Path sourceRoot, Path sourceFile, Path target) throws IOException {
        Path relative = sourceRoot.relativize(sourceFile);
        Path targetFile = target.resolve(relative);
        Files.createDirectories(targetFile.getParent());
        Files.move(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
    }
}
