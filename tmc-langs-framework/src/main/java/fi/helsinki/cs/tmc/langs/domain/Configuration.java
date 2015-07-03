package fi.helsinki.cs.tmc.langs.domain;

import fi.helsinki.cs.tmc.langs.utils.TmcProjectYmlParser;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Configuration {
    Map<String, ValueObject> options;

    public Configuration() {
        options = new HashMap<>();
    }

    public Configuration(Path path) {
        parseOptions(path);
    }

    public boolean isSet(String config) {
        return options.keySet().contains(config);
    }

    public Object get(String key) {
        if (isSet(key)) {
            return options.get(key);
        }
        return null;
    }

    public void parseOptions(Path path) {
        TmcProjectYmlParser parser = new TmcProjectYmlParser();
        options = parser.parseOptions(path);
    }
}
