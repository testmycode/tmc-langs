package fi.helsinki.cs.tmc.langs.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
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

    private final String beginSolutionRegex;
    private final String capturingGroups;
    private final String endSolutionRegex;
    private final String solutionFileRegex;
    private final String stubRegex;
    private final Pattern stubReplacePattern;

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

    private interface DirectorySkipper {
        /**
         * Returns true if directory and it's subdirectories should be ignored.
         */
        boolean skipDirectory(Path directory);
    }

    private static class GeneralDirectorySkipper implements DirectorySkipper {

        private static final List<String> skipList =
                Arrays.asList(new String[] {".git", "private"});

        @Override
        public boolean skipDirectory(Path directory) {
            return directory.toFile().isDirectory()
                    && (skipList.contains(directory.getFileName().toString())
                            || Files.exists(
                                    Paths.get(directory.toAbsolutePath().toString(), ".tmcignore"))
                            || directory.getFileName().startsWith("."));
        }
    }

    private static class FilterFileTreeVisitor {

        private Path clonePath, destPath;
        private List<DirectorySkipper> skippers = new ArrayList<>();
        private Filer filer;

        FilterFileTreeVisitor addSkipper(DirectorySkipper skipper) {
            skippers.add(skipper);
            return this;
        }

        FilterFileTreeVisitor setClonePath(Path clonePath) {
            this.clonePath = clonePath;
            return this;
        }

        FilterFileTreeVisitor setDestPath(Path destPath) {
            this.destPath = destPath;
            return this;
        }

        FilterFileTreeVisitor setFiler(Filer filer) {
            this.filer = filer;
            return this;
        }

        private boolean skipDirectory(Path dirPath) {
            for (DirectorySkipper skipper : skippers) {
                if (skipper.skipDirectory(dirPath)) {
                    return true;
                }
            }
            return false;
        }

        void traverse() {
            try {
                Files.walkFileTree(
                        clonePath,
                        new FileVisitor<Path>() {

                            @Override
                            public FileVisitResult preVisitDirectory(
                                    Path dir, BasicFileAttributes attrs) throws IOException {
                                if (skipDirectory(dir)) {
                                    return FileVisitResult.SKIP_SUBTREE;
                                }
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                    throws IOException {
                                filer.maybeCopyAndFilterFile(file, clonePath, destPath);
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
    }

    abstract class Filer {

        // TODO - need a more generic one for binary files?
        abstract List<String> prepareFile(Path file);

        protected boolean skipFile(Path file) {
            List<String> nameSkipList = Arrays.asList(new String[] {"hidden", "Hidden", ".tmcrc"});
            for (String item : nameSkipList) {
                if (file.getFileName().toString().contains(item)) {
                    return true;
                }
            }
            return false;
        }

        public void maybeCopyAndFilterFile(Path file, Path fromPath, Path toPath) {
            Path relativePath = file.subpath(fromPath.getNameCount(), file.getNameCount());
            Path toFile = toPath.resolve(relativePath);
            logger.info(
                    "Maybe copying file from: {} to:{} - name: {}",
                    file,
                    toFile,
                    file.getFileName().toString());
            try {
                if (skipFile(file)) {
                    logger.info("not for stub while copying from: {} to:{}", file, toFile);
                    return;
                }
                if (justCopy(file)) {
                    Files.createDirectories(toFile.getParent());
                    Files.copy(file, toFile);
                    logger.info("Just copying file from: {} to:{}", file, toFile);
                } else {
                    List<String> output = prepareFile(file);
                    if (!output.isEmpty()) {
                        Files.createDirectories(toFile.getParent());
                        Files.write(toFile, output);
                        logger.info("Filtered file while copying from: {} to:{}", file, toFile);
                    } else {
                        logger.info(
                                "skipped file as empty while copying from: {} to:{}", file, toFile);
                    }
                }
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }

        protected final String getFileExtension(Path file) {
            String name = file.getFileName().toString();
            try {
                return name.substring(name.lastIndexOf(".") + 1);
            } catch (Exception e) {
                return "";
            }
        }

        protected final boolean justCopy(Path file) {
            List<String> skipList = Arrays.asList(new String[] {"class", "jar"});
            return file.toFile().isFile() && skipList.contains(getFileExtension(file));
        }
    }

    private class StubFileFilterProcessor extends Filer {
        @Override
        List<String> prepareFile(Path from) {
            try {
                boolean skipLine = false;
                List<String> result;
                try (Scanner scanner = new Scanner(Files.newInputStream(from))) {
                    result = new ArrayList<>();
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
                }
                return result;
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    /**
     * Prepares a stub exercise from the original.
     *
     * <p>Implements LanguagePlugin.prepareStub
     */
    public void prepareStub(final Path clonePath, final Path destPath) {
        new FilterFileTreeVisitor()
                .setClonePath(clonePath)
                .setDestPath(destPath)
                .addSkipper(new GeneralDirectorySkipper())
                .setFiler(new StubFileFilterProcessor())
                .traverse();
    }

    /**
     * Prepares a presentable solution from the original.
     *
     * <p>
     * Implements LanguagePlugin.prepareSolution
     */
    public void prepareSolution(final Path clonePath, final Path destPath) {
        new FilterFileTreeVisitor()
                .setClonePath(clonePath)
                .setDestPath(destPath)
                .addSkipper(new GeneralDirectorySkipper())
                .setFiler(new SolutionFileFilterProcessor())
                .traverse();
    }

    private class SolutionFileFilterProcessor extends Filer {

        List<String> prepareFile(Path file) {
            try {
                List<String> result;
                try (Scanner scanner = new Scanner(Files.newInputStream(file))) {
                    result = new ArrayList<>();
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
                }
                return result;
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }
}
