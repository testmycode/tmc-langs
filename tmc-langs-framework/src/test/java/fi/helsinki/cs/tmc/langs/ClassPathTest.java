
package fi.helsinki.cs.tmc.langs;

import com.google.common.base.Throwables;
import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class ClassPathTest {
    
    private ClassPath cp;
    
    @Before
    public void setUp() {
        cp = new ClassPath(getPath("ant_project"));
    }
    
    @Test
    public void classPathConstructorWorks() {
        assertTrue("ClassPath didn\'t contain path that was passed in constructor", cp.toString().contains("ant_project"));
        assertEquals("Wrong amount of subpaths ", 1, cp.getPaths().size());
    }
    
    @Test
    public void addingPathAddsPathToSubpaths() {
        cp.add(getPath("arith_funcs"));
        assertTrue("ClassPath didn\'t contain path that was added", cp.toString().contains("arith_funcs"));
        assertEquals("Wrong amount of subpaths ", 2, cp.getPaths().size());
    }
    
    @Test
    public void addingSamePathDoesntAddItTwice() {
        cp.add(getPath("arith_funcs"));
        cp.add(getPath("arith_funcs"));
        assertTrue("ClassPath didn\'t contain path that was added", cp.toString().contains("arith_funcs"));
        assertEquals("Wrong amount of subpaths ", 2, cp.getPaths().size());
    }
    
    @Test
    public void addingAnotherClassPathAddsNewSubpaths() {
        ClassPath classPath = new ClassPath(getPath("arith_funcs"));
        assertFalse(cp.toString().contains("arith_funcs"));
        cp.add(classPath);
        assertTrue("ClassPath didn\'t contain path that was added", cp.toString().contains("arith_funcs"));
        assertEquals("Wrong amount of subpaths ", 2, cp.getPaths().size());
    }

    @Test
    public void addDirAndSubDirAddsDirAndSubDirsToPaths() {
        cp.addDirAndContents(getPath("arith_funcs" + File.separatorChar + "lib"));
        System.out.println(cp);
        assertTrue("ClassPath didn\'t contain edu-test-utils dir", cp.toString().contains("edu-test-utils"));
        assertTrue("Base path didn\'t get added to the ClassPath", cp.toString().contains("lib:"));
    }

    @Test
    public void nonDirectoryPathPassedToDirAndSubdirs() {
        cp.addDirAndContents(getPath("ant_project" + File.separatorChar + "build.xml"));
        assertEquals("Subpaths size shouldn\'t change", 1, cp.getPaths().size());
    }

    private Path getPath(String location) {
        Path path;
        try {
            path = Paths.get(getClass().getResource(File.separatorChar + location).toURI());
        } catch (URISyntaxException e) {
            throw Throwables.propagate(e);
        }
        return path;
    }
}
