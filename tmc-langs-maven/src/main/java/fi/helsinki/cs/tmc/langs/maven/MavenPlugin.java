package fi.helsinki.cs.tmc.langs.maven;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.helsinki.cs.tmc.langs.*;
import org.apache.maven.cli.MavenCli;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MavenPlugin extends AbstractLanguagePlugin {

    @Override
    protected boolean isExerciseTypeCorrect(Path path) {
        return new File(path.toString() + File.separatorChar + "pom.xml").exists();
    }

    @Override
    public String getLanguageName() {
        return "apache-maven";
    }

    @Override
    public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RunResult runTests(Path path) {
        CompileResult compileResult = buildMaven(path);

        if (compileResult.compileResult != 0) {
            Map<String, byte[]> logs = new HashMap<>();
            logs.put(SpecialLogs.STDOUT, compileResult.output.toByteArray());
            logs.put(SpecialLogs.STDERR, compileResult.err.toByteArray());

            return new RunResult(RunResult.Status.COMPILE_FAILED, ImmutableList.copyOf(new ArrayList<TestResult>()),
                    ImmutableMap.copyOf(logs));
        }

        return null;
    }

    protected CompileResult buildMaven(Path path) {
        MavenCli maven = new MavenCli();
        String[] args = {"clean", "compile"};

        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuf = new ByteArrayOutputStream();

        int compileResult = maven.doMain(args, path.toAbsolutePath().toString(), new PrintStream(outBuf), new PrintStream(errBuf));

        return new CompileResult(compileResult, outBuf, errBuf);
    }

    protected class CompileResult {
        public CompileResult(int compileResult, ByteArrayOutputStream output, ByteArrayOutputStream err) {
            this.compileResult = compileResult;
            this.output = output;
            this.err = err;
        }

        public int compileResult;
        public ByteArrayOutputStream output;
        public ByteArrayOutputStream err;
    }

}
