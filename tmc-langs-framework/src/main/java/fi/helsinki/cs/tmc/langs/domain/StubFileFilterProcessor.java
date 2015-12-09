package fi.helsinki.cs.tmc.langs.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

final class StubFileFilterProcessor extends Filer {

    @Override    
    List<String> filterData(List<String> input, MetaSyntax metaSyntax) {
        boolean skipLine = false;
        List<String> output = new ArrayList<>(input.size());
        for (String line : input) {
            if (line.matches(metaSyntax.getSolutionFileRegex())) {
                return new ArrayList<>();
            }
            if (line.matches(metaSyntax.getBeginSolutionRegex())) {
                skipLine = true;
            } else if (skipLine && line.matches(metaSyntax.getEndSolutionRegex())) {
                skipLine = false;
            } else if (line.matches(metaSyntax.getStubRegex())) {
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!! STUBSYNTAX DETECTED line=" + line);
                String stubContent = line.replaceFirst(metaSyntax.getStubMarker(), "");
                stubContent = stubContent.replaceFirst(metaSyntax.getCommentEndRegex(), "");
                output.add(stubContent);
                System.out.println("!!!!!!!!!!!!!!!!!! output=" + stubContent);
            } else if (!skipLine) {
                output.add(line);
            }
        }

        return output;
    }
}
