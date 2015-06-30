package fi.helsinki.cs.tmc.langs.io.zip;

import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class StudentFileAwareUnzipper extends ZipProcessor implements Unzipper{

    private Logger log = LoggerFactory.getLogger(StudentFileAwareUnzipper.class);
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

        ZipInputStream zipStream = new ZipInputStream(new FileInputStream(zip.toFile()));
        ZipEntry zipEntry = zipStream.getNextEntry();
        while (zipEntry != null) {
            Path entryPath = target.resolve(zipEntry.getName());

            log.debug("Processing zipEntry with name {} to {}", zipEntry.getName(), entryPath);
            if (zipEntry.getName().endsWith(File.separator)) {
                Files.createDirectories(entryPath);
            } else {
                if (allowedToUnzip(entryPath, target)) {
                    log.trace("Allowed to unzip, unzipping");
                    unzipFile(zipStream, entryPath);
                } else {
                    log.trace("Not allowed to unzip, skipping file");
                }
            }


            log.debug("Done with file {}", entryPath);
            zipEntry = zipStream.getNextEntry();
        }

        log.debug("Done unzipping");

        zipStream.closeEntry();
        log.debug("Closed last entry");

        zipStream.close();
        log.debug("Closed zip stream");
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

    private void unzipFile(ZipInputStream zipStream, Path entryPath) throws IOException {
        log.debug("Unzipping file to {}", entryPath);

        if (!Files.exists(entryPath.getParent())) {
            log.trace("Parent directory does not exist, creating");
            Files.createDirectories(entryPath.getParent());
        } else {
            log.trace("Parent directory exists");
        }

        log.trace("Creating file {}", entryPath);
        FileOutputStream fileStream = new FileOutputStream(entryPath.toFile(), false);
        copyBytes(zipStream, fileStream);

        log.trace("Closing file stream to {}", entryPath);
        fileStream.close();
    }
}
