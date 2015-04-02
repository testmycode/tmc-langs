package fi.helsinki.cs.tmc.langs.testrunner;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.TestDesc;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;

public class TestCaseList extends ArrayList<TestCase> {

    public static TestCaseList fromExerciseDesc(Optional<ExerciseDesc> methods) {
        TestCaseList result = new TestCaseList();
        for (TestDesc m : methods.get().tests) {

            TestCase c = new TestCase(m.name, m.name, pointsAsArray(m));
            result.add(c);
        }
        return result;
    }

    private static String[] pointsAsArray(TestDesc m) {
        String[] points = new String[m.points.size()];

        int index = 0;
        for (String thi : m.points) {
            points[index] = thi;
            index++;
        }
        return points;
    }

    public TestCaseList findByMethodName(String methodName) {
        TestCaseList result = new TestCaseList();
        for (TestCase t : this) {
            if (t.methodName.equals(methodName)) {
                result.add(t);
            }
        }
        return result;
    }

    public TestCaseList findByPointName(String pointName) {
        TestCaseList result = new TestCaseList();
        for (TestCase t : this) {
            if (Arrays.asList(t.pointNames).contains(pointName)) {
                result.add(t);
            }
        }
        return result;
    }

    public void writeToJsonFile(File file) throws IOException {
        Writer w = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)), "UTF-8");
        writeToJson(w);
        w.close();
    }

    private void writeToJson(Writer w) throws IOException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(StackTraceElement.class, new StackTraceSerializer())
                .create();
        gson.toJson(this, w);
    }

    @Override
    public TestCaseList clone() {
        TestCaseList clone = new TestCaseList();

        for (TestCase t : this) {
            clone.add(new TestCase(t));
        }

        return clone;
    }

}
