package fi.helsinki.cs.tmc.langs.maven;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.shared.invoker.InvocationOutputHandler;

public class MavenOutputLogger implements InvocationOutputHandler {

    List<String> lines = new ArrayList<>();

    @Override
    public void consumeLine(String string) {
        lines.add(string);
    }

    public List<String> getLines() {
        return lines;
    }
    
    public byte[] toByteArray() {
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            builder.append(line);
            builder.append('\n');
        }
        
        return builder.toString().getBytes();
    }
}
