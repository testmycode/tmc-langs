TEMPLATE = app
CONFIG += console
CONFIG -= app_bundle

test {
    include(test.pro)
    TARGET = test/test
} else {
    SOURCES += main.c
}
