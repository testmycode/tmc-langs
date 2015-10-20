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

	public CommentSyntax(
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
}
