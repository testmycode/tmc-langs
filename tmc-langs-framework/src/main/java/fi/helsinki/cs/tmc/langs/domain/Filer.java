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
import java.nio.file.StandardCopyOption;
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
    
    public void visitFileExceptionWrapper(Path source, Path relativePath) {
        try {
            visitFile(source, relativePath);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private void visitFile(Path source, Path relativePath) throws IOException {
        Path destination = toPath.resolve(relativePath);
        if (skipFilename(source)) {
            return;
        }
        if (looksLikeBinary(source)) {
            justCopy(source, destination);
        } else {
            copyWithFilters(source, destination);
        }
    }
    
    private boolean skipFilename(Path source) {
        String skipRegex = "\\.tmcrc|metadata\\.yml|(.*)Hidden(.*)";
        if (source.getFileName().toString().matches(skipRegex)) {
            logger.info("Skipping file: {} ", source);
            return true;
        }
        logger.info("Not skipping file: {} ", source);
        return false;
    }
    
    private boolean looksLikeBinary(Path source) {
        String nonTextTypes = "class|jar|exe|jpg|jpeg|gif|png";
        return getFileExtension(source).matches(nonTextTypes);
    }
    
    private void justCopy(Path source, Path destination) throws IOException {
        logger.info("Just copying file from: {} to:{}", source, destination);
        Files.createDirectories(destination.getParent());
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
    }
    
    private void copyWithFilters(Path source, Path destination) throws IOException {
        List<String> data = prepareFile(readFile(source), getFileExtension(source));
        logger.info("Filtered file while copying from: {} to:{}", source, destination);
        if (data.isEmpty()) {
            logger.info("skipped file as empty while copying from: {} to:{}", source, destination);
        } else {
            Files.createDirectories(destination.getParent());
            Files.write(destination, data, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        }
    }
    
    public List<String> prepareFile(List<String> data, String fileType) {
        List<MetaSyntax> syntaxes = MetaSyntaxGenerator.listSyntaxes(fileType);
        for (MetaSyntax metaSyntax : syntaxes) {
            data = filterData(data, metaSyntax);
        }
        return data;
    }
    
    List<String> filterData(List<String> data, MetaSyntax metaSyntax) {
        return data; // usually overridden, not always
    }    
    
    /*
     * Note: we need to use the inputStream for reading more interesting filetypes.
     */
    protected List<String> readFile(Path file) throws IOException {
        List<String> data = new ArrayList<>();
        Scanner scanner = new Scanner(Files.newInputStream(file));
        while (scanner.hasNextLine()) {
            data.add(scanner.nextLine());
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
}
