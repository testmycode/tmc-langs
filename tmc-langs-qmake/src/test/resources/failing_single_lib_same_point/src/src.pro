QT -= gui

CONFIG += c++11 console
CONFIG -= app_bundle

win32 {
    CONFIG -= debug_and_release debug_and_release_target
}

DEFINES += QT_DEPRECATED_WARNINGS

SOURCES += main.cpp

INCLUDEPATH += $$PWD/../test_case_lib
LIBS += -L$$OUT_PWD/../test_case_lib -ltest_case_lib
