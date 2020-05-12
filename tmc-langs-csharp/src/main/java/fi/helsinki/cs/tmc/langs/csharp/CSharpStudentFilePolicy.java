package fi.helsinki.cs.tmc.langs.csharp;

import fi.helsinki.cs.tmc.langs.io.ConfigurableStudentFilePolicy;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CSharpStudentFilePolicy extends ConfigurableStudentFilePolicy {

    public CSharpStudentFilePolicy(Path configFileParent) {
        super(configFileParent);
    }

    @Override
    public boolean isStudentSourceFile(Path path, Path projectRootPath) {
        return path.startsWith(Paths.get("src"));
    }
    
}
