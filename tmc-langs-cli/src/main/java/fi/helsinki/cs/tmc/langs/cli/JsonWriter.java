package fi.helsinki.cs.tmc.langs.cli;

import com.google.gson.Gson;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
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
        OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(outputFile.toAbsolutePath().toFile()),
                        StandardCharsets.UTF_8);
        writer.write(new Gson().toJson(obj));
        writer.close();
    }
}
