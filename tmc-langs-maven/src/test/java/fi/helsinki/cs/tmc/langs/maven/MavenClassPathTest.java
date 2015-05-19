package fi.helsinki.cs.tmc.langs.maven;

import fi.helsinki.cs.tmc.langs.ClassPath;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.maven.shared.invoker.MavenInvocationException;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class MavenClassPathTest {
    
    @Test
    public void addsAllDependeciesToClassPath() throws IOException, MavenInvocationException {
        Path path = TestUtils.getPath(getClass(), "maven_exercise");
        ClassPath classPath = MavenClassPathBuilder.fromProjectBasePath(path);
        
        assertTrue(classPath.toString().contains("junit-4.10.jar"));
        assertTrue(classPath.toString().contains("hamcrest-core-1.1.jar"));
        assertTrue(classPath.toString().contains("edu-test-utils-0.4.1.jar"));
    }
}
