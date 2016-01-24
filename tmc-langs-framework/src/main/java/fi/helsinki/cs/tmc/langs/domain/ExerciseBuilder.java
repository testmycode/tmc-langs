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
    private static final DirectorySkipper GENERAL_DIRECTORY_SKIPPER = new GeneralDirectorySkipper();

    private static final Logger logger = LoggerFactory.getLogger(ExerciseBuilder.class);

    /**
     * Prepares a stub exercise from the original.
     */
    public void prepareStubs(
            Map<Path, LanguagePlugin> exerciseMap, final Path repoPath, final Path destPath) {
        for (Map.Entry<Path, LanguagePlugin> project : exerciseMap.entrySet()) {

            // Copy exercises over file by file
            logger.info("Project: {}", project.getKey());

            new FilterFileTreeVisitor()
                    .setClonePath(repoPath)
                    .setExercisePath(project.getKey())
                    .addSkipper(GENERAL_DIRECTORY_SKIPPER)
                    .setFiler(
                            new StubFileFilterProcessor()
                                    .setToPath(destPath)
                                    .setLanguagePlugin(project.getValue()))
                    .traverse();
            Path relativePath =
                    project.getKey()
                            .subpath(repoPath.getNameCount(), project.getKey().getNameCount());
            project.getValue().maybeCopySharedStuff(destPath.resolve(relativePath));
        }
    }

    /**
     * Prepares a presentable solution from the original.
     */
    public void prepareSolutions(
            Map<Path, LanguagePlugin> exerciseMap, final Path repoPath, final Path destPath) {

        for (Map.Entry<Path, LanguagePlugin> project : exerciseMap.entrySet()) {
            new FilterFileTreeVisitor()
                    .setClonePath(repoPath)
                    .setExercisePath(project.getKey())
                    .addSkipper(GENERAL_DIRECTORY_SKIPPER)
                    .setFiler(
                            new SolutionFileFilterProcessor()
                                    .setToPath(destPath)
                                    .setLanguagePlugin(project.getValue()))
                    .traverse();
        }
    }
}
