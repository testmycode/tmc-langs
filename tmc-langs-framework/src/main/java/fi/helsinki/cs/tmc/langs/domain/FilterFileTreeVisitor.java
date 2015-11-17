package fi.helsinki.cs.tmc.langs.domain;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class FilterFileTreeVisitor {

    private Path repoPath;
    private Path exercisePath;

    private List<DirectorySkipper> skippers = new ArrayList<>();

    private Filer filer;

    public FilterFileTreeVisitor addSkipper(DirectorySkipper skipper) {
        skippers.add(skipper);
        return this;
    }

    public FilterFileTreeVisitor setClonePath(Path repoPath) {
        this.repoPath = repoPath;
        return this;
    }

    public FilterFileTreeVisitor setExercisePath(Path exercisePath) {
        this.exercisePath = exercisePath;
        return this;
    }

    public FilterFileTreeVisitor setFiler(Filer filer) {
        this.filer = filer;
        return this;
    }

    private boolean skipDirectory(Path dirPath) {
        for (DirectorySkipper skipper : skippers) {
            if (skipper.skipDirectory(dirPath)) {
                return true;
            }
        }
        return false;
    }

    public void traverse() {
        System.out.println(repoPath);
        try {
            Files.walkFileTree(
                    repoPath,
                    new FileVisitor<Path>() {

                        @Override
                        public FileVisitResult preVisitDirectory(
                                Path dir, BasicFileAttributes attrs) throws IOException {
                            if (skipDirectory(dir)) {
                                return FileVisitResult.SKIP_SUBTREE;
                            }

                            return filer.decideOnDirectory(dir);
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                throws IOException {
                            filer.maybeCopyAndFilterFile(file, repoPath, exercisePath);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc)
                                throws IOException {
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                                throws IOException {
                            return FileVisitResult.CONTINUE;
                        }
                    });
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
