package fi.helsinki.cs.tmc.langs.abstraction;

public interface ValidationError {
    int getColumn();

    int getLine();

    String getMessage();

    String getSourceName();
}
