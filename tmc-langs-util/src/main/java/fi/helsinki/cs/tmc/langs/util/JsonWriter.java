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
     * @param obj to be converted into JSON format.
     * @param outputFile destination where the converted result is to be saved
     */
    public static void writeObjectIntoJsonFormat(Object obj, Path outputFile) {
        String objectInJson = buildJson(obj);
        writeOutputFile(objectInJson, outputFile);
    }
    
    private static String buildJson(Object obj) {
        Gson gson = new Gson();
        return gson.toJson(obj);
    }
    
    private static void writeOutputFile(String content, Path outputFile) {
        try {
            FileWriter writer = new FileWriter(outputFile.toAbsolutePath().toString());
            writer.write(content);
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(JsonWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
