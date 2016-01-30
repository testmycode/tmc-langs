package fi.helsinki.cs.tmc.langs.domain;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class FilterFileTreeVisitor {

    private Path repoPath;
    private Path startPath;

    private List<DirectorySkipper> skippers = Lists.newArrayList();

    private Filer filer;

    public FilterFileTreeVisitor addSkipper(DirectorySkipper skipper) {
        skippers.add(skipper);
        return this;
    }

    public FilterFileTreeVisitor setStartPath(Path startPath) {
        this.startPath = startPath;
        return this;
    }

    public FilterFileTreeVisitor setClonePath(Path repoPath) {
        this.repoPath = repoPath;
        return this;
    }

    public FilterFileTreeVisitor setFiler(Filer filer) {
        this.filer = filer;
        return this;
    }

    private boolean shouldSkipDirectory(Path dirPath) {
        for (DirectorySkipper skipper : skippers) {
            if (skipper.skipDirectory(dirPath)) {
                return true;
            }
        }
        return false;
    }

    public void traverse() {
        try {
            Files.walkFileTree(
                    startPath,
                    new FileVisitor<Path>() {

                        @Override
                        public FileVisitResult preVisitDirectory(
                                Path dir, BasicFileAttributes attrs) throws IOException {
                            if (shouldSkipDirectory(dir)) {
                                return FileVisitResult.SKIP_SUBTREE;
                            }

                            return filer.decideOnDirectory(dir);
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            Path relativePath =
                                    file.subpath(repoPath.getNameCount(), file.getNameCount());
                            filer.visitFile(file, relativePath);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc)
                                throws IOException {
                            if (exc != null) {
                                throw exc;
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                                throws IOException {
                            if (exc != null) {
                                throw exc;
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
