#include <QtTest>

class TestTest : public QObject {
    Q_OBJECT

private slots:
    void initTestCase(); // called before the first test function is executed
    void init(); // called before each test function is executed
    void cleanup(); // called after every test function
    void cleanupTestCase(); // called after the last test function was executed

    // Declare unit test prototype definitions here
    void testExample1();
    void testExample2();
};


void TestTest::initTestCase()
{
    // called before the first test function is executed
}

void TestTest::init() {
    // called before each test function is executed
}

void TestTest::cleanup() {
    // called after every test function
}

void TestTest::cleanupTestCase()
{
    // called after the last test function was executed
}

// Implement unit test functions here.

void TestTest::testExample1()
{
    QVERIFY2(true, "Please fix your boolean logic");
    qInfo("TMC:13.1");
}

void TestTest::testExample2()
{
    QVERIFY2(true, "Please fix your boolean logic");
    qInfo("TMC:13.2");
}

// To make our test case a stand-alone executable, expand QtTest main macro
QTEST_MAIN(TestTest)
// Include meta object compiler output because we have no header file
#include "test.moc"
