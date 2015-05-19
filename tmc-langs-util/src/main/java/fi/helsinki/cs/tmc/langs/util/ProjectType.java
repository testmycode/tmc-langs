package fi.helsinki.cs.tmc.langs.util;

import fi.helsinki.cs.tmc.langs.LanguagePlugin;
import fi.helsinki.cs.tmc.langs.ant.AntPlugin;

/**
 * All the possible project types
 */
public enum ProjectType {

    JAVA_ANT(new AntPlugin());

    private final LanguagePlugin languagePlugin;

    ProjectType(LanguagePlugin languagePlugin) {
        this.languagePlugin = languagePlugin;
    }

    public LanguagePlugin getLanguagePlugin() {
        return languagePlugin;
    }
}
