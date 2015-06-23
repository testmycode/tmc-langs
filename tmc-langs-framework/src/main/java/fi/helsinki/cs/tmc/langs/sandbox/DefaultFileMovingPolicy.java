package fi.helsinki.cs.tmc.langs.sandbox;

import java.nio.file.Path;

/**
 * A {@link FileMovingPolicy} that moves all files.
 */
public class DefaultFileMovingPolicy implements FileMovingPolicy {

    @Override
    public boolean shouldMove(Path path, Path rootPath) {
        return true;
    }
}
