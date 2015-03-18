package fi.helsinki.cs.tmc.langs.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class JsonWriterTest {
    
    Path outputFile;
    
    @Before
    public void setUp() {
        outputFile = Paths.get("./src/test/resources/jsonwriter_files/test.json");
    }
    
    /**
     * Test of writeObjectIntoJsonFormat method, of class JsonWriter.
     */
    @Test
    public void testWriteObjectIntoJsonFormat() {
        try {
            MockClass mock = new MockClass("test");
            JsonWriter.writeObjectIntoJsonFormat(mock, outputFile);
            
            File f = outputFile.toFile();
            Scanner s = new Scanner(f);
            assertEquals(s.nextLine(), "{\"arr\":[0,1,2,3,4,5,6,7,8,9],\"name\":\"testi\"}");
            s.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JsonWriterTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private class MockClass {
        private ArrayList<Integer> arr;
        private String name;

        public MockClass(String name) {
            this.name = name;
            for (int i = 0; i < 10; i++) {
                arr.add(i);
            }
        }
        
    }
}