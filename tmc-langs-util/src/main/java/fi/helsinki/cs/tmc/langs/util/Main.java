package fi.helsinki.cs.tmc.langs.util;

import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            printHelp();
            System.exit(0);
        }

        run(args);
    }

    private static void printHelp() {
        System.out.println("Usage: TODO: Write instructions here.");
    }

    private static void run(String[] args) {
        for (String arg : args) {
            System.out.println(ProjectTypeHandler.getLanguagePlugin(Paths.get(arg)).getLanguageName());
        }

    }
}
