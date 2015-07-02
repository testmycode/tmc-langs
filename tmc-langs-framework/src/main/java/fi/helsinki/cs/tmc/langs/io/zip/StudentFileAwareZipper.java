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

public class StudentFileAwareZipper implements Zipper {

    private Logger log = LoggerFactory.getLogger(StudentFileAwareZipper.class);
    private StudentFilePolicy filePolicy;

    public StudentFileAwareZipper() { }

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
        ZipArchiveOutputStream zipStream = new ZipArchiveOutputStream(buffer);

        zipRecursively(rootDirectory, zipStream, rootDirectory.getParent());

        zipStream.finish();
        zipStream.close();

        return buffer.toByteArray();
    }


    private void zipRecursively(Path currentPath,
                                ZipArchiveOutputStream zipStream,
                                Path zipParent) throws IOException {

        log.trace("Processing {}", currentPath);

        if (filePolicy.isStudentFile(currentPath, zipParent)) {
            log.trace("{} is student file", currentPath);

            writeToZip(currentPath, zipStream, zipParent);

            if (Files.isDirectory(currentPath)) {
                log.trace("Recursing to zip contents of {}", currentPath);
                try (DirectoryStream<Path> directory = Files.newDirectoryStream(currentPath)) {
                    for (Path child : directory) {
                        zipRecursively(child, zipStream, zipParent);
                    }
                } catch (IOException exception) {
                    log.error("Exception while attempting to zip contents of {}", currentPath);
                    throw new IOException("Unable to zip contents of " + currentPath, exception);
                }
            }
        }
    }

    private void writeToZip(Path currentPath,
                            ZipArchiveOutputStream zipStream,
                            Path zipParent) throws IOException {

        log.trace("Writing {} to zip", currentPath);

        String name = zipParent.relativize(currentPath).toString();

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
