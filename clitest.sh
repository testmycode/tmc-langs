#!/bin/bash -v

mvn clean  package -Dmaven.test.skip=true

java -jar tmc-langs-cli/target/tmc-langs-cli-*-SNAPSHOT.jar scan-exercise  --exercisePath tmc-langs-java/src/test/resources/maven_exercise --outputPath a
cat a

java -jar tmc-langs-cli/target/tmc-langs-cli-*-SNAPSHOT.jar prepare-stubs --exercisePath tmc-langs-java/src/test/resources/ant_arith_funcs/ --outputPath arithfuncs-proj
java -jar tmc-langs-cli/target/tmc-langs-cli-*-SNAPSHOT.jar checkstyle --exercisePath tmc-langs-cli/src/test/resources/arith_funcs --locale=en --outputPath tmp
cat tmp
