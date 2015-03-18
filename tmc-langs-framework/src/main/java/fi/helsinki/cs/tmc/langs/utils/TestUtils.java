package fi.helsinki.cs.tmc.langs.utils;

import com.google.common.base.Throwables;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class TestUtils {
    public static Path getPath(Class clazz, String location) {
        Path path;
        try {
            path = Paths.get(clazz.getResource("/" + location).toURI());
        } catch (URISyntaxException e) {
            throw Throwables.propagate(e);
        }
        return path;
    }
}
