package fi.helsinki.cs.tmc.langs.testrunner;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class MockRunner extends BlockJUnit4ClassRunner {

    public final Class<?> testClass;
    public static boolean runCalled = false;

    public static void reset() {
        runCalled = false;
    }

    public MockRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        this.testClass = testClass;
    }

    @Override
    public void run(RunNotifier notifier) {
        runCalled = true;
    }
}
