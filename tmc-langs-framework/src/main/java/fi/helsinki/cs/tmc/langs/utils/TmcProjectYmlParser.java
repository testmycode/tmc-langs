package fi.helsinki.cs.tmc.langs.utils;

import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;

import org.apache.commons.io.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TmcProjectYmlParser {

    private Logger log = LoggerFactory.getLogger(TmcProjectYmlParser.class);

    private Path rootPath;
    private List<Path> extraStudentFiles;

    /**
     * Parses a list of extra student files from a <tt>.tmcproject.yml</tt> file.
     */
    public List<Path> parseExtraStudentFiles(Path configFilePath, Path projectRootPath) {

        log.debug("Parsing extra student files from {}", configFilePath);

        rootPath = projectRootPath;
        extraStudentFiles = new ArrayList<>();

        String fileContents = initFileContents(configFilePath.toAbsolutePath().toFile());
        Yaml yaml = new Yaml();
        Object yamlSpecifications = yaml.load(fileContents);

        if (!(yamlSpecifications instanceof Map)) {
            return extraStudentFiles;
        }

        Map<?, ?> specsAsMap = (Map<?, ?>) yamlSpecifications;
        Object fileMap = specsAsMap.get("extra_student_files");
        addFiles(fileMap);

        return extraStudentFiles;
    }

    private void addFiles(Object files) {
        addAllIfList(files);
        addIfString(files);
    }

    private void addAllIfList(Object files) {
        if (files instanceof List) {
            log.trace("extra_student_files contains a list, parsing");
            for (Object value : (List<?>) files) {
                addIfString(value);
            }
        }
    }

    private void addIfString(Object value) {
        if (value instanceof String) {
            Path path = this.rootPath.resolve((String) value);
            extraStudentFiles.add(path);
            log.trace("Added {} as extra student file", path);
        }
    }

    private String initFileContents(File file) {
        try {
            log.trace("Reading config file");
            return FileUtils.readFileToString(file);
        } catch (IOException e) {
            log.warn("Unable to read config file at {}", file, e);
            return "";
        }
    }
}
