package fi.helsinki.cs.tmc.langs.testrunner;

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

    public TestCase(String className, String methodName, String[] pointNames) {
        this.methodName = methodName;
        this.className = className;
        this.status = Status.NOT_STARTED;
        this.pointNames = pointNames;
        this.message = null;
        this.exception = null;
    }

    public TestCase(TestCase aTestCase) {
        this.methodName = aTestCase.methodName;
        this.className = aTestCase.className;
        this.message = aTestCase.message;
        this.status = aTestCase.status;
        this.pointNames = aTestCase.pointNames.clone();
        this.exception = aTestCase.exception.clone();
    }

    public void testStarted() {
        this.status = Status.RUNNING;
    }

    public void testFinished() {
        if (this.status != Status.FAILED) {
            this.status = Status.PASSED;
        }
    }

    public void testFailed(Failure f) {
        this.message = failureMessage(f);
        this.status = Status.FAILED;

        Throwable ex = f.getException();
        if (ex != null) {
            this.exception = new CaughtException(ex);
        }
    }

    private String failureMessage(Failure f) {
        if (f.getException() == null) { // Not sure if this is possible
            return null;
        }

        String exceptionClass = f.getException().getClass().getSimpleName();
        String exMsg = f.getException().getMessage();
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
