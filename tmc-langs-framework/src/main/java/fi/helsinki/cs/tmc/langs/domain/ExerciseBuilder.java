package fi.helsinki.cs.tmc.langs.domain;

import fi.helsinki.cs.tmc.langs.LanguagePlugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

/**
 * Builder for generating stubs and model solutions.
 */
public class ExerciseBuilder {

    private static final String SOURCE_FOLDER_NAME = "src";
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final Logger logger = LoggerFactory.getLogger(ExerciseBuilder.class);

    /**
     * Prepares a stub exercise from the original.
     */
    public void prepareStubs(Map<Path, LanguagePlugin> exerciseMap, final Path destPath) {
        for (Map.Entry<Path, LanguagePlugin> project : exerciseMap.entrySet()) {
            // Copy exercises over file by file
            new FilterFileTreeVisitor()
                    .setClonePath(project.getKey())
                    .addSkipper(new GeneralDirectorySkipper())
                    .setFiler(
                            new StubFileFilterProcessor()
                                    .setToPath(destPath)
                                    .setLanguagePlugin(project.getValue()))
                    .traverse();
            Path relativePath =
                    project.getKey()
                            .subpath(
                                    project.getKey().getNameCount() - 1,
                                    project.getKey().getNameCount());
            project.getValue().maybeCopySharedStuff(destPath.resolve(relativePath));
        }
    }

    /**
     * Prepares a presentable solution from the original.
     */
    public void prepareSolutions(Map<Path, LanguagePlugin> exerciseMap, final Path destPath) {

        for (Map.Entry<Path, LanguagePlugin> entrySet : exerciseMap.entrySet()) {
            new FilterFileTreeVisitor()
                    .setClonePath(entrySet.getKey())
                    .addSkipper(new GeneralDirectorySkipper())
                    .setFiler(
                            new SolutionFileFilterProcessor()
                                    .setToPath(destPath)
                                    .setLanguagePlugin(entrySet.getValue()))
                    .traverse();
        }
    }
}
