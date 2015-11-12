package fi.helsinki.cs.tmc.langs.domain;

import com.sun.tools.javac.parser.Tokens.Comment.CommentStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

class CommentStyleFileFilter {

    private CommentSyntax commentSyntax;

    public CommentStyleFileFilter(CommentSyntax commentSyntax) {
        this.commentSyntax = commentSyntax;
    }

    List<String> filterForStub(List<String> input) {
        boolean skipLine = false;
        List<String> output = new ArrayList<>(input.size());
        for (String line : input) {
            if (line.matches(commentSyntax.getSolutionFile())) {
                return new ArrayList<>();
            }
            if (line.matches(commentSyntax.getBeginSolution())) {
                skipLine = true;
            } else if (skipLine && line.matches(commentSyntax.getEndSolution())) {
                skipLine = false;
            } else if (line.matches(commentSyntax.getBeginSolution())) {
                Matcher stubMatcher = commentSyntax.getStubReplacePattern().matcher(line);
                output.add(stubMatcher.replaceAll(commentSyntax.getCapturingGroups()));
            } else if (!skipLine) {
                output.add(line);
            }
        }

        return output;
    }

    List<String> filterForSolution(List<String> input) {
        List<String> output = new ArrayList<>(input.size());
        for (String line : input) {
            if (line.matches(commentSyntax.getBeginSolution())
                    || line.matches(commentSyntax.getEndSolution())
                    || line.matches(commentSyntax.getStub())
                    || line.matches(commentSyntax.getSolutionFile())) {
                continue;
            }
            output.add(line);
        }
        return output;
    }
}
