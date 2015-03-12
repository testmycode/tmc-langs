package fi.helsinki.cs.tmc.langs.util;


import com.google.common.base.Optional;
import fi.helsinki.cs.tmc.langs.LanguagePlugin;
import org.apache.tools.ant.Project;

import javax.swing.text.html.Option;
import java.nio.file.Path;

public class ProjectTypeHandler {

    /**
     * Recognise the project type.
     *
     * Iterate through all language plugins to find one that recognises the
     * project as their type.
     *
     * @param path The path to the exercise directory.
     * @return The project type, or Optional absent if none.
     */
    static Optional<ProjectType> getProjectType(Path path) {
        for (ProjectType type : ProjectType.values()) {
            if (type.getLanguagePlugin().scanExercise(path, "") != null) {
                return Optional.of(type);
            }
        }
        return Optional.absent();
    }

    /**
     * Return the LanguagePlugin that is responsible for the exercise at the given path.
     *
     * @param path The path to the exercise directory.
     * @return LanguagePlugin that is responsible for the exercise, or Optional absent if none.
     */
    public static Optional<LanguagePlugin> getLanguagePlugin(Path path) {
        Optional<ProjectType> type = getProjectType(path);
        if (type.isPresent()) {
            return Optional.of(type.get().getLanguagePlugin());
        }
        return Optional.absent();
    }

}
