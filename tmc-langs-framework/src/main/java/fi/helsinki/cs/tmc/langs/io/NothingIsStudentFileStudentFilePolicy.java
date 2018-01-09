package fi.helsinki.cs.tmc.langs.io;

import java.nio.file.Path;

/**
 * A {@link StudentFilePolicy} that defines all files to be non-student files.
 */
public final class NothingIsStudentFileStudentFilePolicy implements StudentFilePolicy {

    @Override
    public boolean isStudentFile(Path path, Path projectRootPath) {
        return false;
    }

    @Override
    public boolean mayDelete(Path file, Path projectRoot) {
        return !isStudentFile(file, projectRoot);
    }

    @Override
    public boolean isUpdatingForced(Path path, Path projectRootPath) {
        return false;
    }
}
