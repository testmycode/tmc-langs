package fi.helsinki.cs.tmc.langs.java.maven;

import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.java.ClassPath;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;

public class MavenClassPathTest {

    @Test
    public void addsAllDependeciesToClassPath() throws IOException {
        Path path = TestUtils.getPath(getClass(), "maven_exercise");
        ClassPath classPath = MavenClassPathBuilder.fromProjectBasePath(path);

        assertTrue(classPath.toString().contains("junit-4.10.jar"));
        assertTrue(classPath.toString().contains("hamcrest-core-1.1.jar"));
        assertTrue(classPath.toString().contains("edu-test-utils-0.4.1.jar"));
    }
}
