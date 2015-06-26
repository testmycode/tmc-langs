package fi.helsinki.cs.tmc.langs.io.sandbox;

import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;

import java.nio.file.Path;

public interface SubmissionProcessor {

    void setStudentFilePolicy(StudentFilePolicy studentFilePolicy);

    void moveFiles(Path source, Path target);
}
