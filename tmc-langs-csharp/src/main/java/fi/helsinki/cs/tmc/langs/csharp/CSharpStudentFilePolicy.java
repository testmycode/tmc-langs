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
        if (this.isChildOfBinaryDir(path)) {
            return false;
        }

        return path.startsWith(Paths.get("src"));
    }

    private boolean isChildOfBinaryDir(Path path) {
        for (Path testPath : path) {
            Path fileName = testPath.getFileName();

            if (fileName.equals(Paths.get("bin"))
                    || fileName.equals(Paths.get("obj"))) {
                return true;
            }
        }

        return false;
    }
}
