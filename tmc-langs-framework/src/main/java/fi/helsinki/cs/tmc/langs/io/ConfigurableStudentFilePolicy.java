package fi.helsinki.cs.tmc.langs.io;

import fi.helsinki.cs.tmc.langs.utils.TmcProjectYmlParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * An abstract {@link StudentFilePolicy} that also uses
 * <a href="http://tmc.mooc.fi/usermanual/pages/instructors.html#_tmcproject_yml">
 * ExtraStudentFiles</a> to make a decision on whether a file is a student file or not.
 *
 * <p>For any {@link StudentFilePolicy} that extends this class, a file is a student file if either
 * {@link ConfigurableStudentFilePolicy#isExtraStudentFile(Path)} or
 * {@link ConfigurableStudentFilePolicy#isStudentSourceFile(Path, Path)} returns {@code True}.
 */
public abstract class ConfigurableStudentFilePolicy implements StudentFilePolicy {

    private static final Logger log = LoggerFactory.getLogger(ConfigurableStudentFilePolicy.class);

    private static final Path CONFIG_PATH = Paths.get(".tmcproject.yml");

    private Path configFile;

    private List<Path> extraStudentFiles;
    private Path rootPath;

    public ConfigurableStudentFilePolicy(Path configFileParent) {
        this.configFile = configFileParent.resolve(CONFIG_PATH);
    }

    /**
     * Determines whether a file is a student source file.
     *
     * <p>A file should be considered a student source file if it resides in a location the student
     * is expected to create his or her own source files in the general case. Any special cases
     * are specified as ExtraStudentFiles in a separate configuration.
     *
     * <p>For example in a Java project that uses Apache Ant, {@code isStudentSourceFile} should
     * return {@code True} for any files in the <tt>src</tt> directory.
     */
    // Basically tells whether we should continue recursion deeper into the directory.
    public abstract boolean isStudentSourceFile(Path path, Path projectRootPath);

    @Override
    public boolean isStudentFile(Path path, Path projectRootPath) {
        log.trace("Looking into path: {} root: {}", path, projectRootPath);

        if (!Files.exists(path)) {
            return false;
        }

        if (path.getFileName().equals(configFile.getFileName())) {
            return false;
        }

        this.rootPath = projectRootPath;

        return isExtraStudentFile(path)
                || projectRootPath.equals(path)
                || isStudentSourceFile(
                        path.subpath(projectRootPath.getNameCount(), path.getNameCount()),
                        projectRootPath);
    }

    /** Determines whether a file is an <tt>ExtraStudentFile</tt>. */
    private boolean isExtraStudentFile(Path path) {
        if (extraStudentFiles == null) {
            loadExtraStudentFileList();
        }

        for (Path extraStudentFile : extraStudentFiles) {
            Path extraStudentPath = rootPath.resolve(extraStudentFile).toAbsolutePath();
            Path userSuppliedPath = path.toAbsolutePath();
            if (extraStudentPath.equals(userSuppliedPath)
                    || (userSuppliedPath.startsWith((extraStudentPath))
                            && Files.isDirectory(extraStudentPath))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Loads the <tt>ExtraStudentFiles</tt> of the project specified during construction.
     *
     * <p>More specifically, this reads the <tt>.tmcproject.yml</tt> file from the project root
     * and parses it for the necessary information.
     */
    private void loadExtraStudentFileList() {
        extraStudentFiles = new ArrayList<>();

        if (Files.exists(configFile)) {
            TmcProjectYmlParser parser = new TmcProjectYmlParser();
            extraStudentFiles = parser.parseExtraStudentFiles(configFile);
        }
    }

    @Override
    public boolean mayDelete(Path file, Path projectRoot) {
        return !isStudentFile(file, projectRoot);
    }
}
