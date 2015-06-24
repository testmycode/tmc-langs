package fi.helsinki.cs.tmc.langs;

import org.junit.runner.notification.Failure;

public class TestCase {

    public enum Status {

        PASSED, FAILED, RUNNING, NOT_STARTED
    }

    public final String className;
    public final String methodName;
    public final String[] pointNames;
    public String message;
    public CaughtException exception;
    public Status status;

    /**
     * Creates a new TestCase with given parameters.
     *
     * @param className     Test class' name
     * @param methodName    Test method's name
     * @param pointNames    List of point names associated with this method
     */
    public TestCase(String className, String methodName, String[] pointNames) {
        this.methodName = methodName;
        this.className = className;
        this.status = Status.NOT_STARTED;
        this.pointNames = pointNames;
        this.message = null;
        this.exception = null;
    }

    /**
     * Creates a new TestCase based on the provided TestCase.
     *
     * @param testCase TestCase to copy values from
     */
    public TestCase(TestCase testCase) {
        this.methodName = testCase.methodName;
        this.className = testCase.className;
        this.message = testCase.message;
        this.status = testCase.status;
        this.pointNames = testCase.pointNames.clone();
        this.exception = testCase.exception.clone();
    }

    /**
     * Marks the test case as running.
     */
    public void testStarted() {
        this.status = Status.RUNNING;
    }

    /**
     * Marks a test as finished.
     *
     * <p>
     * If the test status was previously non-failed, the test is interpreted
     * as having passed.
     */
    public void testFinished() {
        if (this.status != Status.FAILED) {
            this.status = Status.PASSED;
        }
    }

    /**
     * Mark the test as failed.
     *
     * @param failure The Failure that caused the test to fail
     */
    public void testFailed(Failure failure) {
        this.message = failureMessage(failure);
        this.status = Status.FAILED;

        Throwable ex = failure.getException();
        if (ex != null) {
            this.exception = new CaughtException(ex);
        }
    }

    private String failureMessage(Failure failure) {
        if (failure.getException() == null) { // Not sure if this is possible
            return null;
        }

        String exceptionClass = failure.getException().getClass().getSimpleName();
        String exMsg = failure.getException().getMessage();
        if (exceptionClass.equals("AssertionError")) {
            if (exMsg != null) {
                return exMsg;
            } else {
                return exceptionClass;
            }
        } else {
            if (exMsg != null) {
                return exceptionClass + ": " + exMsg;
            } else {
                return exceptionClass;
            }
        }
    }

    @Override
    public String toString() {
        String ret = this.methodName + " (" + this.className + ") " + status;
        if (this.message != null) {
            ret += ": " + this.message;
        }
        if (this.exception != null) {
            ret += "\n" + this.exception.toString();
        }
        return ret;
    }
}
