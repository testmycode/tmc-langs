QT += core testlib
QT -= gui

CONFIG += c++11 console testcase
CONFIG -= app_bundle

DEFINES += QT_DEPRECATED_WARNINGS

SOURCES += main.cpp \
        test_case_test_runner.cpp \
        $$PWD/../test_case_app/test_case_app.cpp

HEADERS += \
	test_case_test_runner.h

INCLUDEPATH += $$PWD/../test_case_app


