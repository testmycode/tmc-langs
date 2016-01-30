package fi.helsinki.cs.tmc.langs.domain;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public final class GeneralDirectorySkipper implements DirectorySkipper {

    private static final List<String> directoryNameSkiplist =
            Arrays.asList(new String[] {".git", "private"});

    @Override
    public boolean skipDirectory(Path directory) {
        return directory.toFile().isDirectory()
                && (directoryNameSkiplist.contains(directory.getFileName().toString())
                        || Files.exists(
                                Paths.get(directory.toAbsolutePath().toString(), ".tmcignore"))
                        || directory.getFileName().startsWith("."));
    }
}
