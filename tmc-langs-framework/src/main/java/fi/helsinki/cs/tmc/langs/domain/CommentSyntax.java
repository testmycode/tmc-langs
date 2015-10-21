package fi.helsinki.cs.tmc.langs.domain;

import java.util.regex.Pattern;

/**
 * Information about a language comment syntax used to parse solution and stub
 * files with ExerciseBuilder.
 * 
 * <p>Use CommentSyntaxBuilder for easy setup.
 */
public class CommentSyntax {

    private final String beginSolutionRegex;
    private final String endSolutionRegex;
    private final String solutionFileRegex;
    private final String stubRegex;
    private final Pattern stubReplacePattern;

    private CommentSyntax(
            String beginSolutionRegex,
            String endSolutionRegex,
            String solutionFileRegex,
            String stubRegex) {
        this.beginSolutionRegex = beginSolutionRegex;
        this.endSolutionRegex = endSolutionRegex;
        this.solutionFileRegex = solutionFileRegex;
        this.stubRegex = stubRegex;

        this.stubReplacePattern = Pattern.compile(stubRegex);
	}

    /**
     * Will create a new Comment syntax with the specified comment syntaxes.
     * 
     * <p>If no syntax is specified, will use single line comment syntax from
     * Java.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    public String getBeginSolution() {
        return beginSolutionRegex;
    }

    public String getEndSolution() {
        return endSolutionRegex;
    }

    public String getSolutionFile() {
        return solutionFileRegex;
    }

    public String getStub() {
        return stubRegex;
    }

    public Pattern getStubReplacePattern() {
        return stubReplacePattern;
    }

    /**
     * Creates a new CommnetSyntax with the specifies language syntaxes.
     */
    public static class Builder {

        private static final String BEGIN_SOLUTION = "BEGIN[ \\t]+SOLUTION";
        private static final String END_SOLUTION = "END[ \\t]+SOLUTION";
        private static final String SOLUTION_FILE = "SOLUTION[ \\t]+FILE";
        private static final String SPACES = "[ \\t]*";
        private static final String STUB = "STUB:[ \\t]*";

        private String beginSolutionRegex;
        private String endSolutionRegex;
        private String solutionFileRegex;
        private String stubRegex;

        public Builder() {
            beginSolutionRegex = "";
            endSolutionRegex = "";
            solutionFileRegex = "";
            stubRegex = "";
        }

        /**
         * Adds a single line comment to the comment syntax.
         * @param singleLine Single line comment tag in Java regex format.
         */
        public Builder addSingleLineComment(String singleLine) {
            if (!beginSolutionRegex.isEmpty()) {
                addOrPunctuations();
            }
            String lineStart = "((" + SPACES + ")" + singleLine + SPACES;

            beginSolutionRegex += lineStart + BEGIN_SOLUTION + ".*)";
            endSolutionRegex += lineStart + END_SOLUTION + ".*)";
            solutionFileRegex += lineStart + SOLUTION_FILE + ".*)";
            stubRegex += lineStart + STUB + "(.*))";

            return this;
        }

        /**
         * Adds a multi line comment to the comment syntax.
         * @param beginning Beginning tag for the multi line comment as Java
         * regex.
         * @param end End tag for the multi line comment as Java regex.
         */
        public Builder addMultiLineComment(String beginning,
                String end) {
            if (!beginSolutionRegex.isEmpty()) {
                addOrPunctuations();
            }
            String beginComment = "((" + SPACES + ")" + beginning + SPACES;
            String endComment = SPACES + end + SPACES + ".*)";

            beginSolutionRegex += beginComment + BEGIN_SOLUTION + endComment;
            endSolutionRegex += beginComment + END_SOLUTION + endComment;
            solutionFileRegex += beginComment + SOLUTION_FILE + endComment;
            stubRegex += beginComment + STUB + "(.*[^ \\t])" + endComment;

            return this;
        }

        /**
         * Builds the CommentSyntax once all syntax tags have been given.
         */
        public CommentSyntax build() {
            if (beginSolutionRegex.isEmpty()) {
                return new Builder()
                        .addSingleLineComment("\\/\\/")
                        .build();
            }
            return new CommentSyntax(
                    beginSolutionRegex,
                    endSolutionRegex,
                    solutionFileRegex,
                    stubRegex);
        }

        private void addOrPunctuations() {
            beginSolutionRegex += "|";
            endSolutionRegex += "|";
            solutionFileRegex += "|";
            stubRegex += "|";
        }
    }
}
