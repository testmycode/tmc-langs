#include <QTest>
#include "test_case_test_runner.h"
#include "test_case_app.h"

// Produces qInfo("TMC:test_name.point")
#define POINT(test_name, point) qInfo("TMC:"#test_name"."#point)

test_case_test_runner::test_case_test_runner(QObject *parent) : QObject(parent)
{

}

void test_case_test_runner::test_function_one_here() {

    test_case_app test_case;

    POINT(test_function_one_here, 1);
    QVERIFY(!strcmp(test_case.piece_of_string(), "Hello, world!"));

}

void test_case_test_runner::test_function_two_here() {

    test_case_app test_case;

    //Please take note that this test will fail because addition in the tested
    //function is replaced with multiplication and 0+0=0 as well as 0*0=0

     POINT(test_function_two_here, 1);
     QVERIFY(test_case.adding_ints(0, 0) == 0);

}

void test_case_test_runner::test_function_two_here_2() {

    test_case_app test_case;

    POINT(test_function_two_here_2, 2);
    QVERIFY(test_case.adding_ints(-341, 428) == 87);
}
