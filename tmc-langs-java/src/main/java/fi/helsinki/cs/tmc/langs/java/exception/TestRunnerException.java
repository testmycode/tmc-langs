package fi.helsinki.cs.tmc.langs.java.exception;

/**
 * Thrown when a exception occurs when running the TestRunner.
 */
public final class TestRunnerException extends Exception {

    /**
     * Create a new TestRunnerException.
     */
    public TestRunnerException() {
        super();
    }

    /**
     * Create a new TestRunnerException based on an existing Throwable.
     */
    public TestRunnerException(Throwable throwable) {
        super(throwable);
    }
}
