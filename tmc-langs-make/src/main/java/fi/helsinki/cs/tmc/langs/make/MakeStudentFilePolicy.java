package fi.helsinki.cs.tmc.langs.make;

import fi.helsinki.cs.tmc.langs.io.ConfigurableStudentFilePolicy;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class MakeStudentFilePolicy extends ConfigurableStudentFilePolicy {

    private static final Path MAKEFILE_PATH = Paths.get("Makefile");
    private static final Path SOURCE_FOLDER_PATH = Paths.get("src");

    public MakeStudentFilePolicy(Path configFileParent) {
        super(configFileParent);
    }

    /**
     * Returns {@code True} for all files in the <tt>projectRoot/src</tt> directory and other
     * files required for building the project.
     *
     * <p>Will NOT return {@code True} for any test files. If test file modification are part
     * of the exercise, those test files are whitelisted as <tt>ExtraStudentFiles</tt> and the
     * decision to move them is made by {@link ConfigurableStudentFilePolicy}.
     */
    @Override
    public boolean isStudentSourceFile(Path path, Path projectRootPath) {
        return !path.endsWith(MAKEFILE_PATH) && path.startsWith(SOURCE_FOLDER_PATH);
    }
}
