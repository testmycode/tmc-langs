package fi.helsinki.cs.tmc.langs.rust;

import fi.helsinki.cs.tmc.langs.io.ConfigurableStudentFilePolicy;
import fi.helsinki.cs.tmc.langs.rust.util.Constants;

import java.nio.file.Path;

public class CargoStudentFilePolicy extends ConfigurableStudentFilePolicy {

    public CargoStudentFilePolicy(Path configFileParent) {
        super(configFileParent);
    }
    
    /**
     * Returns {@code true} for all files in the <tt>projectRoot/src</tt> directory and other
     * files required for building the project.
     *
     * <p>Will NOT return {@code true} for any test files. If test file modification are part
     * of the exercise, those test files are whitelisted as <tt>ExtraStudentFiles</tt> and the
     * decision to move them is made by {@link ConfigurableStudentFilePolicy}.
     */
    @Override
    public boolean isStudentSourceFile(Path path) {
        return !path.endsWith(Constants.CARGO_TOML) && path.startsWith(Constants.SOURCE);
    }
    
}
