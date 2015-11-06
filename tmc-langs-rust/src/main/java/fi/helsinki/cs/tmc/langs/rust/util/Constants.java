package fi.helsinki.cs.tmc.langs.rust.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Constants {

    public static final Path CARGO_TOML = Paths.get("Cargo.toml");
    public static final Path SOURCE = Paths.get("src");
    public static final Path TESTS = Paths.get("tests", "mod.rs");
    public static final Path POINTS = Paths.get("tmc-points.txt");
}
