package fi.helsinki.cs.tmc.langs.domain;

import fi.helsinki.cs.tmc.langs.LanguagePlugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Builder for generating stubs and model solutions.
 */
public class ExerciseBuilder {

    private static final DirectorySkipper GENERAL_DIRECTORY_SKIPPER = new GeneralDirectorySkipper();

    private static final Logger logger = LoggerFactory.getLogger(ExerciseBuilder.class);

    /**
     * Prepares a stub exercise from the original.
     */
    public void prepareStubs(
            Map<Path, LanguagePlugin> exerciseMap, final Path repoPath, final Path destPath) {

        // Copy exercises over file by file per project
        for (Map.Entry<Path, LanguagePlugin> project : exerciseMap.entrySet()) {

            logger.info("Project: {}", project.getKey());

            Path relativePath;
            if (repoPath.getNameCount() < project.getKey().getNameCount()) {
                System.out.format(
                        "repo: %d proj: %d\n",
                        repoPath.getNameCount(),
                        project.getKey().getNameCount());
                relativePath =
                        project.getKey()
                                .subpath(repoPath.getNameCount(), project.getKey().getNameCount());
            } else {
                relativePath = Paths.get("");
            }
            new FilterFileTreeVisitor()
                    .setClonePath(repoPath)
                    .setStartPath(project.getKey())
                    .addSkipper(GENERAL_DIRECTORY_SKIPPER)
                    .setFiler(new StubFileFilterProcessor().setToPath(destPath))
                    .traverse();
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
                    .setStartPath(project.getKey())
                    .addSkipper(GENERAL_DIRECTORY_SKIPPER)
                    .setFiler(new SolutionFileFilterProcessor().setToPath(destPath))
                    .traverse();
        }
    }
}
