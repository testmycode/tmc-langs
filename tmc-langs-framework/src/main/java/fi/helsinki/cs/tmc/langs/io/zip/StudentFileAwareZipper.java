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

/**
 * This {@link Zipper} implementation recursively zips the files of a directory.
 * Only files considered to be student files by the specified {@link StudentFilePolicy}
 * are included in the archive.
 *
 * <p>The {@link StudentFilePolicy} provided either via the
 * {@link #StudentFileAwareZipper(StudentFilePolicy) constructor} or a
 * {@link #setStudentFilePolicy(StudentFilePolicy) setter method} is used to determine whether
 * a file or directory should be included in the archive.</p>
 *
 * <p>Individual directories can be excluded from archival by adding a file in the directory
 * root with the name of {@code .tmcnosubmit}. For example, if the folder {@code sensitive/}
 * should be excluded, the file {@code sensitive/.tmcnosubmit} should be created. The contents
 * of the file are ignored.</p>
 *
 * <p>File system roots (e.g. {@code /} on *nix and {@code C:\} on Windows platforms) cannot
 * be zipped, as this {@link Zipper} includes the specified {@code rootDirectory}
 * in the zip file as a parent directory.</p>
 */
public final class StudentFileAwareZipper implements Zipper {

    private static final Logger log = LoggerFactory.getLogger(StudentFileAwareZipper.class);
    // The zip standard mandates the forward slash "/" to be used as path separator
    private static final char ZIP_SEPARATOR = '/';

    private StudentFilePolicy filePolicy;

    /**
     * Instantiates a new {@link StudentFileAwareZipper} without a {@link StudentFilePolicy}.
     * The {@link #setStudentFilePolicy(StudentFilePolicy)} method can be used to set the policy.
     */
    public StudentFileAwareZipper() {}

    /**
     * Instantiates a new {@link StudentFileAwareZipper} with the
     * specified {@link StudentFilePolicy}.
     *
     * @param filePolicy Determines which files and directories are included in the archive.
     * @see #setStudentFilePolicy(StudentFilePolicy)
     */
    public StudentFileAwareZipper(StudentFilePolicy filePolicy) {
        this.filePolicy = filePolicy;
    }

    /**
     * Sets the {@link StudentFilePolicy} which determines which files and directories
     * are included in the archive.
     *
     * @param studentFilePolicy Determines which files and directories are included in the archive.
     * @see #StudentFileAwareZipper(StudentFilePolicy)
     */
    @Override
    public void setStudentFilePolicy(StudentFilePolicy studentFilePolicy) {
        this.filePolicy = studentFilePolicy;
    }

    /**
     * Recursively zips all files and directories which are considered to be student files.
     *
     * @param rootDirectory The root directory of the files and directories to zip.
     *                      Included in the archive. Cannot be a file system root.
     * @return Byte array containing the bytes of the {@link ZipArchiveOutputStream}.
     * @throws IOException if reading a file or directory fails.
     * @throws IllegalArgumentException if attempting to zip a file system root.
     * @see #setStudentFilePolicy(StudentFilePolicy)
     */
    @Override
    public byte[] zip(Path rootDirectory) throws IOException {
        log.debug("Starting to zip {}", rootDirectory);

        if (!Files.exists(rootDirectory)) {
            log.error("Attempted to zip nonexistent directory \"{}\"", rootDirectory);
            throw new FileNotFoundException("Attempted to zip nonexistent directory");
        }

        if (rootDirectory.toAbsolutePath().getNameCount() == 0) {
            // getNameCount returns 0 if the path only represents a root component
            log.error("Attempted to zip a root \"{}\"", rootDirectory);
            throw new IllegalArgumentException("Filesystem root zipping is not supported");
        }

        if (filePolicy == null) {
            log.error("Attepted to zip before setting the filePolicy");
            throw new IllegalStateException(
                    "The student file policy must be set before zipping files");
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zipStream = new ZipArchiveOutputStream(buffer)) {
            zipRecursively(rootDirectory, zipStream, rootDirectory);
            zipStream.finish();
        }

        return buffer.toByteArray();
    }

    private void zipRecursively(
            Path currentPath, ZipArchiveOutputStream zipStream, Path projectRoot)
            throws IOException {

        log.trace("Processing {}", currentPath);

        boolean studentFile = filePolicy.isStudentFile(currentPath, projectRoot);
        boolean isDirectory = Files.isDirectory(currentPath);
        if (studentFile || isDirectory) {
            log.trace("{} is student file", currentPath);

            if (isExplicitlyIgnoredDirectory(currentPath)) {
                log.trace("{} contains a .tmcnosubmit file, ignoring this folder", currentPath);
                return;
            }

            if (studentFile) {
                writeToZip(currentPath, zipStream, projectRoot);
            }

            if (isDirectory) {
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

        log.trace(
                "Found directory {} while zipping, checking children for .tmcnosubmit",
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

        Path relativePath = projectPath.getParent().relativize(currentPath);
        String name = relativePathToZipCompliantName(relativePath, Files.isDirectory(currentPath));

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

    private static String relativePathToZipCompliantName(Path path, boolean isDirectory) {
        log.trace("Generating zip-compliant filename from Path \"{}\", isDirectory: {}",
                path, isDirectory);

        StringBuilder sb = new StringBuilder();
        for (Path part : path) {
            sb.append(part);
            sb.append(ZIP_SEPARATOR);
        }

        if (!isDirectory) {
            // ZipArchiveEntry assumes the entry represents a directory if and only
            // if the name ends with a forward slash "/". Remove the trailing slash
            // because this wasn't a directory.
            log.trace("Path wasn't a directory, removing trailing slash");
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }
}
