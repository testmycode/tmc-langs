package fi.helsinki.cs.tmc.langs.maven;

import fi.helsinki.cs.tmc.langs.ClassPath;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

public class MavenClassPathBuilder {
    
    private static final String CLASS_PATH_GOAL = "org.apache.maven.plugins:maven-dependency-plugin:2.10:build-classpath";
    private static final String OUTPUT_FILE_PARAM_PREFIX = "-Dmdep.outputFile=";
    
    public static ClassPath fromProjectBasePath(Path projectPath) throws IOException, MavenInvocationException {
        
        MavenOutputLogger err = new MavenOutputLogger();
        MavenOutputLogger out = new MavenOutputLogger();
        
        File outputFile = File.createTempFile("tmc-classpath", ".tmp");
        
        String outputParameter = OUTPUT_FILE_PARAM_PREFIX + outputFile.getAbsolutePath();
        
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile( new File(projectPath.toFile(), "pom.xml") );
        request.setGoals(new ArrayList<>(Arrays.asList(CLASS_PATH_GOAL, outputParameter)));
        request.setErrorHandler(err);
        request.setOutputHandler(out);
        request.setShowErrors(true);
        
        Invoker invoker = new DefaultInvoker();
        invoker.execute(request);
        
        Scanner scanner = new Scanner(outputFile);
        String classPathString = scanner.nextLine();
        
        outputFile.delete();
        
        ClassPath classPath = new ClassPath();
        for (String part : classPathString.split(File.pathSeparator)) {
            classPath.add(Paths.get(part));
        }
        
        return classPath;
    }
}
