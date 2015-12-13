package fi.helsinki.cs.tmc.langs.domain;

public class MetaSyntax {
    
    private static final String BEGIN_SOLUTION = "BEGIN[ \\t]+SOLUTION";
    private static final String END_SOLUTION = "END[ \\t]+SOLUTION";
    private static final String SOLUTION_FILE = "SOLUTION[ \\t]+FILE";
    private static final String SPACES = "[ \\t]*";
    private static final String STUB = "STUB:" + SPACES;
   
    private String commentStartRegex;
    private String commentEndRegex;
    private String beginSolutionRegex;
    private String endSolutionRegex;
    private String solutionFileRegex;
    
    private String stubMarker;
    private String stubBeginsRegex; // Either single or multi line

    public MetaSyntax(String commentStartSyntax, String commentEndSyntax) {
        this.commentStartRegex = "^" + SPACES + commentStartSyntax + SPACES;
        this.commentEndRegex = SPACES + commentEndSyntax + SPACES + "$";
        this.beginSolutionRegex = commentStartRegex + BEGIN_SOLUTION + commentEndRegex;
        this.endSolutionRegex = commentStartRegex + END_SOLUTION + commentEndRegex;
        this.solutionFileRegex = commentStartRegex + SOLUTION_FILE + commentEndRegex;
        
        // Initial spaces left outside stubMarker to maintain indentation
        this.stubMarker = commentStartSyntax + SPACES + STUB + SPACES;
        this.stubBeginsRegex = commentStartRegex + STUB;
    }

    /** True if line looks like {@code " <!-- BEGIN SOLUTION --> " } */
    public boolean matchBeginSolution(String line) {
        return line.matches(beginSolutionRegex);
    }

    /** True if line looks like {@code " <!-- END SOLUTION --> " } */
    public boolean matchEndSolution(String line) {
        return line.matches(endSolutionRegex);
    }

    /** True if line looks like {@code " <!-- SOLUTION FILE --> " } */
    public boolean matchSolutionFile(String line) {
        return line.matches(solutionFileRegex);
    }
    
    /** True if line STARTS WITH {@code "*whitespace* <!-- STUB:" } */
    public boolean matchStubBegins(String line) {
        return line.matches(stubBeginsRegex + "(.*)");
    }
    
    /** True if line ends with comment end syntax **/
    public boolean matchEndComment(String line) {
        return line.matches("(.*)" + commentEndRegex);
    }
    
    /** Returns given String without Alexander Stubb **/
    public String removeStubMarker(String line) {
        return line.replaceFirst(stubMarker, "");
    }
    
    /** Returns given String without end comment syntax **/
    public String removeEndCommentSyntax(String line) {
        return line.replaceFirst(commentEndRegex, "");
    }
}
