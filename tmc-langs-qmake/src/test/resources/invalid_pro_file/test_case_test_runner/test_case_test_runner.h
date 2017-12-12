#ifndef TEST_CASE_TEST_RUNNER_H
#define TEST_CASE_TEST_RUNNER_H

#include <QObject>

class test_case_test_runner : public QObject {

	Q_OBJECT

	public:
		explicit test_case_test_runner(QObject *parent = nullptr);

	signals:

	public slots:

	private slots:
    void test_function_one_here();
    void test_function_two_here();

};

#endif
