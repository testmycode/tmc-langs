package fi.helsinki.cs.tmc.langs.io.zip;

import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;

import java.io.IOException;
import java.nio.file.Path;

public interface Zipper {

    byte[] zip(Path rootDirectory) throws IOException;

    void setStudentFilePolicy(StudentFilePolicy studentFilePolicy);
}
