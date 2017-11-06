package fi.helsinki.cs.tmc.langs.r;

import fi.helsinki.cs.tmc.langs.domain.TestDesc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RExerciseDescParser {

    private static final Path RESULT_FILE = Paths.get(".available_points.json");
    private static final TypeReference<Map<String, List<String>>> MAP_TYPE_REFERENCE =
            new TypeReference<Map<String, List<String>>>() {};
    private final Path path;
    private final ObjectMapper mapper;

    public RExerciseDescParser(Path path) {
        this.path = path;
        this.mapper = new ObjectMapper();
    }

    public ImmutableList<TestDesc> parse() throws IOException {
        List<TestDesc> testDescs = new ArrayList<>();
        byte[] json = Files.readAllBytes(path.resolve(RESULT_FILE));
        Map<String, List<String>> parse = mapper.readValue(json, MAP_TYPE_REFERENCE);

        for (String name : parse.keySet()) {
            ImmutableList<String> points = ImmutableList.copyOf(parse.get(name));
            testDescs.add(new TestDesc(name, points));
        }

        return ImmutableList.copyOf(testDescs);
    }
}
