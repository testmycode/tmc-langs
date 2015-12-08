package fi.helsinki.cs.tmc.langs.domain;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class SolutionFileFilterProcessor extends Filer {
    
    @Override    
    List<String> filterData(List<String> input, MetaSyntax m) {
        List<String> output = new ArrayList<>(input.size());
        for (String line : input) {
            if (line.matches(m.getBeginSolutionRegex())
                    || line.matches(m.getEndSolutionRegex())
                    || line.matches(m.getStubRegex())
                    || line.matches(m.getSolutionFileRegex())) {
                continue;
            }
            output.add(line);
        }
        return output;
    }
}
