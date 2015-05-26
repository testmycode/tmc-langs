package fi.helsinki.cs.tmc.langs.util;

import fi.helsinki.cs.tmc.langs.LanguagePlugin;
import fi.helsinki.cs.tmc.langs.NoLanguagePluginFoundException;

import java.nio.file.Path;

public class ProjectTypeHandler {

    /**
     * Recognizes the project type.
     *
     * Iterate through all language plugins to find one that recognizes the
     * project as their type.
     *
     * @param path The path to the exercise directory.
     * @return The project type that recognizes the project.
     */
    static ProjectType getProjectType(Path path) throws NoLanguagePluginFoundException {
        for (ProjectType type : ProjectType.values()) {
            if (type.getLanguagePlugin().scanExercise(path, "").isPresent()) {
                return type;
            }
        }

        throw new NoLanguagePluginFoundException("No suitable language plugin found.");
    }

    /**
     * Return the LanguagePlugin that is responsible for the exercise at the
     * given path.
     *
     * @param path The path to the exercise directory.
     * @return LanguagePlugin that is responsible for the exercise.
     */
    public static LanguagePlugin getLanguagePlugin(Path path) throws NoLanguagePluginFoundException {
        return getProjectType(path).getLanguagePlugin();
    }

}
