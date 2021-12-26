package fi.helsinki.cs.tmc.langs.rust;

import fi.helsinki.cs.tmc.langs.abstraction.ValidationError;

public class LintError implements ValidationError {

    private final int startLine;
    private final int startColumn;
    private final String message;
    private final String file;

    /**
     * Creates new error that failing lints generate.
     * 
     * @param file file that the error is in
     * @param message describes what is wrong
     * @param startLine from what line error start
     * @param startColumn from what column in that line error starts
     */
    public LintError(
            String file,
            String message,
            int startLine,
            int startColumn
    ) {
        this.file = file;
        this.message = message;
        this.startLine = startLine;
        this.startColumn = startColumn;
    }

    @Override
    public int getColumn() {
        return startColumn;
    }

    @Override
    public int getLine() {
        return startLine;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getSourceName() {
        return file;
    }

}
