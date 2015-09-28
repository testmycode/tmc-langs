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
import java.util.Scanner;

public class RustPointsParser {

    /**
     * Parses a file containing information of points of exercise.
     */
    public Optional<ExerciseDesc> parse(Scanner scanner, String exerciseName) {
        Map<String, List<String>> map = new HashMap<>();
        while (scanner.hasNextLine()) {
            String[] keyValue = scanner.nextLine().split(" = ");
            if (keyValue.length == 1) {
                return Optional.absent();
            }
            addAndInitialise(map, keyValue[0], keyValue[1]);
            handleSuites(map, keyValue[0], keyValue[1]);
        }
        return Optional.of(buildResult(map, exerciseName));
    }

    private void handleSuites(Map<String, List<String>> map, String key, String value) {
        String[] keySplit = key.split("\\.");
        if (keySplit.length > 1) {
            addAndInitialise(map, keySplit[0], value);
        }
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
