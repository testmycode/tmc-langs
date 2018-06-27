TEMPLATE = app
TARGET = tst_clicker
QT += quick core testlib
CONFIG += c++11 warn_on qmltestcase
CONFIG -= app_bundle

SOURCES += tst_clicker.cpp

RESOURCES += \
        $$PWD/../src/qml.qrc

INCLUDEPATH += $$PWD/../src

QML2_IMPORT_PATH = $$PWD/../src/qml.qrc

OTHER_FILES += \
    tst_clicker.qml \
    ClickerTest.qml
