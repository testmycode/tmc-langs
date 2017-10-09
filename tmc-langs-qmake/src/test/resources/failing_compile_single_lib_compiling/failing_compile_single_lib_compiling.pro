TEMPLATE = subdirs
SUBDIRS += \
	test_case_app \
	test_case_test_runner \
	test_case_lib

	test_case_app.depends = test_case_lib
	test_case_test_runner.depends = test_case_lib
