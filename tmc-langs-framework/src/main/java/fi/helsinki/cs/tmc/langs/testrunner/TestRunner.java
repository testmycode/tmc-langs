package fi.helsinki.cs.tmc.langs.testrunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

public class TestRunner {

    private final ClassLoader testClassLoader;

    // `cases`, `currentCase` and `threadException` are accessed by the thread
    // and shall be synchronized on `lock`. The thread shall not
    // modify them after it has been interrupted.
    private final Object lock = new Object();
    private TestCaseList cases;
    private int currentCaseIndex;
    private Throwable threadException;

    public TestRunner(ClassLoader testClassLoader) {
        this.testClassLoader = testClassLoader;
    }

    public synchronized void runTests(TestCaseList cases, int suiteTimeout) {
        this.cases = cases;
        this.currentCaseIndex = 0;
        this.threadException = null;

        Thread thread = createTestThread();
        thread.start();
        try {
            thread.join(suiteTimeout * 1000l);
        } catch (InterruptedException e) {
            // Ok, we'll stop.
        }

        synchronized (lock) {
            thread.interrupt();  // The thread should now no longer mutate anything.
            if (currentCaseIndex < cases.size()) {
                TestCase currentCase = cases.get(this.currentCaseIndex);
                currentCase.status = TestCase.Status.FAILED;
                if (threadException != null) {
                    currentCase.message = threadException.toString();
                } else {
                    currentCase.message = "timeout";
                }
            }
        }
    }

    private Thread createTestThread() {
        Thread thread = new Thread(new TestingRunnable(), "TestRunner thread");
        thread.setDaemon(true);
        return thread;
    }

    private class TestingRunnable implements Runnable {

        @Override
        public void run() {
            try {
                doRun();
            } catch (Throwable t) {
                synchronized (lock) {
                    threadException = t;
                }
            }
        }

        private void doRun() {
            TestCase currentCase;
            while (true) {
                synchronized (lock) {
                    if (currentCaseIndex == cases.size()) {
                        break;
                    }
                    currentCase = cases.get(currentCaseIndex);
                }

                try {
                    runTestCase(currentCase);
                } catch (NoTestsRemainException ex) {
                    // Don't care about empty test classes.
                } catch (InitializationError ex) {
                    synchronized (lock) {
                        if (Thread.currentThread().isInterrupted()) {
                            break;
                        }
                        currentCase.status = TestCase.Status.FAILED;
                        currentCase.message = "Failed to initialize test: " + ex.getMessage();
                        if (!ex.getCauses().isEmpty()) {
                            currentCase.message += "\n" + ex.getCauses().get(0).getMessage();
                            currentCase.exception = new CaughtException(ex.getCauses().get(0));
                        }
                    }
                } catch (Exception ex) {
                    synchronized (lock) {
                        if (Thread.currentThread().isInterrupted()) {
                            break;
                        }
                        currentCase.status = TestCase.Status.FAILED;
                        currentCase.message = "Failed to run test.";
                        currentCase.exception = new CaughtException(ex);
                    }
                }

                synchronized (lock) {
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    currentCaseIndex += 1;
                }
            }
        }

        private void runTestCase(TestCase currentCase)
                throws NoTestsRemainException, InitializationError {
            Class<?> testClass = loadTestClass(currentCase.className);
            ParentRunner<?> runner = createRunner(testClass);

            runner.filter(new MethodFilter(currentCase.methodName));

            RunNotifier notifier = new RunNotifier();
            notifier.addFirstListener(new TestListener(currentCase));

            runner.run(notifier);
        }

        private ParentRunner<?> createRunner(Class<?> testClass) throws InitializationError {
            RunWith rw = testClass.getAnnotation(RunWith.class);
            if (rw == null) {
                return new BlockJUnit4ClassRunner(testClass);
            }

            Class<? extends Runner> runnerCls = rw.value();
            if (!ParentRunner.class.isAssignableFrom(runnerCls)) {
                throw new InitializationError("TMC requires @RunWith to specify a class inheriting org.junit.runners.ParentRunner");
            }
            // Could check the type parameter too, but the code to do that would be very complex it seems.

            Constructor<? extends Runner> ctor;
            try {
                ctor = runnerCls.getConstructor(Class.class);
            } catch (NoSuchMethodException ex) {
                throw new InitializationError("Runner specified with @RunWith lacks a public constructor taking a test class");
            }

            try {
                return ((ParentRunner<?>) ctor.newInstance(testClass));
            } catch (InstantiationException ex) {
                throw new InitializationError("Failed to initialize test runner specified with @RunWith: " + ex.getMessage());
            } catch (IllegalAccessException ex) {
                throw new InitializationError(ex);
            } catch (InvocationTargetException ex) {
                throw new InitializationError("Failed to initialize test runner specified with @RunWith: " + ex.getMessage());
            }
        }
    }

    private class TestListener extends RunListener {

        private final TestCase testCase;

        public TestListener(TestCase testCase) {
            this.testCase = testCase;
        }

        @Override
        public void testRunStarted(Description description) throws Exception {
        }

        @Override
        public void testRunFinished(Result result) throws Exception {
        }

        @Override
        public void testIgnored(Description description) throws Exception {
        }

        @Override
        public void testStarted(Description desc) throws Exception {
            synchronized (lock) {
                if (!Thread.currentThread().isInterrupted()) {
                    this.testCase.testStarted();
                }
            }
        }

        @Override
        public void testFinished(Description description) throws Exception {
            synchronized (lock) {
                if (!Thread.currentThread().isInterrupted()) {
                    this.testCase.testFinished();
                }
            }
        }

        @Override
        public void testFailure(Failure failure) throws Exception {
            synchronized (lock) {
                if (!Thread.currentThread().isInterrupted()) {
                    this.testCase.testFailed(failure);
                }
            }
        }
    }

    public TestCase getCurrentCase() {
        synchronized (lock) {
            return cases.get(this.currentCaseIndex);
        }
    }

    private Class<?> loadTestClass(String className) {
        try {
            return testClassLoader.loadClass(className);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Failed to load test class", ex);
        }
    }
}
