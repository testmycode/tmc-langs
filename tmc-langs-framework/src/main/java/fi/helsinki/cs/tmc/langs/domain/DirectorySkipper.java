package fi.helsinki.cs.tmc.langs.domain;

import java.nio.file.Path;

interface DirectorySkipper {

    /**
     * Returns true if directory and it's subdirectories should be ignored.
     */
    boolean skipDirectory(Path directory);
}
