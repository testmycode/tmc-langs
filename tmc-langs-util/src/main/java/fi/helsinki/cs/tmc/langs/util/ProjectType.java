package fi.helsinki.cs.tmc.langs.util;

import fi.helsinki.cs.tmc.langs.LanguagePlugin;
import fi.helsinki.cs.tmc.langs.NoTestsPlugin;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;
import fi.helsinki.cs.tmc.langs.java.ant.AntPlugin;
import fi.helsinki.cs.tmc.langs.java.maven.MavenPlugin;
import fi.helsinki.cs.tmc.langs.make.MakePlugin;
import fi.helsinki.cs.tmc.langs.python3.Python3Plugin;
import fi.helsinki.cs.tmc.langs.qmake.QmakePlugin;
import fi.helsinki.cs.tmc.langs.r.RPlugin;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * All the possible project types.
 */
public enum ProjectType {
    NO_TESTS(new NoTestsPlugin()), // Must be before Makefile for compatability reasons
    QMAKE(new QmakePlugin()),
    MAKEFILE(new MakePlugin()),
    PYTHON3(new Python3Plugin()),
    R(new RPlugin()),
    JAVA_MAVEN(new MavenPlugin()),
    // for legacy and matching reasons keep the java ant as the last choise.
    JAVA_ANT(new AntPlugin());

    private final LanguagePlugin languagePlugin;
    private static final Logger log = LoggerFactory.getLogger(ProjectType.class);

    ProjectType(LanguagePlugin languagePlugin) {
        this.languagePlugin = languagePlugin;
    }

    public LanguagePlugin getLanguagePlugin() {
        return languagePlugin;
    }

    /**
     * Recognizes the project type.
     *
     * <p>Iterate through all language plugins to find one that recognizes the
     * project as their type.
     *
     * @param path The path to the exercise directory.
     * @return The project type that recognizes the project.
     */
    public static ProjectType getProjectType(Path path) throws NoLanguagePluginFoundException {
        log.info("Finding plugin for {}", path);
        for (ProjectType type : ProjectType.values()) {
            try {
                if (type.getLanguagePlugin().isExerciseTypeCorrect(path)) {
                    log.info("Detected project as {}", type.getLanguagePlugin().getPluginName());
                    return type;
                }
            } catch (ExceptionInInitializerError e) {
                log.warn(
                        "Exception while checking for exercise type, tried for {}. Exception: {}",
                        type,
                        e);
            }
        }

        log.error("No suitable language plugin found for project at {}", path);
        throw new NoLanguagePluginFoundException("No suitable language plugin found.");
    }
}
