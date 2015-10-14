package fi.helsinki.cs.tmc.langs;

/**
 * Interface for information about a language syntax used to parse comments.
 */
public interface LanguageSyntax {
    
    /**
     * Returns a comment syntax for the beginning of a multi line comment or null if there is none.
     */
    String getBeginComment();
    
    /**
     * Returns a comment syntax for the end of a multi line comment or null if there is none.
     */
    String getEndComment();
    
    /**
     * Returns a language single line comment syntax.
     */
    String getSingleLineComment();
    
    /**
     * Returns true if language supports multi line comments.
     */
    boolean hasMultiLineComments();
}
