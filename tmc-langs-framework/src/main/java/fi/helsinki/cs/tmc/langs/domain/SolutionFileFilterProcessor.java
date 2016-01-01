package fi.helsinki.cs.tmc.langs.domain;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class SolutionFileFilterProcessor extends Filer {
    
    @Override    
    List<String> filterData(List<String> data, MetaSyntax meta) {
        data = removeStubs(data, meta);
        data = cleanSolutionMarkers(data, meta);
        return data;
    }
    
    private List<String> removeStubs(List<String> input, MetaSyntax meta) {
        boolean atStub = false;
        List<String> output = new ArrayList<String>();
        for (String line : input) {
            if (meta.matchStubBegins(line)) {
                atStub = true;
            }
            if (!atStub) {
                output.add(line);
            } else if (meta.matchEndComment(line)) {
                atStub = false;
            }
        }
        return output;    
    }
    
    private List<String> cleanSolutionMarkers(List<String> input, MetaSyntax meta) {
        List<String> output = new ArrayList<>(input.size());
        for (String line : input) {
            if (meta.matchSolutionFile(line)
                    || meta.matchBeginSolution(line)
                    || meta.matchEndSolution(line)) {
                continue;
            }
            output.add(line);
        }
        return output;
    }
   
}
