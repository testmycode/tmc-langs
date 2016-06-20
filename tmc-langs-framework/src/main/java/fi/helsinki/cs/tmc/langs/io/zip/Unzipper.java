package fi.helsinki.cs.tmc.langs.io.zip;

import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;

import java.io.IOException;
import java.nio.file.Path;

public interface Unzipper {

    UnzipResult unzip(Path zipFile, Path target) throws IOException;

    void setStudentFilePolicy(StudentFilePolicy studentFilePolicy);
}
