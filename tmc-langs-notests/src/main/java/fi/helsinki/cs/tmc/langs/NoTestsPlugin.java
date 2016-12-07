
package fi.helsinki.cs.tmc.langs;

import fi.helsinki.cs.tmc.langs.abstraction.Strategy;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationError;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.ExerciseBuilder;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.TestDesc;
import fi.helsinki.cs.tmc.langs.domain.TestResult;
import fi.helsinki.cs.tmc.langs.io.EverythingIsStudentFileStudentFilePolicy;
import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.io.sandbox.StudentFileAwareSubmissionProcessor;
import fi.helsinki.cs.tmc.langs.io.sandbox.SubmissionProcessor;
import fi.helsinki.cs.tmc.langs.io.zip.StudentFileAwareUnzipper;
import fi.helsinki.cs.tmc.langs.io.zip.StudentFileAwareZipper;
import fi.helsinki.cs.tmc.langs.io.zip.Unzipper;
import fi.helsinki.cs.tmc.langs.io.zip.Zipper;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Plugin for projects with no tests. Gets points and exercise_name from {@code no-tests.points}
 * and {@code no-tests.exercise_name} respectfully from {@code .tmcproject.yml} configuration file.
 */
public class NoTestsPlugin extends AbstractLanguagePlugin {

    private static final Logger log = LoggerFactory.getLogger(NoTestsPlugin.class);

    public NoTestsPlugin() {
        super(
                new ExerciseBuilder(),
                new StudentFileAwareSubmissionProcessor(),
                new StudentFileAwareZipper(),
                new StudentFileAwareUnzipper());
    }

    public NoTestsPlugin(
            ExerciseBuilder exerciseBuilder,
            SubmissionProcessor submissionProcessor,
            Zipper zipper,
            Unzipper unzipper) {
        super(exerciseBuilder, submissionProcessor, zipper, unzipper);
    }

    @Override
    public boolean isExerciseTypeCorrect(Path path) {
        return getConfiguration(path).isSet("no-tests");
    }

    @Override
    protected StudentFilePolicy getStudentFilePolicy(Path projectPath) {
        return new EverythingIsStudentFileStudentFilePolicy();
    }

    @Override
    public String getPluginName() {
        return "No-Tests";
    }

    @Override
    public Optional<ExerciseDesc> scanExercise(Path path, String exerciseName) {
        TestDesc test = new TestDesc(exerciseName + "Test", getPoints(path));
        return Optional.of(
                new ExerciseDesc(exerciseName, ImmutableList.<TestDesc>of(test)));
    }

    private ImmutableList<String> getPoints(Path path) {
        if (!getConfiguration(path).isSet("no-tests.points")) {
            return ImmutableList.<String>of();
        }
        return ImmutableList.copyOf(getConfiguration(path).get("no-tests.points").asList());
    }

    @Override
    public RunResult runTests(Path path) {
        TestResult fakeResult =
                new TestResult(
                        "Default test", true, getPoints(path), "", ImmutableList.<String>of());
        return new RunResult(
                RunResult.Status.PASSED,
                ImmutableList.<TestResult>of(fakeResult),
                ImmutableMap.<String, byte[]>of());
    }

    @Override
    public ValidationResult checkCodeStyle(Path path, Locale messageLocale)
            throws UnsupportedOperationException {
        return new ValidationResult() {
            @Override
            public Strategy getStrategy() {
                return Strategy.DISABLED;
            }

            @Override
            public Map<File, List<ValidationError>> getValidationErrors() {
                return Maps.newHashMap();
            }
        };
    }

    @Override
    public void clean(Path path) {}
}
