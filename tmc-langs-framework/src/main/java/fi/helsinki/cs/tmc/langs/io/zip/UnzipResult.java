package fi.helsinki.cs.tmc.langs.io.zip;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class UnzipResult {
    /**
     * The project directory to which we extracted.
     */
    public Path projectDir;

    /**
     * Files that were in the zip but did not exist before.
     * In the usual case of downloading a new project, all files go here.
     */
    public List<Path> newFiles = new ArrayList<>();

    /**
     * Files overwritten as permitted by the given {@code OverwritingDecider}.
     */
    public List<Path> overwrittenFiles = new ArrayList<>();

    /**
     * Files skipped because the given {@code OverwritingDecider} didn't allow overwriting.
     */
    public List<Path> skippedFiles = new ArrayList<>();

    /**
     * Files that existed before but were the same in the zip.
     */
    public List<Path> unchangedFiles = new ArrayList<>();

    /**
     * Files that were deleted because they weren't in the zip.
     */
    public List<Path> deletedFiles = new ArrayList<>();

    /**
     * Files skipped because the given {@code OverwritingDecider} didn't allow deleting.
     */
    public List<Path> skippedDeletingFiles = new ArrayList<>();

    UnzipResult(Path projectDir) {
        this.projectDir = projectDir;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Project: \"").append(projectDir).append("\"\n");
        sb.append("New: ").append(newFiles).append('\n');
        sb.append("Overwritten: ").append(overwrittenFiles).append('\n');
        sb.append("Skipped: ").append(skippedFiles).append('\n');
        sb.append("Unchanged: ").append(unchangedFiles).append('\n');
        sb.append("Deleted: ").append(deletedFiles).append('\n');
        sb.append("Not deleted: ").append(skippedDeletingFiles).append('\n');
        return sb.toString();
    }
}
