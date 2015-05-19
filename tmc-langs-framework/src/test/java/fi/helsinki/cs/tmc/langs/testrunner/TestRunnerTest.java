package fi.helsinki.cs.tmc.langs.testrunner;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestRunnerTest {

    @Test
    public void shouldReturnTestResults() throws Exception {
        TestCaseList allCases = new TestCaseList();
        allCases.add(new TestCase(
                TestRunnerTestSubject.class.getName(), "successfulTestCase",
                new String[]{"one", "two", "three"}
        ));
        allCases.add(new TestCase(
                TestRunnerTestSubject.class.getName(), "failingTestCase",
                new String[]{"two"}
        ));

        TestRunner testRunner = new TestRunner(this.getClass().getClassLoader());
        testRunner.runTests(allCases, 5);

        TestCaseList seekResults
                = allCases.findByMethodName("successfulTestCase");
        assertEquals(1, seekResults.size());
        TestCase testCase = seekResults.get(0);
        assertEquals(TestCase.Status.PASSED, testCase.status);

        seekResults = allCases.findByMethodName("failingTestCase");
        assertEquals(1, seekResults.size());
        testCase = seekResults.get(0);
        assertEquals(TestCase.Status.FAILED, testCase.status);
        assertEquals("java.lang.AssertionError", testCase.exception.className);
        assertEquals(17, testCase.exception.stackTrace[1].getLineNumber()); // (below Assert.fail's stack frame)

        seekResults = allCases.findByPointName("one");
        assertEquals(1, seekResults.size());

        seekResults = allCases.findByPointName("two");
        assertEquals(2, seekResults.size());

        seekResults = allCases.findByPointName("three");
        assertEquals(1, seekResults.size());

        seekResults = allCases.findByPointName("ninethousand");
        assertTrue(seekResults.isEmpty());
    }

    @Test
    public void shouldTimeoutInfiniteLoop() throws Exception {
        TestCaseList allCases = new TestCaseList();
        allCases.add(new TestCase(
                TimeoutTestSubject.class.getName(), "infinite",
                new String[]{"infinite"}
        ));
        allCases.add(new TestCase(
                TimeoutTestSubject.class.getName(), "empty",
                new String[]{"passing"}
        ));
        allCases.add(new TestCase(
                TimeoutTestSubject.class.getName(), "empty2",
                new String[]{"passing"}
        ));

        TestRunner testRunner = new TestRunner(this.getClass().getClassLoader());
        testRunner.runTests(allCases, 1);

        assertEquals(3, allCases.size());
        TestCase infiniteCase = allCases.findByMethodName("infinite").get(0);

        assertEquals("infinite", infiniteCase.methodName);
        assertEquals(TestCase.Status.FAILED, infiniteCase.status);
        assertTrue(infiniteCase.message.contains("timeout"));

        TestCaseList passingCases = allCases.findByPointName("passing");
        assertEquals(2, passingCases.size());
        for (TestCase t : passingCases) {
            assertTrue(t.status == TestCase.Status.NOT_STARTED
                    || t.status == TestCase.Status.PASSED);
        }

    }

    @Test
    public void shouldHonorRunWithAnnotation() {
        MockRunner.reset();

        TestCaseList allCases = new TestCaseList();
        allCases.add(new TestCase(
                RunWithTestSubject.class.getName(), "testCase",
                new String[]{}
        ));

        TestRunner testRunner = new TestRunner(this.getClass().getClassLoader());

        assertFalse(MockRunner.runCalled);
        testRunner.runTests(allCases, 1000000);
        if (allCases.get(0).status == TestCase.Status.FAILED) {
            fail("Test failed: " + allCases.get(0).message);
        }
        assertTrue(MockRunner.runCalled);

        MockRunner.reset();
    }
}
