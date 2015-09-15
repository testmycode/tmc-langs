package fi.helsinki.cs.tmc.langs.domain;

import fi.helsinki.cs.tmc.langs.utils.ConfigurationParser;
import fi.helsinki.cs.tmc.langs.utils.TmcProjectYmlParser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public final class Configuration {

    private Map<String, ValueObject> options;
    public static final Path TMC_PROJECT_YML = Paths.get(".tmcproject.yml");

    public Configuration() {
        options = new HashMap<>();
    }

    public Configuration(Path path) {
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

    /**
     * Parse options from the path.
     *
     * @param path Absolute path to configuration, e.g. .tmcproject.yml -file.
     */
    public void parseOptions(Path path) {
        this.options = parseTmcProjectYmlOptions(path);
    }

    private Map<String, ValueObject> parseTmcProjectYmlOptions(Path path) {
        ConfigurationParser parser = new TmcProjectYmlParser();
        Path configFile = path.resolve(TMC_PROJECT_YML);
        return parser.parseOptions(configFile);
    }
}
