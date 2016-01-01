package fi.helsinki.cs.tmc.langs.utils;

import fi.helsinki.cs.tmc.langs.domain.ValueObject;

import org.apache.commons.io.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TmcProjectYmlParser implements ConfigurationParser {

    private static final Logger log = LoggerFactory.getLogger(TmcProjectYmlParser.class);

    private Path rootPath;
    private List<Path> extraStudentFiles;

    /**
     * Parses a list of extra student files from a <tt>.tmcproject.yml</tt> file.
     */
    public List<Path> parseExtraStudentFiles(Path configFilePath, Path projectRootPath) {

        log.debug("Parsing extra student files from {}", configFilePath);

        rootPath = projectRootPath;
        extraStudentFiles = new ArrayList<>();

        Object yamlSpecifications = getYamlSpecs(configFilePath.toAbsolutePath());

        if (!(yamlSpecifications instanceof Map)) {
            return extraStudentFiles;
        }

        Map<?, ?> specsAsMap = (Map<?, ?>) yamlSpecifications;
        Object fileMap = specsAsMap.get("extra_student_files");
        addFiles(fileMap);

        return extraStudentFiles;
    }

    @Override
    public Map<String, ValueObject> parseOptions(Path configFile) {
        log.debug("Parsing configuration from {}", configFile);

        Object yamlSpecifications = getYamlSpecs(configFile);

        if (!(yamlSpecifications instanceof Map)) {
            return new HashMap<>();
        }

        Map<?, ?> specsAsMap = (Map<?, ?>) yamlSpecifications;
        Map<String, ValueObject> options = new HashMap<>();

        for (Object key : specsAsMap.keySet()) {
            Object value = specsAsMap.get(key);
            if (!(key instanceof String)) {
                continue;
            }
            options.put((String) key, new ValueObject(value));
        }

        return options;
    }

    private Object getYamlSpecs(Path path) {
        String fileContents = initFileContents(path.toFile());
        Yaml yaml = new Yaml();
        return yaml.load(fileContents);
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
