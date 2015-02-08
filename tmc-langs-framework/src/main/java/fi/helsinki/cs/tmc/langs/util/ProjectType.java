package fi.helsinki.cs.tmc.langs.util;

import fi.helsinki.cs.tmc.langs.LanguagePlugin;

/**
 * All the possible project types
 */
public enum ProjectType {
    JAVA_SIMPLE(new AntPlugin());

    private final LanguagePlugin languagePlugin;

    ProjectType(LanguagePlugin languagePlugin) {
        this.languagePlugin = languagePlugin;
    }

    public LanguagePlugin getLanguagePlugin() {
        return languagePlugin;
    }
}
