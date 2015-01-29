
import com.google.common.collect.ImmutableList;
import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.LanguagePluginAbstract;
import fi.helsinki.cs.tmc.langs.RunResult;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 *
 * @author TMC-langs
 */
public class AntPlugin extends LanguagePluginAbstract {

    private static final Logger log = Logger.getLogger(AntPlugin.class.getName());

    public AntPlugin() {
        super("apache-ant");
    }

    @Override
    public ImmutableList<Path> findExercises(Path basePath) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ExerciseDesc scanExercise(Path path, String exerciseName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RunResult runTests(Path path) {
        ArrayList<String> args = new ArrayList<>();
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
