#This additional CONFIG variable option will allow libraries
#to be correctly linked when building projects using Windows.

win32 {
    CONFIG -= debug_and_release debug_and_release_target
}

TEMPLATE = subdirs
SUBDIRS += \
	test_case_app \
	test_case_test_runner \
        test_case_lib \
        test_case_lib2

        test_case_app.depends = test_case_lib \
                                test_case_lib2
        test_case_test_runner.depends = test_case_lib \
                                        test_case_lib2
