package fi.helsinki.cs.tmc.langs.rust;

import static org.junit.Assert.*;

import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;


public class CargoStudentFilePolicyTest {
    private CargoStudentFilePolicy policy;
    
    @Before
    public void setUp() {
        policy = new CargoStudentFilePolicy(Paths.get(""));
    }

    @Test
    public void testCargoTomlIsNotStudentFile() {
        assertFalse(policy.isStudentSourceFile(Paths.get("src/Cargo.toml")));
    }
    
    @Test
    public void testSourceFileIsSourceFile() {
        assertTrue(policy.isStudentSourceFile(Paths.get("src/hello.rs")));
    }
}
