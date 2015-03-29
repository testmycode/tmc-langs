package ant_project;

import org.junit.Test;

public class AntTestSubject {

    @Test
    public void bareTestMethod() {
    }

    public void notATestMethod() {
    }

    @Test
    @Points("one")
    public void oneExTestMethod() {
    }

    @Test
    @Points("one")
    public void secondOneExTestMethod() {
    }

    @Test
    @Points("one two")
    public void twoExTestMethod() {
    }
}