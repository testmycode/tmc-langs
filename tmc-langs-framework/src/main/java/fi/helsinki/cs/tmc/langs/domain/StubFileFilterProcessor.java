package fi.helsinki.cs.tmc.langs.domain;

import java.util.ArrayList;
import java.util.List;

final class StubFileFilterProcessor extends Filer {

    @Override
    List<String> filterData(List<String> data, MetaSyntax meta) {
        data = removeSolutions(data, meta);
        data = cleanStubMarkers(data, meta);
        return data;
    }

    private List<String> removeSolutions(List<String> input, MetaSyntax meta) {
        boolean atSolution = false;
        List<String> output = new ArrayList<String>();
        for (String line : input) {
            if (meta.matchSolutionFile(line)) {
                return new ArrayList<>();
            }
            if (meta.matchBeginSolution(line)) {
                atSolution = true;
            }
            if (!atSolution) {
                output.add(line);
            }
            if (meta.matchEndSolution(line)) {
                atSolution = false;
            }
        }
        return output;
    }

    /**
     * NOTE: traditional comments inside multiline stubs will cause problems.
     */
    private List<String> cleanStubMarkers(List<String> input, MetaSyntax meta) {
        boolean atStub = false;
        List<String> output = new ArrayList<String>();
        for (String line : input) {
            if (line.trim().isEmpty()) {
                output.add(line);
                continue;
            }
            if (meta.matchStubBegins(line)) {
                atStub = true;
                line = meta.removeStubMarker(line);
            }
            if (atStub && meta.matchEndComment(line)) {
                atStub = false;
                line = meta.removeEndCommentSyntax(line);
            }
            if (!line.trim().isEmpty()) {
                // If line only contained metadata, don't add empty line
                output.add(line);
            }
        }
        return output;
    }
}
