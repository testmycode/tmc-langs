package fi.helsinki.cs.tmc.langs.rust;

import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.TestDesc;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class RustPointsParser {

    /**
     * Parses a file containing information of points of exercise.
     */
    public Optional<ExerciseDesc> parse(List<String> lines, String exerciseName) {
        Map<String, List<String>> map = new HashMap<>();
        Map<String, List<String>> suites = new HashMap<>();
        for (String line : lines) {
            String[] keyValue = line.split(" = ");
            if (keyValue.length != 2) {
                return Optional.absent();
            }
            String[] values = keyValue[1].split(" ");
            for (String value: values) {
                addAndInitialise(map, keyValue[0], value);
            }
            if (!keyValue[0].contains(".")) {
                for (String value: values) {
                    addAndInitialise(suites, keyValue[0], value);
                }
            }
        }
        for (Entry<String, List<String>> e: map.entrySet()) {
            String[] keySplit = e.getKey().split("\\.");
            if (keySplit.length > 2) {
                return Optional.absent();
            }
            if (keySplit.length > 1 && suites.containsKey(keySplit[0])) {
                for (String v: suites.get(keySplit[0])) {
                    e.getValue().add(v);
                }
            }
        }
        return Optional.of(buildResult(map, exerciseName));
    }

    private void addAndInitialise(Map<String, List<String>> map, String key, String value) {
        List<String> list = map.get(key);
        if (list == null) {
            list = new ArrayList<>();
            map.put(key, list);
        }
        list.add(value);
    }

    private ExerciseDesc buildResult(Map<String, List<String>> map, String exerciseName) {
        Builder<TestDesc> builder = ImmutableList.builder();
        for (Entry<String, List<String>> entry : map.entrySet()) {
            builder.add(new TestDesc(entry.getKey(), ImmutableList.copyOf(entry.getValue())));
        }
        return new ExerciseDesc(exerciseName, builder.build());
    }

}
