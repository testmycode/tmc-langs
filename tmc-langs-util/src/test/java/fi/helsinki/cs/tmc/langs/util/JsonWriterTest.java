package fi.helsinki.cs.tmc.langs.util;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

public class JsonWriterTest {

    File outputFile;

    @Before
    public void setUp() throws IOException {
        outputFile = Files.createTempFile("test", "json").toFile();
    }

    /**
     * Test of writeObjectIntoJsonFormat method, of class JsonWriter.
     */
    @Test
    public void testWriteObjectIntoJsonFormat() {
        try {
            MockClass mock = new MockClass("test");
            try {
                JsonWriter.writeObjectIntoJsonFormat(mock, outputFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            Scanner s = new Scanner(outputFile);
            assertEquals(s.nextLine(), "{\"arr\":[0,1,2,3,4,5,6,7,8,9],\"name\":\"test\"}");
            s.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JsonWriterTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private class MockClass {

        private ArrayList<Integer> arr = new ArrayList<>();
        private String name;

        public MockClass(String name) {
            this.name = name;
            for (int i = 0; i < 10; i++) {
                arr.add(i);
            }
        }
    }
}
