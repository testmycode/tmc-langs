package fi.helsinki.cs.tmc.langs.make;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public final class MakeUtils {

    private static final String FILE_NOT_FOUND_ERROR_MESSAGE = "Couldn't initialize file scanner.";

    private static final Logger log = LoggerFactory.getLogger(MakeUtils.class);

    /**
     * Builds a map that maps tests with their available points.
     */
    public Map<String, List<String>> mapIdsToPoints(Path availablePoints) {
        Scanner scanner = initFileScanner(availablePoints);
        Map<String, List<String>> idsToPoints = new HashMap<>();

        if (scanner == null) {
            return idsToPoints;
        }

        while (scanner.hasNextLine()) {
            String[] parts = rowParts(scanner);

            String key = parts[1];
            String value = parts[2];

            addPointsToId(idsToPoints, key, value);
        }

        return idsToPoints;
    }

    /**
     * Initializes a file scanner or returns null if it's not possible.
     */
    public Scanner initFileScanner(Path file) {
        try {
            return new Scanner(file);
        } catch (IOException e) {
            log.error(FILE_NOT_FOUND_ERROR_MESSAGE);
            log.error(e.toString());
            return null;
        }
    }

    /**
     * Extracts method and class names from the scanner's next line.
     */
    public String[] rowParts(Scanner scanner) {
        if (scanner == null) {
            return new String[0];
        }

        String row = scanner.nextLine();
        String[] parts = row.split(" \\[|\\] | ", 3);

        parts = removeFirstCharacters(parts, "[");

        String testClass = parts[0];
        String testMethod = parts[1];
        String testPoint = parts[2];

        return new String[] {testClass, testMethod, testPoint};
    }

    private void addPointsToId(Map<String, List<String>> idsToPoints, String key, String value) {
        if (!idsToPoints.containsKey(key)) {
            idsToPoints.put(key, new ArrayList<String>());
        }

        String[] points = value.split(" ");

        for (String point : points) {
            idsToPoints.get(key).add(point);
        }
    }

    private String[] removeFirstCharacters(String[] array, String character) {
        String[] ret = new String[array.length];

        for (int i = 0; i < array.length; i++) {
            if (array[i].startsWith(character)) {
                ret[i] = array[i].substring(1);
            } else {
                ret[i] = array[i];
            }
        }

        return ret;
    }
}
