package fi.helsinki.cs.tmc.langs.sandbox;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

    private List<Path> extraStudentFiles;
    private Path rootPath;
    private Path target;

    /**
     * Determines whether a file should be moved even if it is not an <tt>ExtraStudentFile</tt>.
     */
    protected abstract boolean shouldMoveFile(Path path);
    
    @Override
    public boolean shouldMove(Path path, Path rootPath, Path target) {
        if (!path.toFile().exists()) {
            return false;
        }

        if (path.toFile().isDirectory()) {
            return false;
        }

        if (path.toString().endsWith(".tmcproject.yml")) {
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

        File tmcprojectyml = new File(this.target.toAbsolutePath().toString()
                + File.separatorChar + ".tmcproject.yml");
        Scanner scanner = initFileScanner(tmcprojectyml);

        if (scanner == null) {
            return;
        }

        parseExtraStudentFiles(scanner);
    }

    private void parseExtraStudentFiles(Scanner scanner) {
        boolean parseFiles = false;
        while (scanner.hasNextLine()) {
            String row = scanner.nextLine();

            if (row.startsWith("extra_student_files:")) {
                parseFiles = true;
            }

            if (!parseFiles) {
                continue;
            }

            if (row.startsWith("  - ")) {
                String relativePath = row.substring(4);
                extraStudentFiles.add(Paths.get(this.rootPath.toAbsolutePath() + relativePath));
            } else {
                parseFiles = false;
            }
        }
    }

    private Scanner initFileScanner(File file) {
        Scanner scanner;

        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        return scanner;
    }
}
