package fi.helsinki.cs.tmc.langs.domain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class Filer {

    private static final Logger logger = LoggerFactory.getLogger(Filer.class);

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
                    logger.info("skipped file as empty while copying from: {} to:{}", file, toFile);
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
