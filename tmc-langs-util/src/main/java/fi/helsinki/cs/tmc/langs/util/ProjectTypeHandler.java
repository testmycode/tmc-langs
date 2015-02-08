package fi.helsinki.cs.tmc.langs.util;


import fi.helsinki.cs.tmc.langs.LanguagePlugin;

import java.nio.file.Path;
import java.util.HashMap;

public class ProjectTypeHandler {

    private final HashMap<ProjectType, LanguagePlugin> projectTypes = new HashMap<>();

    /**
     * Register all the available LanguagePlugins to ProjectTypeHandler
     */
    public ProjectTypeHandler() {
        for(ProjectType type : ProjectType.values()) {
            projectTypes.put(type, type.getLanguagePlugin());
        }
    }

    /**
     * Recognise the project type.
     *
     * <p>
     * Iterate through all language plugins to find one that recognises the
     * project as their type.
     *
     * @param path The path to the exercise directory.
     * @return The project type, or null if none.
     */
    public ProjectType getProjectType(Path path) {
        for(ProjectType type : projectTypes.keySet()) {
            if(type.getLanguagePlugin().isExerciseTypeCorrect(path)) {
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
        return projectTypes.get(getProjectType(path));
    }

}
