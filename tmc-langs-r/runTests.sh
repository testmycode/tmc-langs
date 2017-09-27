#!/bin/sh
#Currently this script needs to be run at project root!
/usr/bin/Rscript -e "library(tmcRtestrunner);run_tests_with_default(TRUE)"
