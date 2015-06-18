package fi.helsinki.cs.tmc.langs.sandbox;

import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An abstract {@link FileMovingPolicy} that also uses
 * <a href="http://tmc.mooc.fi/usermanual/pages/instructors.html#_tmcproject_yml">
 * ExtraStudentFiles</a> to make a decision on whether to move a file or not.
 *
 * <p>For any {@link FileMovingPolicy} that extends this class, a file is moved if either
 * {@link ExtraStudentFileAwareFileMovingPolicy#isExtraStudentFile(Path)} or
 * {@link ExtraStudentFileAwareFileMovingPolicy#shouldMoveFile(Path)} returns {@code True}.
 */
public abstract class ExtraStudentFileAwareFileMovingPolicy implements FileMovingPolicy {

    private static final String TMC_PROJECT_YML = ".tmcproject.yml";

    private List<Path> extraStudentFiles;
    private Path rootPath;
    private Path target;

    /**
     * Determines whether a file should be moved even if it is not an <tt>ExtraStudentFile</tt>.
     */
    public abstract boolean shouldMoveFile(Path path);
    
    @Override
    public boolean shouldMove(Path path, Path rootPath, Path target) {
        if (!path.toFile().exists()) {
            return false;
        }

        if (path.toFile().isDirectory()) {
            return false;
        }

        if (path.toString().endsWith(TMC_PROJECT_YML)) {
            return false;
        }

        this.rootPath = rootPath;
        this.target = target;
        
        return isExtraStudentFile(path) || shouldMoveFile(path);
    }

    /**
     * Determines whether a file is an <tt>ExtraStudentFile</tt> and therefore should be always
     * moved.
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

        File tmcprojectyml = this.target.toAbsolutePath().resolve(TMC_PROJECT_YML).toFile();

        if (tmcprojectyml.exists()) {
            parseExtraStudentFiles(tmcprojectyml);
        }
    }

    private void parseExtraStudentFiles(File file) {
        String fileContents = initFileContents(file);
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
            String path = this.rootPath.toAbsolutePath() + (String) value;
            extraStudentFiles.add(Paths.get(path));
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
