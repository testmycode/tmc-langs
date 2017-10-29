#This additional CONFIG variable option will allow libraries
#to be correctly linked when building projects using Windows.

win32 {
    CONFIG -= debug_and_release debug_and_release_target
}

TEMPLATE = subdirs
SUBDIRS += \
	test_case_app \
	test_case_test_runner 
