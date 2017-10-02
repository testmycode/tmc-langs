package fi.helsinki.cs.tmc.langs.domain;

import fi.helsinki.cs.tmc.langs.utils.TmcProjectYmlParser;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Configuration {

    private final TmcProjectYmlParser tmcProjectYmlParser;
    private final Path path;
    private Map<String, ValueObject> options;
    public static final Path TMC_PROJECT_YML = Paths.get(".tmcproject.yml");

    @VisibleForTesting
    public Configuration() {
        tmcProjectYmlParser = new TmcProjectYmlParser();
        options = new HashMap<>();
        path = null;
    }

    public Configuration(Path path) {
        this.path = path;
        tmcProjectYmlParser = new TmcProjectYmlParser();
        parseOptions(path);
    }

    public boolean isSet(String config) {
        return options.containsKey(config);
    }

    /**
     * Returns the configuration option for the given key if it exists.
     *
     * @param key   Name of the configuration.
     * @return      Value of the configuration. Null if not set.
     */
    public ValueObject get(String key) {
        if (isSet(key)) {
            return options.get(key);
        }
        return null;
    }

    public List<Path> getExtraStudentFiles() {
        return tmcProjectYmlParser.parseExtraStudentFiles(path.resolve(TMC_PROJECT_YML));
    }

    /**
     * Parse options from the path.
     *
     * @param path Absolute path to configuration, e.g. .tmcproject.yml -file.
     */
    void parseOptions(Path path) {
        this.options = parseTmcProjectYmlOptions(path);
    }

    private Map<String, ValueObject> parseTmcProjectYmlOptions(Path path) {
        Path configFile = path.resolve(TMC_PROJECT_YML);
        if (Files.exists(path)) {
            return tmcProjectYmlParser.parseOptions(configFile);
        }
        return Maps.newHashMap();
    }
}
