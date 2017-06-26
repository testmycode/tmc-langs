package fi.helsinki.cs.tmc.langs.util.tarservice;

import static org.apache.commons.compress.archivers.tar.TarArchiveOutputStream.LONGFILE_POSIX;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TarCreator {

    /**
     * Copies the location of tmc-langs-cli and tmcrun to unzipped project folder
     * and creates a tarball.
     *
     * @param projectDir     Location of unzipped project dir
     * @param tmcLangs       Location of tmc-langs-cli.jar
     * @param tmcrun         Location of tmc-run init script
     * @param targetLocation Location where the tar archive should be extracted to
     * @throws IOException      Error!
     * @throws ArchiveException Error!
     */
    public void createTarFromProject(Path projectDir, Path tmcLangs, Path tmcrun,
                                     Path targetLocation) throws IOException, ArchiveException {
        Files.copy(tmcrun, projectDir.resolve(tmcrun.getFileName()));
        Files.copy(tmcLangs, projectDir.resolve(tmcLangs.getFileName()));
        createTarBall(projectDir, targetLocation);
    }

    /**
     * Creates a tarball from a directory.
     *
     * @param project        Project directory file
     * @param targetLocation Location where the tar archive should be extracted to
     * @throws IOException      Error!
     * @throws ArchiveException Error!
     */
    private void createTarBall(Path project, Path targetLocation)
            throws IOException, ArchiveException {
        try (FileOutputStream tarOut = new FileOutputStream(targetLocation.toString())) {
            try (TarArchiveOutputStream aos = new TarArchiveOutputStream(tarOut)) {
                aos.setLongFileMode(LONGFILE_POSIX);
                addFilesToTarBall(project, aos, project);
                aos.finish();
            }
        }
    }

    /**
     * Adds all files and folders inside a folder to a tar file.
     *
     * @param folder       The folder to add
     * @param tar          TarArchiveOutputStreamer tar
     * @param lengthOfPath The length of String from root until the start folder.
     * @throws FileNotFoundException Error!
     * @throws IOException           Error!
     */
    private void addFilesToTarBall(Path folder, TarArchiveOutputStream tar,
                                   Path basePath) throws FileNotFoundException, IOException {
        for (Path path : Files.newDirectoryStream(folder)) {
            if (Files.isDirectory(path)) {
                addFilesToTarBall(path, tar, basePath);
            } else {
                TarArchiveEntry entry = new TarArchiveEntry(basePath.relativize(path).toString());
                entry.setSize(path.toFile().length());
                tar.putArchiveEntry(entry);
                try (FileInputStream fis = new FileInputStream(path.toFile())) {
                    IOUtils.copy(fis, tar);
                }
                tar.closeArchiveEntry();
            }
        }
    }
}
