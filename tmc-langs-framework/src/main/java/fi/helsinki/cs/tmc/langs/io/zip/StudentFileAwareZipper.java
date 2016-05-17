package fi.helsinki.cs.tmc.langs.io.zip;

import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class StudentFileAwareZipper implements Zipper {

    private static final Logger log = LoggerFactory.getLogger(StudentFileAwareZipper.class);
    private StudentFilePolicy filePolicy;

    public StudentFileAwareZipper() {}

    public StudentFileAwareZipper(StudentFilePolicy filePolicy) {
        this.filePolicy = filePolicy;
    }

    @Override
    public void setStudentFilePolicy(StudentFilePolicy studentFilePolicy) {
        this.filePolicy = studentFilePolicy;
    }

    @Override
    public byte[] zip(Path rootDirectory) throws IOException {
        log.debug("Starting to zip {}", rootDirectory);

        if (!Files.exists(rootDirectory)) {
            log.error("Attempted to zip nonexistent directory {}", rootDirectory);
            throw new FileNotFoundException("Attempted to zip nonexistent directory");
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zipStream = new ZipArchiveOutputStream(buffer)) {
            zipRecursively(rootDirectory, zipStream, rootDirectory);
            zipStream.finish();
        }

        return buffer.toByteArray();
    }

    private void zipRecursively(Path currentPath, ZipArchiveOutputStream zipStream, Path projectRoot)
            throws IOException {

        log.trace("Processing {}", currentPath);

        if (filePolicy.isStudentFile(currentPath, projectRoot)) {
            log.trace("{} is student file", currentPath);

            if (isExplicitlyIgnoredDirectory(currentPath)) {
                log.trace("{} contains a .tmcnosubmit file, ignoring this folder", currentPath);
                return;
            }

            writeToZip(currentPath, zipStream, projectRoot);

            if (Files.isDirectory(currentPath)) {
                log.trace("Recursing to zip contents of {}", currentPath);
                try (DirectoryStream<Path> directory = Files.newDirectoryStream(currentPath)) {
                    for (Path child : directory) {
                        zipRecursively(child, zipStream, projectRoot);
                    }
                } catch (IOException exception) {
                    log.error("Exception while attempting to zip contents of {}", currentPath);
                    throw new IOException("Unable to zip contents of " + currentPath, exception);
                }
            }
        }
    }

    private boolean isExplicitlyIgnoredDirectory(Path currentPath) throws IOException {
        if (!Files.isDirectory(currentPath)) {
            return false;
        }

        log.trace("Found directory {} while zipping, checking children for .tmcnosubmit",
                currentPath);

        try (DirectoryStream<Path> directory = Files.newDirectoryStream(currentPath)) {
            for (Path child : directory) {
                if (child.getFileName().toString().equals(".tmcnosubmit")) {
                    log.trace("Detected {} as .tmcnosubmit", child);
                    return true;
                }
            }
        } catch (IOException ex) {
            log.error("Exception while checking for .tmcnosubmit", ex);
            throw new IOException("Exception while checking for .tmcnosubmit", ex);
        }
        log.trace("Found no .tmcnosubmit in {}", currentPath);
        return false;
    }

    private void writeToZip(Path currentPath, ZipArchiveOutputStream zipStream, Path projectPath)
            throws IOException {

        log.trace("Writing {} to zip", currentPath);


        String name = projectPath.getParent().relativize(currentPath).toString();

        if (Files.isDirectory(currentPath)) {
            log.trace("{} is a directory", currentPath);
            // Must be "/", can not be replaces with File.separator
            name += "/";
        }

        ZipArchiveEntry entry = new ZipArchiveEntry(name);
        zipStream.putArchiveEntry(entry);

        if (Files.isRegularFile(currentPath)) {
            log.trace("{} is a regular file, copying bytes", currentPath);
            FileUtils.copyFile(currentPath.toFile(), zipStream);
            log.trace("Done copying bytes");
        }

        log.trace("Closing entry");
        zipStream.closeArchiveEntry();
    }
}
