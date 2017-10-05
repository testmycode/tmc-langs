
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



class RExerciseDescParser {
    
    private static Path RESULT_FILE = Paths.get(".available_points.json");
    private static final TypeReference<List<RResult>> MAP_TYPE_REFERENCE =
            new TypeReference<List<RResult>>() {};
    private Path path;
    private ObjectMapper mapper;
    
    public RExerciseDescParser(Path path) {
        this.path = path;
        this.mapper = new ObjectMapper();
    }
    
    public ImmutableList<TestDesc> parse() throws IOException {
        
        List<TestDesc> testDescs = new ArrayList<>();
        byte[] json = Files.readAllBytes(path.resolve(RESULT_FILE));
        List<RResult> parse = mapper.readValue(json, MAP_TYPE_REFERENCE);
        for (RResult result : parse) {
            ImmutableList<String> points = ImmutableList.copyOf(result.getPoints());
            testDescs.add(new TestDesc(result.getName(), points));
        }
        return ImmutableList.copyOf(testDescs);
    }
    
}
