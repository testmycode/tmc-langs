package fi.helsinki.cs.tmc.langs.domain;

import com.google.common.io.ByteSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builder for generating stubs and model solutions.
 */
public class ExerciseBuilder {

    private static final String SOURCE_FOLDER_NAME = "src";
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final Logger logger = LoggerFactory.getLogger(ExerciseBuilder.class);

    private String beginSolutionRegex;
    private String capturingGroups;
    private String endSolutionRegex;
    private String solutionFileRegex;
    private String stubRegex;
    private Pattern stubReplacePattern;

    public ExerciseBuilder() {
        this(CommentSyntax.newBuilder().build());
    }

    /**
     * Builder for generating stubs and model solutions.
     */
    public ExerciseBuilder(CommentSyntax commentSyntax) {
        beginSolutionRegex = commentSyntax.getBeginSolution();
        capturingGroups = commentSyntax.getCapturingGroups();
        endSolutionRegex = commentSyntax.getEndSolution();
        solutionFileRegex = commentSyntax.getSolutionFile();
        stubRegex = commentSyntax.getStub();
        stubReplacePattern = commentSyntax.getStubReplacePattern();
    }

    /**
     * Prepares a stub exercise from the original.
     *
     * <p>Implements LanguagePlugin.prepareStub
     */
    public void prepareStub(final Path clonePath, final Path destPath) {
        try {
            Files.walkFileTree(
                    clonePath,
                    new FileVisitor<Path>() {

                        @Override
                        public FileVisitResult preVisitDirectory(
                                Path dir, BasicFileAttributes attrs) throws IOException {
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                throws IOException {
                            maybeCopyAndFilterFile(file, clonePath, destPath);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc)
                                throws IOException {
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                                throws IOException {
                            return FileVisitResult.CONTINUE;
                        }
                    });
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void maybeCopyAndFilterFile(Path file, Path clonePath, Path destPath) {
        Path relativePath = file.subpath(clonePath.getNameCount(), file.getNameCount());
        Path toFile = destPath.resolve(relativePath);
        logger.info("Maybe copying file from: {} to:{}", file, toFile);
        try {

            List<String> skipList = Arrays.asList(new String[] {"class", "jar"});
            if (file.toFile().isFile() && skipList.contains(getFileExtension(file))) {
                Files.createDirectories(toFile.getParent());
                Files.copy(file, toFile);
                logger.info("Just copying file from: {} to:{}", file, toFile);
            } else {
                List<String> output = prepareStubFile(file);
                if (!output.isEmpty()) {
                    Files.createDirectories(toFile.getParent());
                    Files.write(toFile, output);
                    logger.info("Filtered file while copying from: {} to:{}", file, toFile);
                }
            }
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(ExerciseBuilder.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    private String getFileExtension(Path file) {
        String name = file.getFileName().toString();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }

    private List<String> prepareStubFile(Path from) {
        try {
            boolean skipLine = false;
            Scanner scanner = new Scanner(Files.newInputStream(from));
            List<String> result = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.matches(solutionFileRegex)) {
                    scanner.close();
                    return new ArrayList<>();
                }
                if (line.matches(beginSolutionRegex)) {
                    skipLine = true;
                } else if (skipLine && line.matches(endSolutionRegex)) {
                    skipLine = false;
                } else if (line.matches(stubRegex)) {
                    Matcher stubMatcher = stubReplacePattern.matcher(line);
                    result.add(stubMatcher.replaceAll(capturingGroups));
                } else if (!skipLine) {
                    result.add(line);
                }
            }
            scanner.close();
            return result;
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Prepares a presentable solution from the original.
     *
     * <p>Implements LanguagePlugin.prepareSolution
     */
    public void prepareSolution(final Path clonePath, final Path destPath) {
        try {
            Files.walkFileTree(
                    clonePath,
                    new FileVisitor<Path>() {

                        @Override
                        public FileVisitResult preVisitDirectory(
                                Path dir, BasicFileAttributes attrs) throws IOException {
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                throws IOException {
                            maybeCopyAndFilterSolutionFile(file, clonePath, destPath);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc)
                                throws IOException {
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                                throws IOException {
                            return FileVisitResult.CONTINUE;
                        }
                    });
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void maybeCopyAndFilterSolutionFile(Path file, Path clonePath, Path destPath) {
        Path relativePath = file.subpath(clonePath.getNameCount(), file.getNameCount());
        Path toFile = destPath.resolve(relativePath);
        logger.info("Maybe copying file from: {} to:{}", file, toFile);
        try {

            List<String> skipList = Arrays.asList(new String[] {"class", "jar"});
            if (file.toFile().isFile() && skipList.contains(getFileExtension(file))) {
                Files.createDirectories(toFile.getParent());
                Files.copy(file, toFile);
                logger.info("Just copying file from: {} to:{}", file, toFile);
            } else {
                List<String> output = prepareSolutionFile(file);
                if (!output.isEmpty()) {
                    Files.createDirectories(toFile.getParent());
                    Files.write(toFile, output);
                    logger.info("Filtered file while copying from: {} to:{}", file, toFile);
                }
            }
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(ExerciseBuilder.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    private List<String> prepareSolutionFile(Path file) {
        try {
            Scanner scanner = new Scanner(Files.newInputStream(file));
            List<String> result = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.matches(beginSolutionRegex)
                        || line.matches(endSolutionRegex)
                        || line.matches(stubRegex)
                        || line.matches(solutionFileRegex)) {
                    continue;
                }
                result.add(line);
            }
            scanner.close();
            return result;
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
