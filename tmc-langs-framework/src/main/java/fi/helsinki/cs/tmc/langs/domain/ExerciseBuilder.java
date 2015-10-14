package fi.helsinki.cs.tmc.langs.domain;

import fi.helsinki.cs.tmc.langs.DefaultSyntax;
import fi.helsinki.cs.tmc.langs.LanguageSyntax;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExerciseBuilder {

    private static final String BEGIN_SOLUTION = "BEGIN[ \\t]+SOLUTION";
    private static final String END_SOLUTION = "END[ \\t]+SOLUTION";
    private static final String SOLUTION_FILE = "SOLUTION[ \\t]+FILE";
    private static final String STUB = "STUB:[ \\t]*";
    private static final String SOURCE_FOLDER_NAME = "src";
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final Logger logger = LoggerFactory.getLogger(ExerciseBuilder.class);

    private String beginSolutionRegex;
    private String endSolutionRegex;
    private String solutionFileRegex;
    private String stubRegex;
    private Pattern stubReplacePattern;

    public ExerciseBuilder() {
        this(new DefaultSyntax());
    }

    public ExerciseBuilder(LanguageSyntax language) {
        String spaces = "[ \\t]*";
        String lineStart = "(" + spaces + ")" + language.getSingleLineComment() + spaces;

        beginSolutionRegex = "(" + lineStart + BEGIN_SOLUTION + ".*)";
        endSolutionRegex = "(" + lineStart + END_SOLUTION + ".*)";
        solutionFileRegex = "(" + lineStart + SOLUTION_FILE + ".*)";
        stubRegex = "(" + lineStart + STUB + "(.*))";

        if (language.hasMultiLineComments()) {
            String beginComment = "|((" + spaces + ")" + language.getBeginComment() + spaces;
            String endComment = spaces + language.getEndComment() + spaces + ")";

            beginSolutionRegex += beginComment + BEGIN_SOLUTION + endComment + ".*";
            endSolutionRegex += beginComment + END_SOLUTION + endComment + ".*";
            solutionFileRegex += beginComment + SOLUTION_FILE + endComment + ".*";
            stubRegex += beginComment + STUB + "(.*[^ \\t])" + endComment;
        }

        stubReplacePattern = Pattern.compile(stubRegex);
    }

    /**
     * Prepares a stub exercise from the original.
     *
     * <p>Implements LanguagePlugin.prepareStub
     */
    public void prepareStub(Path path) {
        Path projectRoot = path.resolve(SOURCE_FOLDER_NAME);
        List<Path> projectFiles = getFileList(projectRoot);

        for (Path projectFile : projectFiles) {
            prepareStubFile(projectFile);
        }
    }

    private void prepareStubFile(Path file) {
        try {
            List<String> lines = Files.readAllLines(file, CHARSET);
            List<String> filteredLines = new ArrayList<>();
            boolean skipLine = false;
            for (String line : lines) {
                if (line.matches(solutionFileRegex)) {
                    Files.deleteIfExists(file);
                    return;
                }
                if (line.matches(beginSolutionRegex)) {
                    skipLine = true;
                } else if (skipLine && line.matches(endSolutionRegex)) {
                    skipLine = false;
                } else if (line.matches(stubRegex)) {
                    Matcher stubMatcher = stubReplacePattern.matcher(line);
                    filteredLines.add(stubMatcher.replaceAll("$2$5$3$6"));
                } else if (!skipLine) {
                    filteredLines.add(line);
                }
            }
            Files.write(file, filteredLines, CHARSET);
        } catch (IOException ex) {
            logger.error("Unexpected IOException, preparation of file {} was interrupted",
                    file.toAbsolutePath().toString(),
                    ex);
        }
    }

    private List<Path> getFileList(Path folder) {
        if (!folder.toFile().isDirectory()) {
            return new ArrayList<>();
        }
        ArrayList<Path> result = new ArrayList<>();
        try {
            for (Path file : Files.newDirectoryStream(folder)) {
                if (file.toFile().isDirectory()) {
                    result.addAll(getFileList(file));
                    continue;
                }
                result.add(file);
            }
        } catch (IOException e) {
            logger.error("Unexpected IOException, getting file list of {} was interrupted",
                    folder.toAbsolutePath().toString(),
                    e);
        }
        return result;
    }

    /**
     * Prepares a presentable solution from the original.
     *
     * <p>Implements LanguagePlugin.prepareSolution
     */
    public void prepareSolution(Path path) {
        Path projectRoot = path.resolve(SOURCE_FOLDER_NAME);
        List<Path> projectFiles = getFileList(projectRoot);

        for (Path projectFile : projectFiles) {
            prepareSolutionFile(projectFile);
        }
    }

    private void prepareSolutionFile(Path file) {
        try {
            List<String> lines = Files.readAllLines(file, CHARSET);
            List<String> filteredLines = new ArrayList<>();
            for (String line : lines) {
                if (line.matches(beginSolutionRegex)
                        || line.matches(endSolutionRegex)
                        || line.matches(stubRegex)
                        || line.matches(solutionFileRegex)) {
                    continue;
                }
                filteredLines.add(line);
            }
            Files.write(file, filteredLines, CHARSET);
        } catch (IOException ex) {
            logger.error("Unexpected IOException, preparation of file {} was interrupted",
                    file.toAbsolutePath().toString(),
                    ex);
        }
    }
}