package fi.helsinki.cs.tmc.langs.java.testrunner;

import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class TestRunnerTestSubject {
    @Test
    public void successfulTestCase() {}

    @Test
    public void failingTestCase() {
        fail("too bad"); // This must be on line 17 of the source file, or the test must be updated.
    }
}
