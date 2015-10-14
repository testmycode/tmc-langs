package fi.helsinki.cs.tmc.langs;

/**
 * Contains default (Java) language syntax information.
 */
public class DefaultSyntax implements LanguageSyntax {

    @Override
    public String getBeginComment() {
        return "\\/\\*+";
    }

    @Override
    public String getEndComment() {
        return "\\*+\\/";
    }

    @Override
    public String getSingleLineComment() {
        return "\\/\\/";
    }

    @Override
    public boolean hasMultiLineComments() {
        return true;
    }
}
