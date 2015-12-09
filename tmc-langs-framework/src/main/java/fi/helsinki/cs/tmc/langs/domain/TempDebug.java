/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.helsinki.cs.tmc.langs.domain;

import com.google.common.collect.ImmutableMap;
import fi.helsinki.cs.tmc.langs.LanguagePlugin;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author baobab
 */
public class TempDebug {
    
    public static void main(String[] args) throws IOException {
        ExerciseBuilder exerciseBuilder = new ExerciseBuilder();
        Path originProject = Paths.get("src", "test", "resources", "arith_funcs");
        Path tempDir = Files.createTempDirectory("tmc-langs");
        tempDir.toFile().deleteOnExit();

        tempDir.resolve("clone").toFile().mkdirs();
        tempDir.resolve("stub").toFile().mkdirs();

        final Path cloneDir = tempDir.resolve(Paths.get("clone"));
        final Path stubDir = tempDir.resolve(Paths.get("stub"));

        FileUtils.copyDirectory(originProject.toFile(), cloneDir.resolve("arith_funcs").toFile());

//        LanguagePlugin languagePlugin = new LanguagePlugin() ;
//        final Map<Path, LanguagePlugin> exerciseMap =
//                ImmutableMap.of(cloneDir.resolve("arith_funcs"), languagePlugin);

//        exerciseBuilder.prepareStubs(exerciseMap, cloneDir, stubDir);
//
//        Path expected = Paths.get("src", "test", "resources", "arith_funcs_stub");
//        assertFileLines(expected, stubDir.resolve("arith_funcs"));
    }
    
}
