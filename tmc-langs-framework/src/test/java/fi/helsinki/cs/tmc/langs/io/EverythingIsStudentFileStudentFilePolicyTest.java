package fi.helsinki.cs.tmc.langs.io;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;

public class EverythingIsStudentFileStudentFilePolicyTest {

    private StudentFilePolicy policy;

    @Before
    public void setUp() {
        policy = new EverythingIsStudentFileStudentFilePolicy();
    }

    @Test
    public void returnsTrueForPath() {
        assertTrue(policy.isStudentFile(Paths.get(""), Paths.get("")));
    }

    @Test
    public void returnsTrueForNull() {
        assertTrue(policy.isStudentFile(null, null));
    }
}
