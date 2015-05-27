package fi.helsinki.cs.tmc.langs.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class SourceFilesTest {

    private SourceFiles sf;

    @Before
    public void setUp() {
        sf = new SourceFiles();
    }

    @Test
    public void testConstructorWithoutPaths() {
        assertTrue("SourceFiles should be empty with empty constructor", sf.isEmpty());
    }

    @Test
    public void testConstructorWithPath() {
        sf = new SourceFiles(TestUtils.getPath(getClass(), "arith_funcs"));
        assertFalse("SourceFiles should not be empty", sf.isEmpty());
        List<File> files = sf.getSources();
        assertEquals(3, files.size());
        assertContains(files, "Arith.java");
    }

    @Test
    public void testAddSource() {
        addSource("arith_funcs");
        assertFalse("SourceFiles should not be empty", sf.isEmpty());
        assertContains(sf.getSources(), "Arith.java");
    }

    @Test
    public void testClearSources() {
        addSource("arith_funcs");
        assertFalse("SourceFiles should not be empty before calling clearSources", sf.isEmpty());
        sf.clearSources();
        assertTrue("SourceFiles should be empty after calling clearSources", sf.isEmpty());
    }

    private void addSource(String path) {
        sf.addSource(TestUtils.getPath(getClass(), path).toFile());
    }

    private void assertContains(List<File> files, String fileName) {
        boolean contains = false;
        for (File file : files) {
            if (file.getName().contains(fileName)) {
                contains = true;
            }
        }

        assertTrue("SourceFiles didn't contain " + fileName, contains);
    }
}
