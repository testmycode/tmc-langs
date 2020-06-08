package fi.helsinki.cs.tmc.langs.domain;

import org.apache.commons.io.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.regex.Pattern;

public class Filer {

    private static final Logger logger = LoggerFactory.getLogger(Filer.class);

    private Path toPath;

    private static final Pattern NON_TEXT_TYPES =
            Pattern.compile("class|jar|exe|jpg|jpeg|gif|png|zip|tar|gz|db|bin|csv|tsv");
    private static final Pattern FILES_TO_SKIP_ALLWAYS =
            Pattern.compile("\\.tmcrc|metadata\\.yml|(.*)Hidden(.*)");

    public Filer setToPath(Path toPath) {
        this.toPath = toPath;
        return this;
    }

    public FileVisitResult decideOnDirectory(Path directory) {
        return FileVisitResult.CONTINUE;
    }

    public void visitFile(Path source, Path relativePath) {
        try {
            Path destination = toPath.resolve(relativePath);
            if (skipFilename(source)) {
                return;
            }
            if (looksLikeBinary(source)) {
                justCopy(source, destination);
            } else {
                copyWithFilters(source, destination);
            }
        } catch (IOException ex) {
            logger.warn("IOException for path: {}, relativePath: {}", source, relativePath);
            throw new RuntimeException(ex);
        }
    }

    private boolean skipFilename(Path source) {
        // skipping hidden files is ok, as this is only for stubs and solutions. Not for sandbox.
        if (FILES_TO_SKIP_ALLWAYS.matcher(source.getFileName().toString()).matches()) {
            logger.debug("Skipping file: {} ", source);
            return true;
        }
        logger.debug("Not skipping file: {} ", source);
        return false;
    }

    private boolean looksLikeBinary(Path source) {
        return NON_TEXT_TYPES.matcher(getFileExtension(source)).matches();
    }

    private void justCopy(Path source, Path destination) throws IOException {
        logger.debug("Just copying file from: {} to:{}", source, destination);
        FileUtils.copyFile(source.toFile(), destination.toFile());
    }

    private void copyWithFilters(Path source, Path destination) throws IOException {
        List<String> originalFile = FileUtils.readLines(source.toFile(), StandardCharsets.UTF_8);
        List<String> preparedFile = prepareFile(originalFile, getFileExtension(source));
        logger.debug("Filtered file while copying from: {} to:{}", source, destination);
        if (!originalFile.isEmpty() && preparedFile.isEmpty()) {
            logger.debug("skipped file as empty while copying from: {} to:{}", source, destination);
        } else {
            Files.createDirectories(destination.getParent());
            Files.write(
                    destination, preparedFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
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
        return data;
    }

    protected final String getFileExtension(Path file) {
        String name = file.getFileName().toString();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (IndexOutOfBoundsException e) {
            logger.info("Unable to determine file extension for: {}\n{}", file.toAbsolutePath(), e);
            return "";
        }
    }
}
