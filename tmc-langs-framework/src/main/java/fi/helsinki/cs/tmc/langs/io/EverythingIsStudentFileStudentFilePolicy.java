package fi.helsinki.cs.tmc.langs.io;

import java.nio.file.Path;

/**
 * A {@link StudentFilePolicy} that defines all files as student files.
 */
public final class EverythingIsStudentFileStudentFilePolicy implements StudentFilePolicy {

    @Override
    public boolean isStudentFile(Path path, Path projectRootPath) {
        return true;
    }

    @Override
    public boolean mayDelete(Path file, Path projectRoot) {
        return false;
    }

    @Override
    public boolean isUpdatingForced(Path path, Path projectRootPath) {
        return false;
    }
}
