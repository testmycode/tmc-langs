package fi.helsinki.cs.tmc.langs.domain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

final class SolutionFileFilterProcessor extends Filer {

    // TODO remove soon
    private final String beginSolutionRegex = null;
    private final String capturingGroups = null;
    private final String endSolutionRegex = null;
    private final String solutionFileRegex = null;
    private final String stubRegex = null;
    private final Pattern stubReplacePattern = null;

    List<String> prepareFile(Path file) {
        try {
            List<String> result;
            try (Scanner scanner = new Scanner(Files.newInputStream(file))) {
                result = new ArrayList<>();
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.matches(beginSolutionRegex)
                            || line.matches(endSolutionRegex)
                            || line.matches(stubRegex)
                            || line.matches(solutionFileRegex)) {
                        continue;
                    }
                    result.add(line);
                }
            }
            return result;
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
