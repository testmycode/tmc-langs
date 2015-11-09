package fi.helsinki.cs.tmc.langs.cli;

import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Utility for converting objects into JSON format and writing them into a file.
 */
public final class JsonWriter {

    /**
     * Convert and save object into outputFile as JSON.
     *
     * @param obj to be converted into JSON format.
     * @param outputFile destination where the converted result is to be saved
     */
    public static void writeObjectIntoJsonFormat(Object obj, Path outputFile) throws IOException {
        FileWriter writer = new FileWriter(outputFile.toAbsolutePath().toFile());
        writer.write(new Gson().toJson(obj));
        writer.close();
    }
}
