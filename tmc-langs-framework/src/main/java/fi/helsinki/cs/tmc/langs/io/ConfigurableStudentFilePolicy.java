package fi.helsinki.cs.tmc.langs.io;

import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An abstract {@link StudentFilePolicy} that also uses
 * <a href="http://tmc.mooc.fi/usermanual/pages/instructors.html#_tmcproject_yml">
 * ExtraStudentFiles</a> to make a decision on whether a file is a student file or not.
 *
 * <p>For any {@link StudentFilePolicy} that extends this class, a file is a student file if either
 * {@link ConfigurableStudentFilePolicy#isExtraStudentFile(Path)} or
 * {@link ConfigurableStudentFilePolicy#isStudentSourceFile(Path)} returns {@code True}.
 */
public abstract class ConfigurableStudentFilePolicy implements StudentFilePolicy {

    private Path configFile;

    private List<Path> extraStudentFiles;
    private Path rootPath;

    public ConfigurableStudentFilePolicy(Path configFile) {
        this.configFile = configFile;
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
    public abstract boolean isStudentSourceFile(Path path);
    
    @Override
    public boolean isStudentFile(Path path, Path projectRootPath) {
        if (!path.toFile().exists()) {
            return false;
        }

        if (path.toFile().isDirectory()) {
            return false;
        }

        if (path.getFileName().equals(configFile.getFileName())) {
            return false;
        }

        this.rootPath = projectRootPath;
        
        return isExtraStudentFile(path) || isStudentSourceFile(path);
    }

    /**
     * Determines whether a file is an <tt>ExtraStudentFile</tt>.
     */
    private boolean isExtraStudentFile(Path path) {
        if (extraStudentFiles == null) {
            loadExtraStudentFileList();
        }

        for (Path extraStudentFile : extraStudentFiles) {
            if (extraStudentFile.toAbsolutePath().equals(path.toAbsolutePath())) {
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
            parseExtraStudentFiles(configFile);
        }
    }

    private void parseExtraStudentFiles(Path path) {
        String fileContents = initFileContents(path.toAbsolutePath().toFile());
        Yaml yaml = new Yaml();
        Object yamlSpecifications = yaml.load(fileContents);

        if (!(yamlSpecifications instanceof Map)) {
            return;
        }

        Map<?, ?> specsAsMap = (Map<?, ?>) yamlSpecifications;
        Object files = specsAsMap.get("extra_student_files");
        addFiles(files);
    }

    private void addFiles(Object files) {
        addAllIfList(files);
        addIfString(files);
    }

    private void addAllIfList(Object files) {
        if (files instanceof List) {
            for (Object value : (List<?>) files) {
                addIfString(value);
            }
        }
    }

    private void addIfString(Object value) {
        if (value instanceof String) {
            Path path = this.rootPath.resolve((String) value);
            extraStudentFiles.add(path);
        }
    }

    private String initFileContents(File file) {
        try {
            return FileUtils.readFileToString(file);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
