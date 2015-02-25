package fi.helsinki.cs.tmc.langs.util;


import fi.helsinki.cs.tmc.langs.LanguagePlugin;

import java.nio.file.Path;

public class ProjectTypeHandler {

    /**
     * Recognise the project type.
     *
     * Iterate through all language plugins to find one that recognises the
     * project as their type.
     *
     * @param path The path to the exercise directory.
     * @return The project type, or null if none.
     */
    public ProjectType getProjectType(Path path) {
        for (ProjectType type : ProjectType.values()) {
            if (type.getLanguagePlugin().scanExercise(path, "") != null) {
                return type;
            }
        }

        return null;
    }

    /**
     * Return the LanguagePlugin that is responsible for the exercise at the given path.
     *
     * @param path The path to the exercise directory.
     * @return LanguagePlugin that is responsible for the exercise, or null if none.
     */
    public LanguagePlugin getLanguagePlugin(Path path) {
        ProjectType type = getProjectType(path);
        return type == null ? null : getProjectType(path).getLanguagePlugin();
    }

}
