TEMPLATE = subdirs
SUBDIRS += \
        src \
        test_case_test_runner \
        test_case_lib \
        test_case_lib2

        src.depends = test_case_lib \
                      test_case_lib2
        test_case_test_runner.depends = test_case_lib \
                                        test_case_lib2





