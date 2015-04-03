package fi.helsinki.cs.tmc.langs.util;

import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility for converting objects into JSON format and writing them into a file.
 */
public class JsonWriter {
    /**
     * Convert and save object into outputFile as JSON.
     *
     * @param obj        to be converted into JSON format.
     * @param outputFile destination where the converted result is to be saved
     */
    public static void writeObjectIntoJsonFormat(Object obj, Path outputFile) {
        String objectInJson = new Gson().toJson(obj);
        try {
            writeOutputFile(objectInJson, outputFile);
        } catch (IOException ex) {
            Logger.getLogger(JsonWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void writeOutputFile(String content, Path outputFile) throws IOException {
        FileWriter writer = null;

        try {
            writer = new FileWriter(outputFile.toAbsolutePath().toString());
            writer.write(content);
        } catch (IOException ex) {
            Logger.getLogger(JsonWriter.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

    }
}
