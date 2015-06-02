package fi.helsinki.cs.tmc.langs.make;

import com.google.common.collect.ImmutableList;
import fi.helsinki.cs.tmc.langs.TestResult;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class CTestCase {

    private static final Logger log = Logger.getLogger(CTestCase.class.getName());

    private String name;
    private String result;
    private String message;
    private List<String> points;
    private String valgrindTrace;
    private Exercise.ValgrindStrategy valgrindStrategy;
    private boolean checkedForMemoryLeaks;
    private int maxBytesAllocated = -1;

    public CTestCase(String name, String result, String message, List<String> points, String valgrindTrace, Exercise
            .ValgrindStrategy valgrindStrategy) {
        this(name);
        this.result = result;
        this.message = message;
        this.points = points;
        this.valgrindTrace = valgrindTrace;
        this.valgrindStrategy = valgrindStrategy;
        this.checkedForMemoryLeaks = false;
    }

    public CTestCase(String name, String result, String message, List<String> points, Exercise.ValgrindStrategy
            valgrindStrategy) {
        this(name, result, message, points, null, valgrindStrategy);
    }

    public CTestCase(String name) {
        this.name = name;
    }

    private boolean failedDueToValgrind(String valgrindTrace) {
        if (Exercise.ValgrindStrategy.FAIL == valgrindStrategy) {
            return StringUtils.isNotBlank(valgrindTrace);
        }
        return false;
    }

    public TestResult getTestResult() {
        String msg = message;

        boolean valgrindFailed = failedDueToValgrind(valgrindTrace);
        boolean resultsSuccessful = result.equals("success");
        boolean successful = resultsSuccessful && !valgrindFailed;
        boolean failedOnlyBecauseOfValgrind = resultsSuccessful && valgrindFailed;
        if (failedOnlyBecauseOfValgrind) {
            msg += " - Failed due to errors in valgrind log; see log below. Try submitting to server, some leaks might be platform dependent";
        }

        ArrayList<String> trace = new ArrayList<>();

        if (!successful && valgrindTrace != null) {
            trace.addAll(Arrays.asList(valgrindTrace.split("\\n")));
        }

        return new TestResult(name, successful, ImmutableList.copyOf(points), msg, ImmutableList
                .copyOf(trace));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getPoints() {
        return this.points;
    }

    public void setPoints(List<String> points) {
        this.points = points;
    }

    public String getValgrindTrace() {
        return valgrindTrace;
    }

    public void setValgrindTrace(String valgrindTrace) {
        this.valgrindTrace = valgrindTrace;
    }

    public boolean isCheckedForMemoryLeaks() {
        return checkedForMemoryLeaks;
    }

    public boolean isCheckedForMemoryUsage() {
        return this.maxBytesAllocated >= 0;
    }

    public int getMaxBytesAllocated() {
        return maxBytesAllocated;
    }

    public void setMaxBytesAllocated(int maxAllocations) {
        this.maxBytesAllocated = maxAllocations;
    }

    public void setCheckedForMemoryLeaks(boolean checkedForMemoryLeaks) {
        this.checkedForMemoryLeaks = checkedForMemoryLeaks;
    }

}
