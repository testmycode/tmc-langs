package fi.helsinki.cs.tmc.langs.java.testrunner;

import static org.junit.Assert.assertEquals;

import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.TestCase;
import fi.helsinki.cs.tmc.langs.domain.TestDesc;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class TestCaseListTest {

    private TestCaseList cases = new TestCaseList();


    @Before
    public void setUp() {
        TestCase testCase = new TestCase("Test", "Method", new String[]{"a", "b", "c"});
        TestCase otherCase = new TestCase("Other", "Case", new String[]{"d", "e", "f"});
        TestCase thirdCase = new TestCase("Third", "Case", new String[]{"a", "g", "h"});
        cases.add(testCase);
        cases.add(otherCase);
        cases.add(thirdCase);
    }

    @Test
    public void testFromExerciseDesc() {
        ArrayList<TestDesc> caseList = new ArrayList<>();
        caseList.add(new TestDesc("Test Method",
                ImmutableList.copyOf(new String[]{"a", "b", "c"})));
        caseList.add(new TestDesc("Other Case", ImmutableList.copyOf(new String[]{"d", "e", "f"})));
        caseList.add(new TestDesc("Third Case", ImmutableList.copyOf(new String[]{"a", "g", "h"})));

        ImmutableList<TestDesc> tests = ImmutableList.copyOf(caseList);
        ExerciseDesc desc = new ExerciseDesc("asd", tests);
        Optional<ExerciseDesc> exDesc = Optional.of(desc);

        TestCaseList result = TestCaseList.fromExerciseDesc(exDesc);
        assertEquals("After fromExeciseDesc all cases should be converted", 3, result.size());
    }

    @Test
    public void testFindByMethodName() {
        TestCaseList result = cases.findByMethodName("Case");
        assertEquals("Result list should contain two cases", 2, result.size());

    }

    @Test
    public void testFindByPointName() {
        TestCaseList result = cases.findByPointName("a");
        assertEquals("Result list should contain two cases", 2, result.size());
    }
}
