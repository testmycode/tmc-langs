package fi.helsinki.cs.tmc.langs.make;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class MakeFileMovingPolicyTest {

    private MakeFileMovingPolicy makeFileMovingPolicy;

    public MakeFileMovingPolicyTest() {
        makeFileMovingPolicy = new MakeFileMovingPolicy();
    }

    @Test
    public void testItDoesNotMoveMakefiles() {
        Path makefile = Paths.get("Makefile");
        assertFalse(makeFileMovingPolicy.shouldMoveFile(makefile));
    }

    @Test
    public void testItMovesFilesInSrc() throws IOException {
        final Path path = TestUtils.getPath(getClass(), "passing");
        final List<String> toBeMoved = new ArrayList<>();

        collectPaths(path, toBeMoved);

        assertEquals(3, toBeMoved.size());
        // Should not move Makefile in source
        assertFalse(toBeMoved.contains("src" + File.separatorChar + "Makefile"));
        assertTrue(toBeMoved.contains("src" + File.separatorChar + "main.c"));
        assertTrue(toBeMoved.contains("src" + File.separatorChar + "source.c"));
        assertTrue(toBeMoved.contains("src" + File.separatorChar + "source.h"));
    }

    @Test
    public void testItDoesNotMoveFilesInTest() throws IOException {
        final Path path = TestUtils.getPath(getClass(), "passing");
        final List<String> toBeMoved = new ArrayList<>();

        collectPaths(path, toBeMoved);

        assertEquals(3, toBeMoved.size());
        assertFalse(toBeMoved.contains("test" + File.separatorChar + "test_source.c"));
        assertFalse(toBeMoved.contains("test" + File.separatorChar + "tmc-check.h"));
        assertFalse(toBeMoved.contains("test" + File.separatorChar + "tmc-check.c"));
        assertFalse(toBeMoved.contains("test" + File.separatorChar + "Makefile"));
        assertFalse(toBeMoved.contains("test" + File.separatorChar + "checkhelp.c"));
    }

    private void collectPaths(final Path path, final List<String> toBeMoved)
            throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                if (makeFileMovingPolicy.shouldMoveFile(path.relativize(file))) {
                    toBeMoved.add(path.relativize(file).toString());
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exception)
                    throws IOException {
                if (exception == null) {
                    return FileVisitResult.CONTINUE;
                } else {
                    // directory iteration failed
                    throw exception;
                }
            }
        });
    }


}