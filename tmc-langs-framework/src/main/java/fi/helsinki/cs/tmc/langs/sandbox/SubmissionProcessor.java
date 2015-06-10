package fi.helsinki.cs.tmc.langs.sandbox;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Moves files from a directory containing an unzipped student submission to a TMC-sandbox.
 */
public class SubmissionProcessor {

    private FileMovingPolicy fileMovingPolicy;


    /**
     * Creates a new SubmissionProcessor that moves all files.
     */
    public SubmissionProcessor() {
        this(new DefaultFileMovingPolicy());
    }

    /**
     * Creates a new SubmissionProcessor that uses a provided FileMovingPolicy to decide what
     * files to move.
     */
    public SubmissionProcessor(FileMovingPolicy fileMovingPolicy) {
        this.fileMovingPolicy = fileMovingPolicy;
    }

    /**
     * Moves the some of the contents of <tt>source</tt> to <tt>target</tt> based on the decisions
     * of the {@link FileMovingPolicy} that was given when constructing this SubmissionProcessor.
     *
     * <p>As an end result, a file with the path <tt>source/foo.java</tt> will be in path
     * <tt>target/foo.java</tt>.
     *
     * @param source    Directory from which the contents are moved. The directory itself is not
     *                  moved.
     * @param target    Directory to which the source files are moved to.
     */
    public void moveFiles(Path source, Path target) {
        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(source);
            for (Path sourceFile : stream) {
                if (fileMovingPolicy.shouldMove(sourceFile)) {
                    moveFile(sourceFile, target);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void moveFile(Path sourceFile, Path target) {

    }


}
