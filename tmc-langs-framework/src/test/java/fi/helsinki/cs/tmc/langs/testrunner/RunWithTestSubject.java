package fi.helsinki.cs.tmc.langs.testrunner;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@Ignore
@RunWith(MockRunner.class)
public class RunWithTestSubject {

    @Test
    public void testCase() {
        assertTrue(true);
    }
}
