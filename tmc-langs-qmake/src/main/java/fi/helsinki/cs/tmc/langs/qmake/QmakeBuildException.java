
package fi.helsinki.cs.tmc.langs.qmake;


public class QmakeBuildException extends RuntimeException {

    QmakeBuildException(Exception exception) {
        super(exception);
    }
    
}
