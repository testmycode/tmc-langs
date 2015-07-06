package fi.helsinki.cs.tmc.langs.domain;

import fi.helsinki.cs.tmc.langs.utils.ConfigurationParser;
import fi.helsinki.cs.tmc.langs.utils.TmcProjectYmlParser;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Configuration {

    private Map<String, ValueObject> options;

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

    public void parseOptions(Path path) {
        ConfigurationParser parser = new TmcProjectYmlParser();
        options = parser.parseOptions(path);
    }
}
