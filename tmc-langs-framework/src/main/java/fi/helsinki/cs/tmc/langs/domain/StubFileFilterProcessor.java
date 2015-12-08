package fi.helsinki.cs.tmc.langs.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

final class StubFileFilterProcessor extends Filer {

    @Override    
    List<String> filterData(List<String> input, MetaSyntax m) {
        boolean skipLine = false;
        List<String> output = new ArrayList<>(input.size());
        for (String line : input) {
            if (line.matches(m.getSolutionFileRegex())) {
                return new ArrayList<>();
            }
            if (line.matches(m.getBeginSolutionRegex())) {
                skipLine = true;
            } else if (skipLine && line.matches(m.getEndSolutionRegex())) {
                skipLine = false;
            } else if (line.matches(m.getStubRegex())) {
                String stubContent = line.replaceFirst(m.getStubMarker(), "");
                stubContent = stubContent.replaceAll(m.getCommentEndRegex(), "");
                //NOTE: will remove all instances of commentEndRegex, for example --> --> -->
                output.add(stubContent);
            } else if (!skipLine) {
                output.add(line);
            }
        }

        return output;
    }
}
