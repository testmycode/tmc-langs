/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.helsinki.cs.tmc.langs.util;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author villheik
 */
public class ExerciseUtilsTest {

    public ExerciseUtilsTest() {
    }

    private File createTemporaryCopyOf(File file) throws IOException {
        File tempFolder = Files.createTempDir();
        FileUtils.copyDirectory(file, tempFolder);
        return tempFolder;
    }

    /**
     * Test of prepareStub method, of class ExerciseUtils.
     * @throws java.io.IOException
     */
    @Test
    public void testPrepareStub() throws IOException {
        System.out.println("prepareStub");
        File originProject = new File("src/test/resources/arith_funcs/");

        File targetFolder = createTemporaryCopyOf(originProject);
        ExerciseUtils instance = new ExerciseUtils();

        instance.prepareStub(targetFolder.toPath());
       
        File expectedFolder = new File("src/test/resources/arith_funcs_stub/src");
        File outputFolder = new File(targetFolder.getAbsolutePath() + "/src");
        
        Map<String, File> expectedFiles = getFileMap(expectedFolder);
        Map<String, File> actualFiles = getFileMap(outputFolder);
        
        for(String fileName : expectedFiles.keySet()) {
            File expected = expectedFiles.get(fileName);
            File actual = actualFiles.get(fileName);
            List<String> expectedLines = FileUtils.readLines(expected);
            List<String> actualLines = FileUtils.readLines(actual);
            for (int i = 0 ; i < expectedLines.size() ; ++i) {
                String expectedLine = expectedLines.get(i);
                String actualLine = actualLines.get(i);
                assertEquals("Line in file " + fileName + " did not match ", expectedLine, actualLine);
            }
            
        }
        
    }

    private Map<String, File> getFileMap(File folder) throws IOException {
        if (!folder.isDirectory()) {
            throw new IOException("Supplied path was not a directory");
        }
        Map<String, File> result = new HashMap<>();
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                result.putAll(getFileMap(file));
                continue;
            }
            result.put(file.getName(), file);
        }
        return result;
    }

    /**
     * Test of prepareSolution method, of class ExerciseUtils.
     */
    @Test
    public void testPrepareSolution() throws IOException {
        System.out.println("prepareSolution");
        File originProject = new File("src/test/resources/arith_funcs/");

        File targetFolder = createTemporaryCopyOf(originProject);
        ExerciseUtils instance = new ExerciseUtils();

        instance.prepareSolution(targetFolder.toPath());
       
        File expectedFolder = new File("src/test/resources/arith_funcs_solution/src");
        File outputFolder = new File(targetFolder.getAbsolutePath() + "/src");
        
        Map<String, File> expectedFiles = getFileMap(expectedFolder);
        Map<String, File> actualFiles = getFileMap(outputFolder);
        
        for(String fileName : expectedFiles.keySet()) {
            File expected = expectedFiles.get(fileName);
            File actual = actualFiles.get(fileName);
            List<String> expectedLines = FileUtils.readLines(expected);
            List<String> actualLines = FileUtils.readLines(actual);
            for (int i = 0 ; i < expectedLines.size() ; ++i) {
                String expectedLine = expectedLines.get(i);
                String actualLine = actualLines.get(i);
                assertEquals("Line in file " + fileName + " did not match ", expectedLine, actualLine);
            }
            
        }
        
    }

}
