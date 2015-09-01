package fi.helsinki.cs.tmc.langs.java.maven;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.java.ClassPath;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;

public class MavenClassPathBuilderTest {

    @Test
    public void canConstruct() {
        MavenClassPathBuilder builder = new MavenClassPathBuilder();
        assertNotNull(builder);
    }

    @Test
    public void addsAllDependenciesToClassPath() throws IOException {
        Path path = TestUtils.getPath(getClass(), "maven_exercise");
        ClassPath classPath = MavenClassPathBuilder.fromProjectBasePath(path);

        assertTrue(classPath.toString().contains("junit-4.10.jar"));
        assertTrue(classPath.toString().contains("hamcrest-core-1.1.jar"));
        assertTrue(classPath.toString().contains("edu-test-utils-0.4.1.jar"));
    }

    @Test(expected = IOException.class)
    public void throwsIoExceptionOnFailure() throws IOException {
        Path path = TestUtils.getPath(getClass(), "non_ant_project");
        ClassPath classPath = MavenClassPathBuilder.fromProjectBasePath(path);
    }
}
