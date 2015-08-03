package fi.helsinki.cs.tmc.langs.python3;

import fi.helsinki.cs.tmc.langs.domain.TestDesc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Python3ExerciseDescParser {
    private static Path RESULT_FILE = Paths.get(".available_points.json");

    private Path path;

    private static Logger log = LoggerFactory.getLogger(Python3Plugin.class);

    public Python3ExerciseDescParser(Path path) {
        this.path = path;
    }

    /**
     * Parses the available points.
     */
    public ImmutableList<TestDesc> parse() {
        List<TestDesc> res = new ArrayList<>();
        Map<String, List<String>> parse = getResults();
        for (String name : parse.keySet()) {
            ImmutableList<String> points = ImmutableList.copyOf(parse.get(name));
            res.add(new TestDesc(name, points));
        }
        return ImmutableList.copyOf(res);
    }

    private Map<String, List<String>> getResults() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            byte[] json = Files.readAllBytes(path.resolve(RESULT_FILE));
            return (Map<String, List<String>>) mapper.readValue(json, Map.class);
        } catch (IOException e) {
            log.error(e.toString());
        }
        return new HashMap<>();
    }
}
