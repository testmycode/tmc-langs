#include <QTest>
#include "test_case_test_runner.h"
#include "test_case_lib.h"
#include "test_case_lib2.h"

// Produces qInfo("TMC:test_name.point")
#define POINT(test_name, point) qInfo("TMC:"#test_name"."#point)

test_case_test_runner::test_case_test_runner(QObject *parent) : QObject(parent)
{

}

void test_case_test_runner::test_function_one_here() {

    test_case_lib test_case;

    POINT(test_function_one_here, testPoint1);
    POINT(test_function_one_here, testPoint1.1);
    QVERIFY(!strcmp(test_case.piece_of_string(), "Hello, world!"));

}

void test_case_test_runner::test_function_two_here() {

    test_case_lib2 test_case;

    POINT(test_function_two_here, testPoint1);
    POINT(test_function_two_here, testPoint1.2);
    QVERIFY(test_case.adding_ints(666, 1337) == 2003);

}

void test_case_test_runner::test_function_two_here_2() {

    test_case_lib2 test_case;

    POINT(test_function_two_here_2, testPoint1);
    POINT(test_function_two_here_2, testPoint1.3);
    QVERIFY(test_case.adding_ints(-341, 428) == 87);
}
