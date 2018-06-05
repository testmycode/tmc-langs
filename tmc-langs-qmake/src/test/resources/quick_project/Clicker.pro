TEMPLATE = subdirs
SUBDIRS += \
        src \
        test_runner

        test_runner.depends = src
