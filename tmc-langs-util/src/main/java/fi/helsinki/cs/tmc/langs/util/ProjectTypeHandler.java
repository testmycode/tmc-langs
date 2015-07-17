package fi.helsinki.cs.tmc.langs.util;

import fi.helsinki.cs.tmc.langs.LanguagePlugin;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class ProjectTypeHandler {

    private static Logger log = LoggerFactory.getLogger(ProjectTypeHandler.class);

    /**
     * Recognizes the project type.
     *
     * <p>Iterate through all language plugins to find one that recognizes the
     * project as their type.
     *
     * @param path The path to the exercise directory.
     * @return The project type that recognizes the project.
     */
    static ProjectType getProjectType(Path path) throws NoLanguagePluginFoundException {
        log.info("Finding plugin for {}", path);
        for (ProjectType type : ProjectType.values()) {
            if (type.getLanguagePlugin().scanExercise(path, "").isPresent()) {
                log.info("Detected project as {}", type.getLanguagePlugin().getLanguageName());
                return type;
            }
        }

        log.error("No suitable language plugin found for project at {}", path);
        throw new NoLanguagePluginFoundException("No suitable language plugin found.");
    }

    /**
     * Return the LanguagePlugin that is responsible for the exercise at the
     * given path.
     *
     * @param path The path to the exercise directory.
     * @return LanguagePlugin that is responsible for the exercise.
     */
    public static LanguagePlugin getLanguagePlugin(Path path)
            throws NoLanguagePluginFoundException {
        return getProjectType(path).getLanguagePlugin();
    }

    public static boolean isExerciseDirectory(Path path) {
        for (ProjectType projectType : ProjectType.values()) {
            if (projectType.getLanguagePlugin().isExerciseTypeCorrect(path)) {
                return true;
            }
        }
        return false;
    }

}
