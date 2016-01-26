package fi.helsinki.cs.tmc.langs.domain;

import static org.junit.Assert.assertEquals;

import com.google.common.io.Files;

import org.apache.commons.io.FileUtils;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FilerTest {

    @Test
    public void testDecideOnDirectory() throws IOException {
        Path path = Paths.get("src", "test", "resources", "arith_funcs_solution_file");
        Path temp = createTemporaryCopyOf(path);
        temp.toFile().deleteOnExit();
        Path toPath = Files.createTempDir().toPath();
        Filer filer = new Filer().setToPath(toPath);
        assertEquals(FileVisitResult.CONTINUE, filer.decideOnDirectory(temp));
    }

    private Path createTemporaryCopyOf(Path path) throws IOException {
        File tempFolder = Files.createTempDir();
        FileUtils.copyDirectory(path.toFile(), tempFolder);
        tempFolder.deleteOnExit();
        return tempFolder.toPath();
    }
}
