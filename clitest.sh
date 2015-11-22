#!/bin/bash -v

mvn clean  package -Dmaven.test.skip=true

java -jar tmc-langs-cli/target/tmc-langs-cli-1.0-SNAPSHOT.jar scan-exercise  tmc-langs-java/src/test/resources/maven_exercise a
cat a

java -jar tmc-langs-cli/target/tmc-langs-cli-1.0-SNAPSHOT.jar prepare-stubs  tmc-langs-java/src/test/resources/ant_arith_funcs/ arithfuncs-proj
