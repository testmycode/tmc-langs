package fi.helsinki.cs.tmc.langs.rust;

import fi.helsinki.cs.tmc.langs.abstraction.ValidationError;

public class LintError implements ValidationError {

    private final int startLine;
    private final int startColumn;
    private final String description;
    private final String file;
    private final int endLine;
    private final int endColumn;

    public LintError(
            String file,
            String description,
            int startLine,
            int startColumn,
            int endLine,
            int endColumn) {
        this.file = file;
        this.description = description;
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
    }

    @Override
    public int getColumn() {
        return startColumn;
    }

    @Override
    public int getLine() {
        return startLine;
    }

    public int getEndColumn() {
        return endColumn;
    }

    public int getEndLine() {
        return endLine;
    }

    @Override
    public String getMessage() {
        return description;
    }

    @Override
    public String getSourceName() {
        return file;
    }

}
