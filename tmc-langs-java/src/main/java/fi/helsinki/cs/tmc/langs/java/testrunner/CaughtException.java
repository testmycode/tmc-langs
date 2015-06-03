package fi.helsinki.cs.tmc.langs.java.testrunner;

/**
 * Serializable form of an exception.
 */
public class CaughtException implements Cloneable {

    public String className; // Fully qualified, not null
    public String message; // May be null
    public StackTraceElement[] stackTrace;
    public CaughtException cause; // May be null

    public CaughtException() {
    }

    public CaughtException(Throwable ex) {
        this.className = ex.getClass().getName();
        this.message = ex.getMessage();
        this.stackTrace = ex.getStackTrace();
        if (ex.getCause() != null) {
            this.cause = new CaughtException(ex.getCause());
        }
    }

    @Override
    public CaughtException clone() {
        CaughtException clone = new CaughtException();
        clone.className = this.className;
        clone.message = this.message;
        clone.stackTrace = this.stackTrace.clone();
        clone.cause = this.cause.clone();
        return clone;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(className);
        if (message != null) {
            sb.append(": ").append(message);
        }
        sb.append("\n");
        for (StackTraceElement ste : stackTrace) {
            sb.append(ste.getFileName());
            sb.append(":");
            sb.append(ste.getLineNumber());
            sb.append(": ");
            sb.append(ste.getClassName());
            sb.append(".");
            sb.append(ste.getMethodName());
            sb.append("\n");
        }
        if (cause != null) {
            sb.append("Caused by ");
            sb.append(cause.toString());
        }
        return sb.toString();
    }
}
