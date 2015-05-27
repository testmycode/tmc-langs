package fi.helsinki.cs.tmc.langs.util;

import fi.helsinki.cs.tmc.langs.LanguagePlugin;
import fi.helsinki.cs.tmc.langs.ant.AntPlugin;
import fi.helsinki.cs.tmc.langs.maven.MavenPlugin;

/**
 * All the possible project types.
 */
public enum ProjectType {

    JAVA_ANT(new AntPlugin()),
    JAVA_MAVEN(new MavenPlugin());


    private final LanguagePlugin languagePlugin;

    ProjectType(LanguagePlugin languagePlugin) {
        this.languagePlugin = languagePlugin;
    }

    public LanguagePlugin getLanguagePlugin() {
        return languagePlugin;
    }
}
