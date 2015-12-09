package fi.helsinki.cs.tmc.langs.domain;

public class MetaSyntax {
    
    private static final String BEGIN_SOLUTION = "BEGIN[ \\t]+SOLUTION";
    private static final String END_SOLUTION = "END[ \\t]+SOLUTION";
    private static final String SOLUTION_FILE = "SOLUTION[ \\t]+FILE";
    private static final String SPACES = "[ \\t]*";
    private static final String STUB = "STUB:[ \\t]*";
   
    private String commentStartRegex;
    private String commentEndRegex;
    private String beginSolutionRegex;
    private String endSolutionRegex;
    private String solutionFileRegex;
    private String stubRegex;
    private String stubMarker;

    public MetaSyntax(String commentStartSyntax, String commentEndSyntax) {
        this.commentStartRegex = SPACES + commentStartSyntax + SPACES;
        this.commentEndRegex = SPACES + commentEndSyntax + SPACES;
        this.beginSolutionRegex = commentStartRegex + BEGIN_SOLUTION + commentEndRegex;
        this.endSolutionRegex = commentStartRegex + END_SOLUTION + commentEndRegex;
        this.solutionFileRegex = commentStartRegex + SOLUTION_FILE + commentEndRegex;
        this.stubRegex = commentStartRegex + STUB + "(.*)" + commentEndRegex;
        this.stubMarker = commentStartSyntax + STUB; // Initial spaces left outside stubMarker on purpose
    }

    
    /** eg. " -->    " */
    public String getCommentEndRegex() {
        return commentEndRegex;
    }

    /** eg. "    <!-- BEGIN SOLUTION --> " */
    public String getBeginSolutionRegex() {
        return beginSolutionRegex;
    }

    /** eg. "    <!-- END SOLUTION --> " */
    public String getEndSolutionRegex() {
        return endSolutionRegex;
    }

    /** eg. "    <!-- SOLUTION FILE --> " */
    public String getSolutionFileRegex() {
        return solutionFileRegex;
    }

    /** eg. "    <!-- STUB: return 0; --> " */
    public String getStubRegex() {
        return stubRegex;
    }
    
    /** eg. "<!--STUB: " */
    public String getStubMarker() {
        return stubMarker;
    }
}
