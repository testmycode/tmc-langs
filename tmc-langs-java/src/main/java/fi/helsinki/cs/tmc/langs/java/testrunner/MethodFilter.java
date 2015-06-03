package fi.helsinki.cs.tmc.langs.java.testrunner;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

public class MethodFilter extends Filter {

    private final String methodName;

    public MethodFilter(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public String describe() {
        return "Filters tests based on method name";
    }

    @Override
    public boolean shouldRun(Description description) {
        return description.getMethodName().equals(this.methodName);
    }
}
