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
    
    public void visitFile(Path source, Path relativePath) {
        logger.info("Looking into file: {} ", source);
        String skipRegex = "\\.tmcrc|metadata\\.yml|(.*)Hidden(.*)";
        if (source.getFileName().toString().matches(skipRegex)) {
            logger.info("Skipping file: {} ", source);
            return;
        }
        Path destination = toPath.resolve(relativePath);
        try {
            copyWithFilters(source, destination);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public void copyWithFilters(Path source, Path destination) throws IOException {
        List<String> data = readFile(source);
        if (!isTextFile(source)) {
            logger.info("Just copying file from: {} to:{}", source, destination);
        } else {
            data = prepareFile(data, getFileExtension(source));
            logger.info("Filtered file while copying from: {} to:{}", source, destination);
        }
        
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
    List<String> filterData(List<String> data, MetaSyntax m) {
        return data; // Subclasses override this method
    }

    protected final boolean isTextFile(Path file) throws IOException {
        List<String> skipList = Arrays.asList(new String[] {"class", "jar"});
        if (file.toFile().isFile() && skipList.contains(getFileExtension(file))) {
            return false;
        }
        // If >90% of characters in file are sourcecode-text-like, interpret as a text file
        long countNormal = 0;
        long countOther = 0;
        List<String> data = readFile(file);
        for (String line : data) {
            for (int i=0; i<line.length(); i++) {
                char c = line.charAt(i);
                if (c >= 32 && c <= 125) countNormal++; // 0-9,A-Z,a-z,{-]+!...
                else if (c == 9)         countNormal++; // horizontal tab
                else                     countOther++;
            }
        }
        if (countOther == 0) return true;
        return (countNormal / countOther > 9);
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
