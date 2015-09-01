package fi.helsinki.cs.tmc.langs.java.testrunner;

import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.TestCase;
import fi.helsinki.cs.tmc.langs.domain.TestDesc;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;

public final class TestCaseList extends ArrayList<TestCase> {

    /**
     * Creates a TestCaseList from a given ExerciseDesc.
     */
    public static TestCaseList fromExerciseDesc(Optional<ExerciseDesc> methods) {
        TestCaseList result = new TestCaseList();
        for (TestDesc method : methods.get().tests) {

            TestCase testCase =
                    new TestCase(
                            getClassName(method.name),
                            getMethodName(method.name),
                            pointsAsArray(method));
            result.add(testCase);
        }
        return result;
    }

    private static String getMethodName(String exerciseName) {
        String[] names = exerciseName.split(" ");
        return names[1];
    }

    private static String getClassName(String exerciseName) {
        String[] names = exerciseName.split(" ");
        return names[0];
    }

    private static String[] pointsAsArray(TestDesc testDesc) {
        String[] points = new String[testDesc.points.size()];

        int index = 0;
        for (String pointName : testDesc.points) {
            points[index] = pointName;
            index++;
        }
        return points;
    }

    /**
     * Returns all TestCases that match the given method name.
     */
    public TestCaseList findByMethodName(String methodName) {
        TestCaseList result = new TestCaseList();
        for (TestCase testCase : this) {
            if (testCase.methodName.equals(methodName)) {
                result.add(testCase);
            }
        }
        return result;
    }

    /**
     * Returns all TestCases that match the given point name.
     */
    public TestCaseList findByPointName(String pointName) {
        TestCaseList result = new TestCaseList();
        for (TestCase testCase : this) {
            if (Arrays.asList(testCase.pointNames).contains(pointName)) {
                result.add(testCase);
            }
        }
        return result;
    }

    /**
     * Writes this TestCaseList to a file as JSON.
     */
    public void writeToJsonFile(File file) throws IOException {
        Writer writer =
                new OutputStreamWriter(
                        new BufferedOutputStream(new FileOutputStream(file)), "UTF-8");
        writeToJson(writer);
        writer.close();
    }

    private void writeToJson(Writer writer) {
        Gson gson =
                new GsonBuilder()
                        .registerTypeAdapter(StackTraceElement.class, new StackTraceSerializer())
                        .create();
        gson.toJson(this, writer);
    }

    @Override
    public TestCaseList clone() {
        TestCaseList clone = new TestCaseList();

        for (TestCase testCase : this) {
            clone.add(new TestCase(testCase));
        }

        return clone;
    }
}
