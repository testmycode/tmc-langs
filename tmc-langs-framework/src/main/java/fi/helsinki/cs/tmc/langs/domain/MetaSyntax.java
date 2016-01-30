package fi.helsinki.cs.tmc.langs.domain;

import java.util.regex.Pattern;

public class MetaSyntax {

    private static final String BEGIN_SOLUTION = "BEGIN[ \\t]+SOLUTION";
    private static final String END_SOLUTION = "END[ \\t]+SOLUTION";
    private static final String SOLUTION_FILE = "SOLUTION[ \\t]+FILE";
    private static final String SPACES = "[ \\t]*";
    private static final String STUB = "STUB:" + SPACES;

    private final Pattern commentStartRegex;
    private final Pattern commentEndRegexPattern;
    private final Pattern beginSolutionRegex;
    private final Pattern endSolutionRegex;
    private final Pattern solutionFileRegex;
    private final Pattern stubBeginsRegexPattern;

    private final String commentEndRegex;
    private final String stubMarker;
    private final String stubBeginsRegex; // Either single or multi line

    public MetaSyntax(String commentStartSyntax, String commentEndSyntax) {
        this.commentStartRegex = Pattern.compile("^" + SPACES + commentStartSyntax + SPACES);
        this.commentEndRegex = SPACES + commentEndSyntax + SPACES + "$";
        this.commentEndRegexPattern = Pattern.compile(this.commentEndRegex);
        this.beginSolutionRegex =
                Pattern.compile(commentStartRegex + BEGIN_SOLUTION + commentEndRegex);
        this.endSolutionRegex = Pattern.compile(commentStartRegex + END_SOLUTION + commentEndRegex);
        this.solutionFileRegex =
                Pattern.compile(commentStartRegex + SOLUTION_FILE + commentEndRegex);

        // Initial spaces left outside stubMarker to maintain indentation
        this.stubMarker = commentStartSyntax + SPACES + STUB + SPACES;

        this.stubBeginsRegex = commentStartRegex + STUB + "(.*)";
        this.stubBeginsRegexPattern = Pattern.compile(this.stubBeginsRegex);
    }

    /** True if line looks like {@code " <!-- BEGIN SOLUTION --> " }. */
    public boolean matchBeginSolution(String line) {
        return beginSolutionRegex.matcher(line).matches();
    }

    /** True if line looks like {@code " <!-- END SOLUTION --> " }. */
    public boolean matchEndSolution(String line) {
        return endSolutionRegex.matcher(line).matches();
    }

    /** True if line looks like {@code " <!-- SOLUTION FILE --> " }. */
    public boolean matchSolutionFile(String line) {
        return solutionFileRegex.matcher(line).matches();
    }

    /** True if line STARTS WITH {@code "*whitespace* <!-- STUB:" }. */
    public boolean matchStubBegins(String line) {
        return stubBeginsRegexPattern.matcher(line).matches();
    }

    /** True if line ends with comment end syntax. **/
    public boolean matchEndComment(String line) {
        return line.matches("(.*)" + commentEndRegexPattern);
    }

    /** Returns given String without Alexander Stubb. **/
    public String removeStubMarker(String line) {
        return line.replaceFirst(stubMarker, "");
    }

    /** Returns given String without end comment syntax. **/
    public String removeEndCommentSyntax(String line) {
        return line.replaceFirst(commentEndRegex, "");
    }
}
