package fi.helsinki.cs.tmc.langs.domain;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class SolutionFileFilterProcessor extends Filer {
    
    @Override    
    List<String> filterData(List<String> input, MetaSyntax metaSyntax) {
        List<String> output = new ArrayList<>(input.size());
        for (String line : input) {
            if (line.matches(metaSyntax.getBeginSolutionRegex())
                    || line.matches(metaSyntax.getEndSolutionRegex())
                    || line.matches(metaSyntax.getStubRegex())
                    || line.matches(metaSyntax.getSolutionFileRegex())) {
                continue;
            }
            output.add(line);
        }
        return output;
    }
}
