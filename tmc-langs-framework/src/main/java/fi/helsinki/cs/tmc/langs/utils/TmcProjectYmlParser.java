package fi.helsinki.cs.tmc.langs.utils;

import fi.helsinki.cs.tmc.langs.domain.ValueObject;

import org.apache.commons.io.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TmcProjectYmlParser implements ConfigurationParser {

    public static final Path CONFIG_PATH = Paths.get(".tmcproject.yml");
    private static final Logger log = LoggerFactory.getLogger(TmcProjectYmlParser.class);

    private final Path configFilePath;

    public TmcProjectYmlParser(Path rootPath) {
        this.configFilePath = rootPath.resolve(CONFIG_PATH);
    }

    /**
     * Parses a list of extra student files from a <tt>.tmcproject.yml</tt> file.
     */
    public List<Path> parseExtraStudentFiles() {
        return parseExtraFiles("extra_student_files");
    }

    public List<Path> parseExtraExerciseFiles() {
        return parseExtraFiles("extra_exercise_files");
    }

    public List<Path> parseForceUpdateFiles() {
        return parseExtraFiles("force_update");
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

        parseRecursiveDefinitions(options, specsAsMap, "");

        return options;
    }

    private List<Path> parseExtraFiles(String key) {
        if (!Files.exists(configFilePath)) {
            return new ArrayList<>();
        }
        log.debug("Parsing " + key + " files from {}", configFilePath);

        List<Path> extraFiles = new ArrayList<>();

        Object yamlSpecifications = getYamlSpecs(configFilePath.toAbsolutePath());

        if (!(yamlSpecifications instanceof Map)) {
            return extraFiles;
        }

        Map<?, ?> specsAsMap = (Map<?, ?>) yamlSpecifications;
        Object fileMap = specsAsMap.get(key);
        addFiles(fileMap, extraFiles);

        return extraFiles;
    }

    private void parseRecursiveDefinitions(
            Map<String, ValueObject> options, Map<?, ?> specsAsMap, String preKey) {
        for (Object keyObject : specsAsMap.keySet()) {
            Object value = specsAsMap.get(keyObject);
            if (!(keyObject instanceof String)) {
                continue;
            }

            String key;
            if (preKey.isEmpty()) {
                key = (String) keyObject;
            } else {
                key = preKey + "." + (String) keyObject;
            }

            if (value instanceof Map) {
                parseRecursiveDefinitions(options, (Map<?, ?>) value, key);
            }
            options.put(key, new ValueObject(value));
        }
    }

    private Object getYamlSpecs(Path path) {
        String fileContents = initFileContents(path.toFile());
        Yaml yaml = new Yaml();
        return yaml.load(fileContents);
    }

    private void addFiles(Object files, List<Path> extraFiles) {
        addAllIfList(files, extraFiles);
        addIfString(files, extraFiles);
    }

    private void addAllIfList(Object files, List<Path> extraFiles) {
        if (files instanceof List) {
            log.trace("extra_student_files contains a list, parsing");
            for (Object value : (List<?>) files) {
                addIfString(value, extraFiles);
            }
        }
    }

    private void addIfString(Object value, List<Path> extraFiles) {
        if (value instanceof String) {
            String[] pathParts = ((String) value).split("/");
            Path path = constructPathfromArray(pathParts);
            extraFiles.add(path);
            log.trace("Added {} as extra student file", path);
        }
    }

    private Path constructPathfromArray(String[] parts) {
        Path path = Paths.get(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            path = path.resolve(parts[i]);
        }
        return path;
    }

    private String initFileContents(File file) {
        try {
            log.trace("Reading config file");
            if (file.exists()) {
                return FileUtils.readFileToString(file);
            }
        } catch (IOException e) {
            log.warn("Unable to read config file at {}", file, e);
        }
        return "";
    }
}
