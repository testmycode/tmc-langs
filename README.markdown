# tmc-langs

[![Build Status](https://travis-ci.org/testmycode/tmc-langs.svg?branch=master)](https://travis-ci.org/testmycode/tmc-langs)
[![Build status](https://ci.appveyor.com/api/projects/status/w6c4s3j09jfhbhb7/branch/master?svg=true)](https://ci.appveyor.com/project/rage/tmc-langs/branch/master)
[![Coverage Status](https://coveralls.io/repos/testmycode/tmc-langs/badge.svg?branch=master&service=github)](https://coveralls.io/github/testmycode/tmc-langs?branch=master)

Framework for supporting different programming languages in [TMC](https://github.com/testmycode/tmc-server).

TMC-langs provides an Java interface that encapsulates everything needed to support a new language in TMC. The framework provides CLI wrappers so that it's fairly convenient to call from other languages like Ruby.

## Build

Note that all the mvn commands should be ran in the projects root directory.

Build the project with `mvn clean package`. Install the dependency to your local Maven repository with `mvn clean install -U`.

## Test

Test the project with `mvn test`.
For the tmc-langs-java maven tests to pass it either needs to be invoked with mvn exec magic, like NetBeans does it. However, when executed from commandline `M3_HOME` must be configured. Either via `$M3_HOME` or from mavens configuration files.

Running tests for:
- `tmc-langs-python`: `python` executable must be accessible from your `$PATH`
- `tmc-langs-rust`: `rust` and rust-package-manager executables must be accessible from your `$PATH`.
- `tmc-langs-qmake`: a valid Qt install with `qmake` executable must be accessible from your `$PATH` and `$QT_QPA_PLATFORM` set to `minimal`

## Usage

Add the dependencies to your project’s `pom.xml`.

```xml
<dependency>
  <groupId>fi.helsinki.cs.tmc</groupId>
  <artifactId>tmc-langs-framework</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
<dependency>
  <groupId>fi.helsinki.cs.tmc</groupId>
  <artifactId>tmc-langs-util</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
<dependency>
  <groupId>fi.helsinki.cs.tmc</groupId>
  <artifactId>tmc-langs-ant</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

TMC-langs can be invoked programmatically or by running the software from the command-line.

### Programmatically

Create a `TaskExecutorImpl` and call the required task of `TaskExecutor`. `TaskExecutor` will take care of detecting the provided projects language, if TMC-langs cannot recognize the project as a supported language `NoLanguagePluginFound` exception will be thrown.

```java
TaskExecutor taskExecutor = new TaskExecutorImpl();
RunResult runResult = taskExecutor.runTests(projectInfo.getProjectDirAsPath());
```

Supported tasks and their return types and parameters can be read from [TaskExecutor](https://github.com/tmc-langs/tmc-langs/blob/master/tmc-langs-util/src/main/java/fi/helsinki/cs/tmc/langs/util/TaskExecutor.java)

### CLI

Running tasks from the command-line can be accomplished by passing the required task with projects directory path and in some tasks the output file path.
After building the `tmc-langs-cli` commandline interface you can find the jar to use under `target/`

`java -jar target/tmc-langs-cli-1.0-SNAPSHOT.jar run-tests --exercisePath test_projects/arith_funcs --outputPath results.txt`

For all options run
`java -jar target/tmc-langs-cli-1.0-SNAPSHOT.jar help`

## Credits

Original draft of the framework structure [mpartel](https://github.com/mpartel).

The project is part of Software Lab project at the [University of Helsinki CS Dept.](https://www.cs.helsinki.fi/home/).

### Developers
  * Ville Heikkinen [zzats](https://github.com/zzats)
  * Joel Järvinen [PunyW](https://github.com/PunyW)
  * Aleksi Paavola [AlePaa](https://github.com/AlePaa)
  * Joakim Store [Tahantos](https://github.com/Tahantos)
  * Jasu Viding [jviding](https://github.com/jviding)

### Instructor
Leo Leppänen [loezi](https://github.com/loezi)

### Clients
  * Martin Pärtel [mpartel](https://github.com/mpartel)
  * Jarmo Isotalo [jamox](https://github.com/jamox)

The project is currently maintained by the Agile Education Research group.