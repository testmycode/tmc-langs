package fi.helsinki.cs.tmc.langs.domain;

public final class NoLanguagePluginFoundException extends Exception {

    public NoLanguagePluginFoundException(String message) {
        super(message);
    }

    public NoLanguagePluginFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
