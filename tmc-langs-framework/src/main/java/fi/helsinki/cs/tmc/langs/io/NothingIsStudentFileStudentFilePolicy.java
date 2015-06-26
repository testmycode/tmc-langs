package fi.helsinki.cs.tmc.langs.io;

import java.nio.file.Path;

/**
 * A {@link StudentFilePolicy} that defines all files to be non-student files.
 */
public class NothingIsStudentFileStudentFilePolicy implements StudentFilePolicy {

    @Override
    public boolean isStudentFile(Path path, Path projectRootPath) {
        return false;
    }
}
