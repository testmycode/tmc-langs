
package fi.helsinki.cs.tmc.langs;

import com.google.common.base.Throwables;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for generating ClassPath.
 */
public class ClassPath {

    private List<Path> subPaths = new ArrayList<>();
    
    public ClassPath(Path path) {
        subPaths.add(path);
    }
    
    public void add(Path path) {
        if (!subPaths.contains(path)) {
           subPaths.add(path);
        }
    }
    
    public void add(ClassPath path) {
        for (Path subPath : path.subPaths) {
            add(subPath);
        }
    }
    
    public List<Path> getPaths() {
        return subPaths;
    }

    /**
     * Crawl through directories and add every directory to the subPaths.
     *
     * @param basePath Directory where to begin the search.
     */
    public void addDirAndSubdirs(Path basePath) {
        if (!basePath.toFile().isDirectory()) {
            return;
        }

        add(basePath);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(basePath, dirFilter)) {
            for (Path path : stream) {
                addDirAndSubdirs(path);
            }
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
    
    private DirectoryStream.Filter<Path> dirFilter = new DirectoryStream.Filter<Path>() {
        @Override
        public boolean accept(Path entry) {
            return Files.isDirectory(entry);
        }
    };
    
    @Override
    public String toString() {
        String classPath = subPaths.get(0).toString();
        
        for (int i = 1; i < subPaths.size(); i++) {
            classPath += File.pathSeparatorChar + subPaths.get(i).toString();
        }
        
        return classPath;
    }
}
