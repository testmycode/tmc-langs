
package fi.helsinki.cs.tmc.langs.qmake;


public class QmakeBuildException extends RuntimeException {

    QmakeBuildException(java.lang.Exception exception) {
        System.err.println(exception);
    }
    
}
