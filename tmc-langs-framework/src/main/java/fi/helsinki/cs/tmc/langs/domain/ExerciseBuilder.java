package fi.helsinki.cs.tmc.langs.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Builder for generating stubs and model solutions.
 */
public class ExerciseBuilder {

    private static final String SOURCE_FOLDER_NAME = "src";
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final Logger logger = LoggerFactory.getLogger(ExerciseBuilder.class);

    /**
     * Prepares a stub exercise from the original.
     *
     * <p>Implements LanguagePlugin.prepareStub
     */
    public void prepareStub(final Path clonePath, final Path destPath) {
        new FilterFileTreeVisitor()
                .setClonePath(clonePath)
                .setDestPath(destPath)
                .addSkipper(new GeneralDirectorySkipper())
                .setFiler(new StubFileFilterProcessor())
                .traverse();
    }

    /**
     * Prepares a presentable solution from the original.
     *
     * <p>Implements LanguagePlugin.prepareSolution
     */
    public void prepareSolution(final Path clonePath, final Path destPath) {
        new FilterFileTreeVisitor()
                .setClonePath(clonePath)
                .setDestPath(destPath)
                .addSkipper(new GeneralDirectorySkipper())
                .setFiler(new SolutionFileFilterProcessor())
                .traverse();
    }
}
