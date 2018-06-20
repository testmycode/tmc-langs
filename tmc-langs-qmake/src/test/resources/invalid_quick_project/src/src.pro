QT += quick
TARGET = clicker
CONFIG += c++11

DEFINES += QT_DEPRECATED_WARNINGS

SOURCES += main.cpp

RESOURCES += qml.qrc

OTHER_FILES += \
    Clicker.qml \
    main.qml
