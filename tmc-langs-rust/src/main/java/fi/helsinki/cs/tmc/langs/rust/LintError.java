
package fi.helsinki.cs.tmc.langs.rust;

import fi.helsinki.cs.tmc.langs.abstraction.ValidationError;

public class LintError implements ValidationError {
    private final int start_line;
    private final int start_column;
    private final String description;
    private final String file;
    private final int end_line;
    private final int end_column;

    public LintError(String file, String description, int start_line, int start_column, int end_line, int end_column) {
        this.file = file;
        this.description = description;
        this.start_line = start_line;
        this.start_column = start_column;
        this.end_line = end_line;
        this.end_column = end_column;
    }

    @Override
    public int getColumn() {
        return 0;
    }

    @Override
    public int getLine() {
        return 0;
    }

    @Override
    public String getMessage() {
        return "";
    }

    @Override
    public String getSourceName() {
        return "";
    }
    
}
