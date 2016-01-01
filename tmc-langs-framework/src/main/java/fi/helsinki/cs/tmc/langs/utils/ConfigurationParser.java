package fi.helsinki.cs.tmc.langs.utils;

import fi.helsinki.cs.tmc.langs.domain.ValueObject;

import java.nio.file.Path;
import java.util.Map;

public interface ConfigurationParser {
    public Map<String, ValueObject> parseOptions(Path path);
}
