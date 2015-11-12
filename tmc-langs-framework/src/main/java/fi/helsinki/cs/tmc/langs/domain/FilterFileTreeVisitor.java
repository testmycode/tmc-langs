package fi.helsinki.cs.tmc.langs.domain;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

final class FilterFileTreeVisitor {

    private Path clonePath, destPath;
    private List<DirectorySkipper> skippers = new ArrayList<>();
    private Filer filer;

    FilterFileTreeVisitor addSkipper(DirectorySkipper skipper) {
        skippers.add(skipper);
        return this;
    }

    FilterFileTreeVisitor setClonePath(Path clonePath) {
        this.clonePath = clonePath;
        return this;
    }

    FilterFileTreeVisitor setDestPath(Path destPath) {
        this.destPath = destPath;
        return this;
    }

    FilterFileTreeVisitor setFiler(Filer filer) {
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

    void traverse() {
        try {
            Files.walkFileTree(
                    clonePath,
                    new FileVisitor<Path>() {

                        @Override
                        public FileVisitResult preVisitDirectory(
                                Path dir, BasicFileAttributes attrs) throws IOException {
                            if (skipDirectory(dir)) {
                                return FileVisitResult.SKIP_SUBTREE;
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                throws IOException {
                            filer.maybeCopyAndFilterFile(file, clonePath, destPath);
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
