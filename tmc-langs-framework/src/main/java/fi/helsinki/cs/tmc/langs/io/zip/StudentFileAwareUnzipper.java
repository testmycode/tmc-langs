package fi.helsinki.cs.tmc.langs.io.zip;

import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;

public final class StudentFileAwareUnzipper implements Unzipper {

    private static final Logger log = LoggerFactory.getLogger(StudentFileAwareUnzipper.class);

    private StudentFilePolicy filePolicy;

    public StudentFileAwareUnzipper() {}

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

            String projectDirInZip = findProjectDirInZip(zipFile.getEntries());
            if (projectDirInZip == null) {
                throw new RuntimeException("No project in zip");
            }
            log.debug("Project dir in zip: {}", projectDirInZip);

            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();

                if (entry.getName().startsWith(projectDirInZip)) {
                    String restOfPath = entry.getName().substring(projectDirInZip.length());
                    restOfPath = trimSlashes(restOfPath);

                    String destFileRelativePath =
                            trimSlashes(restOfPath.replace("/", File.separator));
                    Path entryTargetPath = target.resolve(destFileRelativePath);

                    log.debug(
                            "Processing zipEntry with name {} to {}",
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
                } else {
                    log.debug("Skipping non project file from zip - {}", entry.getName());
                }
            }
        }
        log.debug("Done unzipping");
    }

    private String findProjectDirInZip(Enumeration<ZipArchiveEntry> zipEntries) throws IOException {
        ZipEntry zent;
        while (zipEntries.hasMoreElements()) {
            ZipArchiveEntry element = zipEntries.nextElement();
            String name = element.getName();

            // TODO: detect project root with util?
            if (name.endsWith("/nbproject/")
                    || name.endsWith("/pom.xml")
                    || name.endsWith("Makefike")
                    || name.endsWith("/src/")) {
                return dirname(name);
            }
        }
        return null;
    }

    private String dirname(String zipPath) {
        while (zipPath.endsWith("/")) {
            zipPath = zipPath.substring(0, zipPath.length() - 1);
        }
        return zipPath.replaceAll("/[^/]+$", "");
    }

    private String trimSlashes(String str) {
        while (str.startsWith("/") || str.startsWith(File.separator)) {
            str = str.substring(1);
        }
        while (str.endsWith("/") || str.startsWith(File.separator)) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
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
