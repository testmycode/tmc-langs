package fi.helsinki.cs.tmc.langs.domain;

import fi.helsinki.cs.tmc.langs.LanguagePlugin;

import com.google.common.collect.ImmutableList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Filer {

    private static final Logger logger = LoggerFactory.getLogger(Filer.class);

    private Path toPath;

    private LanguagePlugin languagePlugin;

    public Filer setToPath(Path toPath) {
        this.toPath = toPath;
        return this;
    }

    public Filer setLanguagePlugin(LanguagePlugin languagePlugin) {
        this.languagePlugin = languagePlugin;
        return this;
    }

    public FileVisitResult decideOnDirectory(Path directory) {
        return FileVisitResult.CONTINUE;
    }

    public List<String> prepareFile(List<String> file) {
        return file;
    }

    List<CommentStyleFileFilter> getCommentStyleFileFilters() {
        return new ImmutableList.Builder<CommentStyleFileFilter>()
                .add(
                        // Basic java
                        new CommentStyleFileFilter(
                                // JavaishCommentSyntax
                                CommentSyntax.newBuilder()
                                        .addSingleLineComment("\\/\\/")
                                        .addMultiLineComment("\\/\\*+", "\\*+\\/")
                                        .build()))
                .add(
                        new CommentStyleFileFilter(
                                // XML comment syntax
                                CommentSyntax.newBuilder()
                                        .addMultiLineComment("/\\*", "\\*/")
                                        .build()))
                //TODO: add rest of the comment syntaxes
                .build();
    }

    protected boolean skipFile(Path file) {
        List<String> nameSkipList = Arrays.asList(new String[] {"hidden", "Hidden", ".tmcrc"});
        for (String item : nameSkipList) {
            if (file.getFileName().toString().contains(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Copies and filters the file based on given configuration.
     *
     * <p>TODO: refactor, I don't like this.
     */
    public void maybeCopyAndFilterFile(Path file, Path fromPath) {
        Path relativePath = file.subpath(fromPath.getNameCount() - 1, file.getNameCount());
        logger.info("Looking into file: {} ", file);
        try {
            if (skipFile(file)) {
                logger.info("Skipping file: {} ", file);
                return;
            }
            Path toFile = toPath.resolve(relativePath);
            if (justCopy(file)) {
                Files.createDirectories(toFile.getParent());
                Files.copy(file, toFile);
                logger.info("Just copying file from: {} to:{}", file, toFile);
            } else {
                List<String> data = readFile(file);
                List<String> output = prepareFile(data);
                if (!output.isEmpty()) {
                    Files.createDirectories(toFile.getParent());
                    Files.write(toFile, output, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
                    logger.info("Filtered file while copying from: {} to:{}", file, toFile);
                } else {
                    logger.info("skipped file as empty while copying from: {} to:{}", file, toFile);
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /*
     * Note: we need to use the inputStream for reading more interesting filetypes.
     */
    private List<String> readFile(Path file) {
        List<String> data = new ArrayList<>();
        try (Scanner scanner = new Scanner(Files.newInputStream(file))) {
            while (scanner.hasNextLine()) {
                data.add(scanner.nextLine());
            }
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        return data;
    }

    protected final String getFileExtension(Path file) {
        String name = file.getFileName().toString();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }

    // TODO: make it more accurate by seeing if the file is kinda like text...
    protected final boolean justCopy(Path file) {
        List<String> skipList = Arrays.asList(new String[] {"class", "jar"});
        return file.toFile().isFile() && skipList.contains(getFileExtension(file));
    }
}
