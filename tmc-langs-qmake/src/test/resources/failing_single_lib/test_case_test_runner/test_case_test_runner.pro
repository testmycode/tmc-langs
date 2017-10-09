QT += core testlib
QT -= gui

CONFIG += c++11 console testcase
CONFIG -= app_bundle

DEFINES += QT_DEPRECATED_WARNINGS

SOURCES += main.cpp \
        test_case_test_runner.cpp 

HEADERS += \
	test_case_test_runner.h

INCLUDEPATH += $$PWD/../test_case_lib
LIBS += -L$$OUT_PWD/../test_case_lib -ltest_case_lib


