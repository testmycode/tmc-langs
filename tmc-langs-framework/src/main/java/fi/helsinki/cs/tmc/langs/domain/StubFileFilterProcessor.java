package fi.helsinki.cs.tmc.langs.domain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class StubFileFilterProcessor extends Filer {

    // TODO remove soon
    private final String beginSolutionRegex = null;
    private final String capturingGroups = null;
    private final String endSolutionRegex = null;
    private final String solutionFileRegex = null;
    private final String stubRegex = null;
    private final Pattern stubReplacePattern = null;

    @Override
    List<String> prepareFile(Path from) {
        try {
            boolean skipLine = false;
            List<String> result;
            try (Scanner scanner = new Scanner(Files.newInputStream(from))) {
                result = new ArrayList<>();
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.matches(solutionFileRegex)) {
                        scanner.close();
                        return new ArrayList<>();
                    }
                    if (line.matches(beginSolutionRegex)) {
                        skipLine = true;
                    } else if (skipLine && line.matches(endSolutionRegex)) {
                        skipLine = false;
                    } else if (line.matches(stubRegex)) {
                        Matcher stubMatcher = stubReplacePattern.matcher(line);
                        result.add(stubMatcher.replaceAll(capturingGroups));
                    } else if (!skipLine) {
                        result.add(line);
                    }
                }
            }
            return result;
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
