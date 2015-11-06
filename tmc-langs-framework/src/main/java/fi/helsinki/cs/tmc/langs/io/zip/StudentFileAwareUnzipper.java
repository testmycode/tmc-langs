package fi.helsinki.cs.tmc.langs.io.zip;

import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;

public final class StudentFileAwareUnzipper implements Unzipper {

    private static final Logger log = LoggerFactory.getLogger(StudentFileAwareUnzipper.class);

    private StudentFilePolicy filePolicy;

    public StudentFileAwareUnzipper() { }

    public StudentFileAwareUnzipper(StudentFilePolicy filePolicy) {
        this.filePolicy = filePolicy;
    }

    @Override
    public void setStudentFilePolicy(StudentFilePolicy studentFilePolicy) {
        this.filePolicy = studentFilePolicy;
    }

    @Override
    public void unzip(Path zip, Path target) throws IOException {
        log.info("Unzipping {} to {}", zip, target);
        if (!Files.exists(zip)) {
            log.error("Attempted to unzip nonexistent archive {}", zip);
            throw new FileNotFoundException("Attempted to unzip nonexistent archive");
        }

        if (!Files.exists(target)) {
            log.debug("Unzip target {} does not exist, creating folder structure", target);
            Files.createDirectories(target);
        }

        try (ZipFile zipFile = new ZipFile(zip.toFile())) {
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                Path entryTargetPath = target.resolve(entry.getName());

                log.debug("Processing zipEntry with name {} to {}",
                        entry.getName(),
                        entryTargetPath);
                if (entry.isDirectory()) {
                    Files.createDirectories(entryTargetPath);
                } else {
                    if (allowedToUnzip(entryTargetPath, target)) {
                        log.trace("Allowed to unzip, unzipping");
                        InputStream entryContent = zipFile.getInputStream(entry);
                        FileUtils.copyInputStreamToFile(entryContent, entryTargetPath.toFile());
                    } else {
                        log.trace("Not allowed to unzip, skipping file");
                    }
                }

                log.debug("Done with file {}", entryTargetPath);
            }
        }
        log.debug("Done unzipping");
    }

    private boolean allowedToUnzip(Path file, Path projectRoot) {
        if (!Files.exists(file)) {
            log.trace("File does not exist, allowing unzipping");
            return true;
        }

        log.trace("File exists, checking whether overwriting is allowed");

        if (filePolicy.isStudentFile(file, projectRoot)) {
            log.trace("File is student file, do not allow to overwrite");
            return false;
        }

        log.trace("File is not a student file, allow overwriting");

        return true;
    }
}
