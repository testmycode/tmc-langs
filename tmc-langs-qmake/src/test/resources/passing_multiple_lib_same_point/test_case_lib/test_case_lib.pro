QT -= gui

TARGET = test_case_lib
TEMPLATE = lib
CONFIG += staticlib

DEFINES += QT_DEPRECATED_WARNINGS

SOURCES += \
	test_case_lib.cpp

HEADERS += \
	test_case_lib.h

unix {
	target.path = /usr/lib
	INSTALLS += target
}
