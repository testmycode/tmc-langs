package fi.helsinki.cs.tmc.langs.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;

public class ClassPathTest {

    private ClassPath cp;

    @Before
    public void setUp() {
        cp = new ClassPath(TestUtils.getPath(getClass(), "ant_project"));
    }

    @Test
    public void testConstructor() {
        // TODO: don't rely on tostring representation with the tests
        assertTrue(
                "ClassPath didn't contain path that was passed in constructor",
                cp.toString().contains("ant_project"));
        assertEquals("Wrong amount of subpaths ", 1, cp.getPaths().size());
    }

    @Test
    public void testConstructorWithTwoPaths() {
        Path project1 = TestUtils.getPath(getClass(), "ant_project");
        Path project2 = TestUtils.getPath(getClass(), "ant_arith_funcs");
        cp = new ClassPath(project1, project2);
        assertEquals("There should be 2 subpaths", 2, cp.getPaths().size());
    }

    @Test
    public void testContructorWithoutPaths() {
        cp = new ClassPath();
        assertTrue("Subpaths should be empty", cp.getPaths().isEmpty());
    }

    @Test
    public void testAddingPath() {
        cp.add(TestUtils.getPath(getClass(), "ant_arith_funcs"));
        assertTrue(
                "ClassPath didn't contain path that was added",
                cp.toString().contains("ant_arith_funcs"));
        assertEquals("Wrong amount of subpaths ", 2, cp.getPaths().size());
    }

    @Test
    public void testAddingSamePathDoesntAddItTwice() {
        cp.add(TestUtils.getPath(getClass(), "ant_arith_funcs"));
        cp.add(TestUtils.getPath(getClass(), "ant_arith_funcs"));
        assertTrue(
                "ClassPath didn't contain path that was added",
                cp.toString().contains("arith_funcs"));
        assertEquals("Wrong amount of subpaths ", 2, cp.getPaths().size());
    }

    @Test
    public void testAddingAnotherClassPathAddsNewSubpaths() {
        ClassPath classPath = new ClassPath(TestUtils.getPath(getClass(), "ant_arith_funcs"));
        assertFalse(cp.toString().contains("ant_arith_funcs"));
        cp.add(classPath);
        assertTrue(
                "ClassPath didn't contain path that was added",
                cp.toString().contains("ant_arith_funcs"));
        assertEquals("Wrong amount of subpaths ", 2, cp.getPaths().size());
    }

    @Test
    public void testAddDirAndContents() {
        Path path = TestUtils.getPath(getClass(), "ant_arith_funcs" + File.separatorChar + "lib");
        cp.addDirAndContents(path);
        assertTrue(
                "Base path didn't get added to the ClassPath",
                cp.toString().contains("lib" + File.pathSeparatorChar));
        assertTrue(".jars should be added to the ClassPath", cp.toString().contains(".jar"));
        assertTrue(
                "There should be 4 or 6 subpaths now it was: "
                        + cp.getPaths().size()
                        + " - "
                        + cp.getPaths(),
                cp.getPaths().size() == 4
                        || cp.getPaths().size() == 6
                        || cp.getPaths().size() == 9);
    }

    @Test
    public void testNonDirectoryPathPassedToDirAndSubdirs() {
        Path path = TestUtils.getPath(getClass(), "ant_project" + File.separatorChar + "build.xml");
        cp.addDirAndContents(path);
        assertEquals("Subpaths size shouldn't change", 1, cp.getPaths().size());
    }

    @Test
    public void toStringReturnsEmptyStringWhenClassPathContainsNoSubPaths() {
        ClassPath classPath = new ClassPath();
        assertEquals("", classPath.toString());
    }
}
