
package fi.helsinki.cs.tmc.langs.testrunner;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

@Ignore
public class TestRunnerTestSubject {
    @Test
    public void successfulTestCase() {
    }

    @Test
    public void failingTestCase() {
        fail("too bad"); // This must be on line 16 of the source file, or the test must be updated.
    }
}

