package fi.helsinki.cs.tmc.langs.io.zip;

import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;

import com.google.common.collect.Sets;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Set;
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
    public UnzipResult unzip(Path zip, Path target) throws IOException {
        UnzipResult result = new UnzipResult(target);

        log.info("Unzipping {} to {}", zip, target);
        if (!Files.exists(zip)) {
            log.error("Attempted to unzip nonexistent archive {}", zip);
            throw new FileNotFoundException("Attempted to unzip nonexistent archive");
        }

        if (!Files.exists(target)) {
            log.debug("Unzip target {} does not exist, creating folder structure", target);
            Files.createDirectories(target);
        }

        Set<Path> pathsInZip = Sets.newHashSet();
        try (ZipFile zipFile = new ZipFile(zip.toFile())) {

            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();

            String projectDirInZip = findProjectDirInZip(zipFile.getEntries());

            log.debug("Project dir in zip: {}", projectDirInZip);

            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();

                if (entry.getName().startsWith(projectDirInZip)) {
                    String restOfPath =
                            trimSlashes(entry.getName().substring(projectDirInZip.length()));

                    Path entryTargetPath =
                            target.resolve(trimSlashes(restOfPath.replace("/", File.separator)));
                    pathsInZip.add(entryTargetPath);

                    log.debug(
                            "Processing zipEntry with name {} to {}",
                            entry.getName(),
                            entryTargetPath);
                    if (entry.isDirectory() || entryTargetPath.toFile().isDirectory()) {
                        Files.createDirectories(entryTargetPath);
                        log.debug(
                                "{} is a directory - creating and off to the next file ",
                                entry.getName());
                        continue;
                    }
                    boolean shouldWrite;
                    InputStream entryContent = zipFile.getInputStream(entry);
                    byte[] entryData = IOUtils.toByteArray(entryContent);
                    if (Files.exists(entryTargetPath)) {
                        log.trace("Allowed to unzip, unzipping");

                        if (fileContentEquals(target.toFile(), entryData)) {
                            shouldWrite = false;
                            result.unchangedFiles.add(entryTargetPath);
                        } else if (allowedToUnzip(entryTargetPath, target)) {
                            shouldWrite = true;
                            result.overwrittenFiles.add(entryTargetPath);
                        } else {
                            shouldWrite = false;
                            result.skippedFiles.add(entryTargetPath);
                        }
                    } else {
                        shouldWrite = true;
                        result.newFiles.add(entryTargetPath);
                    }
                    if (shouldWrite) {
                        FileUtils.writeByteArrayToFile(entryTargetPath.toFile(), entryData);
                    } else {
                        log.trace("Not allowed to unzip, skipping file");
                        result.skippedFiles.add(entryTargetPath);
                    }
                    log.debug("Done with file {}", entryTargetPath);

                } else {
                    log.debug("Skipping non project file from zip - {}", entry.getName());
                }
            }
        }

        log.debug("Done unzipping");
        deleteFilesNotInZip(target, target, result, pathsInZip);
        return null;
    }

    // TODO: validate
    private void deleteFilesNotInZip(
            Path projectDir, Path curDir, UnzipResult result, Set<Path> pathsInZip)
            throws IOException {

        for (File file : curDir.toFile().listFiles()) {
            Path filePath = file.toPath();
            if (file.isDirectory()) {
                deleteFilesNotInZip(projectDir, file.toPath(), result, pathsInZip);
            }

            if (!pathsInZip.contains(filePath)) {
                if (mayDelete(filePath, null)) {
                    if (file.isDirectory() && file.listFiles().length > 0) {
                        // Won't delete directories if they still have contents
                        result.skippedDeletingFiles.add(filePath);
                    } else {
                        file.delete();
                        result.deletedFiles.add(filePath);
                    }
                } else {
                    result.skippedDeletingFiles.add(filePath);
                }
            }
        }
    }

    private boolean fileContentEquals(File file, byte[] data) throws IOException {
        if (file.isDirectory()) {
            return false;
        }
        InputStream fileIs = new BufferedInputStream(new FileInputStream(file));
        InputStream dataIs = new ByteArrayInputStream(data);
        boolean eq = IOUtils.contentEquals(fileIs, dataIs);
        fileIs.close();
        dataIs.close();
        return eq;
    }

    private String findProjectDirInZip(Enumeration<ZipArchiveEntry> zipEntries) throws IOException {
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
        throw new RuntimeException("No project in zip");
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

    private boolean mayDelete(Path file, Path projectRoot) {
        if (!Files.exists(file)) {
            log.trace("File does not exist, don't delete it");
            return false;
        }

        log.trace("File exists, checking whether it's studentfile is allowed");

        if (filePolicy.mayDelete(file, projectRoot)) {
            log.trace("File {} can be deleted", file);
            return true;
        }
        return false;
    }
}
