package eRror;

import java.util.HashSet;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;

public class errorMessages {

    // Modifier order
    static private int privateVariable;

    // Visibility Modifier - Class Design
    public int visibilityModifier;


    // Block checks
    public void blockChecks() {

        // Empty Block
        if (true) {

        }

        // Lefty Curly
        if (true)
        {
            doNothing();
        }

        // Need Braces
        if (false) doNothing();

        // RightCurly
        if (true) {
            doNothing();
        }
        else {
            doNothing();
        }

        // Avoid Nested Blocks
        {
            doNothing();
        }

    }

    // Interface is Type - Class Design
    public interface NotType {

        int WTF_TYPE = 0;
    }

    // Coding
    public class Covariant {

        private int asd;

        // CovariantEquals
        public boolean equals(Covariant obj) {
            // Empty statements
            ;

            // Hides field
            int asd = 0;

            // Inner assignment
            String s = Integer.toString(asd = 2);

            // Missing Switch Default
            switch(asd) {
                case 0:
                    break;
            }

            // Modified Control Variable
            for(int i = 0; i < 10; i++) {
                i++;
            }
            return true;
        }

        // Redundant throws
        public void wtf() throws Exception, IllegalAccessError {
            throw new IllegalAccessError();
        }

        // Booleans
        public boolean booleans() {
            boolean bsd = true;

            // String Literal Equality
            if ("asd" == "bsd") {
                return true;
            }
            // Simplify Boolean Expression
            // Simplify Boolean Return
            if (bsd == true) {
                return true;
            } else {
                return false;
            }
        }
    }

    public void nesting() {
        // Nested For Depth
        for (int i = 0; i < 10; i++) {
            for (int ii = 0; ii < 10; ii++) {
                for (int j = 0; j < 10; j++) {
                    doNothing();
                }
            }
        }

    }
    public void moreNesting() {
        // Nested If Depth
        if (true) {
            if (true) {
                if (true) {
                    doNothing();
                }
            }
        }

        // Nested try depth
        try {
            try {
                doNothing();
            } catch (Exception e) {
                doNothing();
            }
        } catch (Exception e) {
            doNothing();
        }
    }


    // No Clone
    @Override
    public errorMessages clone() {
        return this;
    }


    // No finalize
    @Override
    protected void finalize() throws Throwable {
    }

    // Max return statements
    public int noWayToReturn() {
        int asd = 0;
        if (asd == 0) {
            return 0;
        } else if (asd == 1) {
            return 0;
        } else if (asd == 2) {
            return 0;
        } else if (asd == 3) {
            return 0;
        }
        return 0;
    }


    public void moreCoding() {
        HashSet<Integer> set = new HashSet<Integer>();

    }

    // Declaration Order
    // Explicit Initialization
    private static int wrongPlaceWrongTime = 0;

    // Parameter Assignment
    public void parameterAssignment(int param) {
        param = 2;
    }

    // Default Comes Last
    // Fall Through
    public void switchWithDefaultNotLast() {
        int a = 0;
        switch (a) {
            default:
                break;
            case 2:
                a++;
            case 1:
                break;
        }
    }

    public void variables() {
        // Multiple Variable Declarations
        int a = 0, b = 3;
        //Unnecessary Parentheses
        int c = (1 + 1);
        // One statement per line
        a = 1; b = 2;
    }

    public void someComplexity() {
        boolean aa = true;
        boolean bb = false;
        if (aa == bb || aa || bb || aa && bb || !aa || !bb || !aa & !bb) {
            aa = false;
        }
        A a = new A();
        B b = new B();
        C c = new C();
        D d = new D();
        E e = new E();
        F f = new F();
        G g = new G();
        H h = new H();
        I i = new I();
        J j = new J();
        K k = new K();
        L l = new L();
        M m = new M();
        N n = new N();
        O o = new O();
        P p = new P();
        Q q = new Q();
        R r = new R();
        S s = new S();
        T t = new T();
        U u = new U();

        for (int v = 0; v < 10; v++) {
            if (bb) {
                switch (v) {
                    case 1:
                        aa = false;
                        break;
                    default:
                        break;
                }
            }
        }
        boolean path1 = true;
        boolean path2 = true;
        boolean path3 = true;
        boolean path4 = true;
        boolean path5 = true;
        boolean path6 = true;
        if (path1) {
            doNothing();
        }
        if (path2) {
            doNothing();
        }
        if (path3) {
            doNothing();
        }
        if (path4) {
            doNothing();
        }
        if (path5) {
            doNothing();
        }
        if (path6) {
            doNothing();
        }
    }

    // Miscellaneous
    public void goingDownDangerousRoad() {
        long l = 0l;
         String asd[];

    }

    public final class Modifiers {
        public final void modifier() {

        }
    }

    public abstract class NotAbstractName {

    }

    public class Foo<a> {
        // Constant Name
        public static final int FUUU_bar = 0;
        // Local Final Variable Name
        private final int ASD = 0;

        public Foo<a> methodTypeParameterName(Foo<a> asdAsd) {
            return asdAsd;
        }
    }

    public void sizesSizesSizesSizesSizesSizesSizesSizesSizesSizesSizesSizesSizesSizesSizesSizesSizesSizesSizesSizesSizes() {
        doNothing();
        doNothing();
        doNothing();
        doNothing();
        doNothing();
        doNothing();
        doNothing();
        doNothing();
        doNothing();
        doNothing();
        doNothing();
        doNothing();
        doNothing();
        doNothing();
        doNothing();
        doNothing();
        doNothing();
        doNothing();
        doNothing();
        doNothing();
        doNothing();
        doNothing();
        doNothing();
        doNothing();
        doNothing();
        doNothing();
        doNothing();
        doNothing();
        doNothing();
        doNothing();
        doNothing();
        doNothing();

        new AnonymousInterface() {

            @Override
            public void foo() {
                foo();
                foo();
                foo();
                foo();
                foo();
                foo();
                foo();
                foo();
                foo();
                foo();
                foo();
                foo();
                foo();
                foo();
                foo();
            }
        };
    }

    public interface AnonymousInterface {
        void foo();
    }

    public void tooManyParameters(int a, int b, int c, int d, int e) {

    }

    public class SomeMethods {
        public void doNothing1() {
        }
        public void doNothing2() {
        }
        public void doNothing3() {
        }
        public void doNothing4() {
        }
        public void doNothing5() {
        }
        public void doNothing6() {
        }
        public void doNothing7() {
        }
        public void doNothing8() {
        }
        public void doNothing9() {
        }
        public void doNothing10() {
        }
        public void doNothing11() {
        }
        public void doNothing12() {
        }
        public void doNothing13() {
        }
        public void doNothing14() {
        }
        public void doNothing15() {
        }
        public void doNothing16() {
        }
        public void doNothing17() {
        }
        public void doNothing18() {
        }
        public void doNothing19() {
        }
        public void doNothing20() {
        }
        public void doNothing21() {
        }
    }

    public void someWhitespace( int i){
        List < Integer> x = new ArrayList<Integer>();

        for ( ;i < 0;) {
            doNothing();
        }
        int a = 0;
        ++ a;
        int b = a ++;

        String wrap = "asd"
                + "bsd";

        b = (int) 1 * ( 2 + 3);
        tooManyParameters(1,2, 3, 4, 5);
    }

    public static void main(String[] args) {
        System.out.println("WTF");
    }

    public class A {
    }
    public class B {
    }
    public class C {
    }
    public class D {
    }
    public class E {
    }
    public class F {
    }
    public class G {
    }
    public class H {
    }
    public class I {
    }
    public class J {
    }
    public class K {
    }
    public class L {
    }
    public class M {
    }
    public class N {
    }
    public class O {
    }
    public class P {
    }
    public class Q {
    }
    public class R {
    }
    public class S {
    }
    public class T {
    }
    public class U {
    }
    public void doNothing() {

    }

    public int itShallReturnOne() {
        return 0;
    }
}