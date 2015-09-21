package fi.helsinki.cs.tmc.langs.rust;

import fi.helsinki.cs.tmc.langs.abstraction.ValidationError;

import java.util.List;

public class LintError implements ValidationError {

    private final List<String> code;
    private final int startLine;
    private final int startColumn;
    private final String description;
    private final String file;
    private final int endLine;
    private final int endColumn;

    /**
     * Creates new error that failing lints generate.
     * 
     * @param file file that the error is in
     * @param description describes what is wrong
     * @param code list of code lines the error is in
     * @param startLine from what line error start
     * @param startColumn from what column in that line error starts
     * @param endLine in what line error ends
     * @param endColumn in what column in that line error ends
     */
    public LintError(
            String file,
            String description,
            List<String> code,
            int startLine,
            int startColumn,
            int endLine,
            int endColumn) {
        this.code = code;
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

    public List<String> getCode() {
        return code;
    }

}
