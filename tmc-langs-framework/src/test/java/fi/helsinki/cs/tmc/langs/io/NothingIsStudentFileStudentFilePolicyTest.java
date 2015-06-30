package fi.helsinki.cs.tmc.langs.io;

import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;

public class NothingIsStudentFileStudentFilePolicyTest {

    private StudentFilePolicy policy;

    @Before
    public void setUp() {
        policy = new NothingIsStudentFileStudentFilePolicy();
    }

    @Test
    public void returnsTrueForPath() {
        assertFalse(policy.isStudentFile(Paths.get(""), Paths.get("")));
    }

    @Test
    public void returnsTrueForNull() {
        assertFalse(policy.isStudentFile(null, null));
    }
}
