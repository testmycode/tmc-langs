package fi.helsinki.cs.tmc.langs.io.zip;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class UnzipResultTest {

    @Test
    public void unzipResultToStringContainsExpectedPaths() {
        final List<Path> expected = new ArrayList<>();

        Path projectPath = Paths.get("./test/");
        UnzipResult result = new UnzipResult(projectPath);
        expected.add(projectPath);
        addItemToBoth(Paths.get("test/Toaster.java"), expected, result.newFiles);
        addItemToBoth(Paths.get("test/Overwritten.java"), expected, result.overwrittenFiles);
        addItemToBoth(Paths.get("test/Test.iml"), expected, result.skippedFiles);
        addItemToBoth(Paths.get("test/bar/Foo.java"), expected, result.unchangedFiles);
        addItemToBoth(Paths.get("test/target/Thing.jar"), expected, result.deletedFiles);
        addItemToBoth(Paths.get("test/target/Report.xml"), expected, result.skippedDeletingFiles);

        String resultString = result.toString();

        for (Path path : expected) {
            String pathString = path.toString();
            assertTrue(
                    "Expected UnzipResult to contain path \"" + pathString + "\"",
                    resultString.contains(pathString));
        }
    }

    private static <T> void addItemToBoth(T item, List<T> first, List<T> second) {
        first.add(item);
        second.add(item);
    }
}
