package fi.helsinki.cs.tmc.langs.util;

import fi.helsinki.cs.tmc.langs.LanguagePlugin;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.ExerciseBuilder;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.ExercisePackagingConfiguration;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.io.EverythingIsStudentFileStudentFilePolicy;
import fi.helsinki.cs.tmc.langs.io.NothingIsStudentFileStudentFilePolicy;
import fi.helsinki.cs.tmc.langs.io.zip.StudentFileAwareUnzipper;
import fi.helsinki.cs.tmc.langs.io.zip.Unzipper;
import fi.helsinki.cs.tmc.langs.util.tarservice.TarCreator;

import com.google.common.base.Optional;

import org.apache.commons.compress.archivers.ArchiveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

public class TaskExecutorImpl implements TaskExecutor {

    private static final Logger log = LoggerFactory.getLogger(TaskExecutorImpl.class);

    @Override
    public ValidationResult runCheckCodeStyle(Path path, Locale locale)
            throws NoLanguagePluginFoundException {
        return getLanguagePlugin(path).checkCodeStyle(path, locale);
    }

    @Override
    public RunResult runTests(Path path) throws NoLanguagePluginFoundException {
        return getLanguagePlugin(path).runTests(path);
    }

    @Override
    public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName)
            throws NoLanguagePluginFoundException {
        Optional<ExerciseDesc> result = getLanguagePlugin(path).scanExercise(path, exerciseName);

        if (result.isPresent()) {
            return result;
        }

        return Optional.absent();
    }

    @Override
    public boolean isExerciseRootDirectory(Path path) {
        for (ProjectType projectType : ProjectType.values()) {
            if (projectType.getLanguagePlugin().isExerciseTypeCorrect(path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void extractProject(Path compressedProject, Path targetLocation) throws IOException {
        try {
            LanguagePlugin languagePlugin = getLanguagePlugin(targetLocation);
            languagePlugin.extractProject(compressedProject, targetLocation);
        } catch (NoLanguagePluginFoundException e) {
            StudentFileAwareUnzipper unzipper =
                    new StudentFileAwareUnzipper(new EverythingIsStudentFileStudentFilePolicy());
            unzipper.unzip(compressedProject, targetLocation);
        }
    }

    @Override
    public void extractProject(
            Path compressedProject, Path targetLocation, boolean overwriteEverything)
            throws IOException {
        if (overwriteEverything) {
            StudentFileAwareUnzipper unzipper =
                    new StudentFileAwareUnzipper(new NothingIsStudentFileStudentFilePolicy());
            unzipper.unzip(compressedProject, targetLocation);
        } else {
            extractProject(compressedProject, targetLocation);
        }
    }

    @Override
    public void extractAndRewriteEveryhing(Path compressedProject, Path targetLocation)
            throws IOException {
        Unzipper unzipper =
                new StudentFileAwareUnzipper(new NothingIsStudentFileStudentFilePolicy());
        unzipper.unzip(compressedProject, targetLocation);
    }

    @Override
    public void prepareStubs(Map<Path, LanguagePlugin> exerciseMap, Path repoPath, Path destPath)
            throws NoLanguagePluginFoundException {
        new ExerciseBuilder().prepareStubs(exerciseMap, repoPath, destPath);
    }

    @Override
    public void prepareSolutions(
            Map<Path, LanguagePlugin> exerciseMap, Path repoPath, Path destPath)
            throws NoLanguagePluginFoundException {
        new ExerciseBuilder().prepareSolutions(exerciseMap, repoPath, destPath);
    }

    @Override
    public byte[] compressProject(Path path) throws NoLanguagePluginFoundException, IOException {
        return getLanguagePlugin(path).compressProject(path);
    }

    @Override
    public ExercisePackagingConfiguration getExercisePackagingConfiguration(Path path)
            throws NoLanguagePluginFoundException {
        return getLanguagePlugin(path).getExercisePackagingConfiguration(path);
    }

    @Override
    public void compressTarForSubmitting(Path projectDir, Path tmcLangs,
            Path tmcrun, Path targetLocation) throws IOException, ArchiveException {
        TarCreator tarCompresser = new TarCreator();
        log.info("Copying files to directory " + projectDir.toString() + " and creating tar ball");
        tarCompresser.createTarFromProject(projectDir, tmcLangs, tmcrun, targetLocation);
    }

    @Override
    public void clean(Path path) throws NoLanguagePluginFoundException {
        getLanguagePlugin(path).clean(path);
    }

    /**
     * Get language plugin for the given path.
     *
     * @param path of the exercise.
     * @return Language Plugin that recognises the exercise.
     */
    private LanguagePlugin getLanguagePlugin(Path path) throws NoLanguagePluginFoundException {
        return ProjectType.getProjectType(path).getLanguagePlugin();
    }
}
