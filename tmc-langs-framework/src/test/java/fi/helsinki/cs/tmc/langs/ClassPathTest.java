
package fi.helsinki.cs.tmc.langs;

import fi.helsinki.cs.tmc.langs.utils.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class ClassPathTest {

    private ClassPath cp;

    @Before
    public void setUp() {
        cp = new ClassPath(TestUtils.getPath(getClass(), "ant_project"));
    }

    @Test
    public void testConstructor() {
        assertTrue("ClassPath didn't contain path that was passed in constructor", cp.toString().contains("ant_project"));
        assertEquals("Wrong amount of subpaths ", 1, cp.getPaths().size());
    }

    @Test
    public void testConstructorWithTwoPaths() {
        cp = new ClassPath(TestUtils.getPath(getClass(), "ant_project"), TestUtils.getPath(getClass(), "arith_funcs"));
        assertEquals("There should be 2 subpaths", 2, cp.getPaths().size());
    }

    @Test
    public void testContructorWithoutPaths() {
        cp = new ClassPath();
        assertTrue("Subpaths should be empty", cp.getPaths().isEmpty());
    }

    @Test
    public void testAddingPath() {
        cp.add(TestUtils.getPath(getClass(), "arith_funcs"));
        assertTrue("ClassPath didn't contain path that was added", cp.toString().contains("arith_funcs"));
        assertEquals("Wrong amount of subpaths ", 2, cp.getPaths().size());
    }

    @Test
    public void testAddingSamePathDoesntAddItTwice() {
        cp.add(TestUtils.getPath(getClass(), "arith_funcs"));
        cp.add(TestUtils.getPath(getClass(), "arith_funcs"));
        assertTrue("ClassPath didn't contain path that was added", cp.toString().contains("arith_funcs"));
        assertEquals("Wrong amount of subpaths ", 2, cp.getPaths().size());
    }

    @Test
    public void testAddingAnotherClassPathAddsNewSubpaths() {
        ClassPath classPath = new ClassPath(TestUtils.getPath(getClass(), "arith_funcs"));
        assertFalse(cp.toString().contains("arith_funcs"));
        cp.add(classPath);
        assertTrue("ClassPath didn't contain path that was added", cp.toString().contains("arith_funcs"));
        assertEquals("Wrong amount of subpaths ", 2, cp.getPaths().size());
    }

    @Test
    public void testAddDirAndContents() {
        cp.addDirAndContents(TestUtils.getPath(getClass(), "arith_funcs" + File.separatorChar + "lib"));
        assertTrue("Base path didn't get added to the ClassPath", cp.toString().contains("lib" + File.pathSeparatorChar));
        assertTrue(".jars should be added to the ClassPath", cp.toString().contains(".jar"));
        assertEquals("There should be 5 subpaths", 5, cp.getPaths().size());
    }

    @Test
    public void testNonDirectoryPathPassedToDirAndSubdirs() {
        cp.addDirAndContents(TestUtils.getPath(getClass(), "ant_project" + File.separatorChar + "build.xml"));
        assertEquals("Subpaths size shouldn't change", 1, cp.getPaths().size());
    }
}
