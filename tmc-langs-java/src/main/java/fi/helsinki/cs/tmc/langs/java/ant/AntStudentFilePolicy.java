package fi.helsinki.cs.tmc.langs.java.ant;

import fi.helsinki.cs.tmc.langs.io.ConfigurableStudentFilePolicy;

import java.nio.file.Path;
import java.nio.file.Paths;

public class AntStudentFilePolicy extends ConfigurableStudentFilePolicy {

    public AntStudentFilePolicy(Path configFile) {
        super(configFile);
    }

    /**
     * Returns {@code True} for all files in the <tt>projectRoot/src</tt> directory and other
     * files required for building the project.
     *
     * <p>Will NOT return {@code True} for any test files. If test file modification are part
     * of the exercise, those test files are whitelisted as <tt>ExtraStudentFiles</tt> and the
     * decision to include them is made by {@link ConfigurableStudentFilePolicy}.
     */
    @Override
    public boolean isStudentSourceFile(Path path) {
        return path.startsWith(Paths.get("src"));
    }
}
