package fi.helsinki.cs.tmc.langs.make;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.domain.TestResult;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import java.util.ArrayList;

public class CTestCaseTest {

    private static final ImmutableList<String> points = ImmutableList.of("1.1");

    private static final CTestCase passing = new CTestCase("test_passing", true, "", points);
    private static final CTestCase failing =
            new CTestCase("test_failing", false, "Some tests failed", new ArrayList<String>());
    private static final CTestCase valgrindFail =
            new CTestCase("test_valgrindFail", true, "", points);
    private static final String valgrindTrace =
            "\n"
                    + "==20737== \n"
                    + "==20737== HEAP SUMMARY:\n"
                    + "==20737==     in use at exit: 1,744 bytes in 31 blocks\n"
                    + "==20737==   total heap usage: 46 allocs, 15 frees, 4,839 bytes "
                    + "allocated\n"
                    + "==20737== \n"
                    + "==20737== 32 bytes in 1 blocks are definitely lost in loss record "
                    + "27 of 31\n"
                    + "==20737==    at 0x4C2AB80: malloc "
                    + "(in /usr/lib/valgrind/vgpreload_memcheck-amd64-linux.so)\n"
                    + "==20737==    by 0x4025A9: passing (source.c:11)\n"
                    + "==20737==    by 0x401ADE: test_passing (test_source.c:14)\n"
                    + "==20737==    by 0x405CC6: srunner_run "
                    + "(in /home/mession/Code/cprojects/Module_1/Task_1_4/test/test)\n"
                    + "==20737==    by 0x401FAE: tmc_run_tests (tmc-check.c:122)\n"
                    + "==20737==    by 0x401C71: main (test_source.c:35)\n"
                    + "==20737== \n"
                    + "==20737== LEAK SUMMARY:\n"
                    + "==20737==    definitely lost: 32 bytes in 1 blocks\n"
                    + "==20737==    indirectly lost: 0 bytes in 0 blocks\n"
                    + "==20737==      possibly lost: 0 bytes in 0 blocks\n"
                    + "==20737==    still reachable: 1,712 bytes in 30 blocks\n"
                    + "==20737==         suppressed: 0 bytes in 0 blocks\n"
                    + "==20737== Reachable blocks (those to which a pointer was found) "
                    + "are not shown.\n"
                    + "==20737== To see them, rerun with: --leak-check=full "
                    + "--show-leak-kinds=all\n"
                    + "==20737== \n"
                    + "==20737== For counts of detected and suppressed errors, rerun with: -v\n"
                    + "==20737== ERROR SUMMARY: 1 errors from 1 contexts (suppressed: 0"
                    + " from 0)";

    private static final CTestCase bothFail =
            new CTestCase("test_bothFail", false, "Some tests failed", points);
    private static final CTestCase valgrindFailAllowed =
            new CTestCase("test_valgrindFailAllowed", true, "", points, false);

    static {
        valgrindFail.setValgrindTrace(valgrindTrace);
        bothFail.setValgrindTrace(valgrindTrace);
        valgrindFailAllowed.setValgrindTrace(valgrindTrace);
    }

    @Test
    public void testResultIsCorrectWithPassingTest() {
        TestResult testResult = this.passing.getTestResult();
        System.out.println(this.passing.getTestResult());
        assertEquals("test_passing", testResult.getName());
        assertTrue(testResult.isSuccessful());
        assertEquals(1, testResult.points.size());
        assertEquals("1.1", testResult.points.get(0));
        assertEquals("", testResult.getMessage());
        assertEquals(0, testResult.getException().size());
    }

    @Test
    public void testResultIsCorrectWithFailingTest() {
        TestResult testResult = this.failing.getTestResult();

        assertEquals("test_failing", testResult.getName());
        assertFalse(testResult.isSuccessful());
        assertEquals(0, testResult.points.size());
        assertEquals("Some tests failed", testResult.getMessage());
        assertEquals(0, testResult.getException().size());
    }

    @Test
    public void testResultIsCorrectWhenValgrindFailsButTestPasses() {
        TestResult testResult = this.valgrindFail.getTestResult();

        assertEquals("test_valgrindFail", testResult.getName());
        assertFalse(testResult.isSuccessful());
        assertEquals(1, testResult.points.size());
        assertEquals("1.1", testResult.points.get(0));
        String valgrindErrorMessage =
                " - Failed due to errors in valgrind log; see log below. "
                        + "Try submitting to server, some leaks might be platform dependent";
        assertEquals(valgrindErrorMessage, testResult.getMessage());
        assertEquals(25, testResult.getException().size());
        assertTrue(
                testResult
                        .getException()
                        .get(testResult.getException().size() - 1)
                        .contains("ERROR SUMMARY: 1 errors from 1 contexts"));
    }

    @Test
    public void testResultIsCorrectWhenValgrindFailsAndTestFails() {
        TestResult testResult = this.bothFail.getTestResult();

        assertEquals("test_bothFail", testResult.getName());
        assertFalse(testResult.isSuccessful());
        assertEquals(1, testResult.points.size());
        assertEquals("1.1", testResult.points.get(0));
        assertEquals("Some tests failed", testResult.getMessage());
        assertEquals(25, testResult.getException().size());
        assertTrue(
                testResult
                        .getException()
                        .get(testResult.getException().size() - 1)
                        .contains("ERROR SUMMARY: 1 errors from 1 contexts"));
    }

    @Test
    public void testResultIsCorrectWhenValgrindIsAllowedToFail() {
        TestResult testResult = this.valgrindFailAllowed.getTestResult();

        assertEquals("test_valgrindFailAllowed", testResult.getName());
        assertTrue(testResult.isSuccessful());
        assertEquals(1, testResult.points.size());
        assertEquals("1.1", testResult.points.get(0));
        assertEquals("", testResult.getMessage());
        assertTrue(testResult.getException().isEmpty());
    }
}
