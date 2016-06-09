package fi.helsinki.cs.tmc.langs.java;

import fi.helsinki.cs.tmc.testscanner.TestScanner;

/**
 * Creates testscanner only when needed, to avoid issues whre no java compiler is present.
 */
public class LazyTestScanner {

    private TestScanner testScanner;

    public TestScanner get() {
        if (this.testScanner == null) {
            this.testScanner = new TestScanner();
        }
        return this.testScanner;
    }
}
