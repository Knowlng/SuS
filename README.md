# SuS - Securing user Software

**This project was successfully completed through team collaboration during the Project-Based Learning course at Vilnius University.**

Cross-platform `static analysis` software for analyzing Android Applications

## Project vision

Our goal is to create a versatile tool for in-depth analysis of Android applications. This software is designed decompile APK files from the Android store, enabling users to conduct thorough investigations into their Java code. Key features include the ability to detect and analyze potentially dangerous code patterns, which are identified based on a specific method, that rewrites dangerous patterns as a list of dangerous patterns detectors.

The analysis results are then articulated in a detailed PDF report. This report includes not only textual information but also graphical representations of dangerous patterns as well as code snippets from our pattern investigation.

Our vision is to empower both experienced and novice users with a robust, user-friendly tool that makes the complex task of static analysis more approachable and effective, contributing to safer Android application development and usage.

## Project Documentation

### Table of Contents

- [Contribution Guidelines](./doc/CONTRIBUTING.md)
  If you're interested in contributing to this project, please refer to this section for essential information and guidelines.

- [Supported command line arguments](./doc/commandLineArguments.md)
  Explore this section, to gain insights on each available parameters and their usage.

- [Dangerous patterns detectors](./doc/patternDetectors.md)
  Learn about the mechanisms we've implemented for identifying dangerous patterns.

- [Describing dangerous patterns in a json file](./doc/dangerousPatterns.md)
  Enhance your understanding on how to describe dangerous patterns in a structured `JSON` format.

- [Project Architecture Overview](./doc/projectArchitecture.md)
  Get a detailed overview of the project architecture, including the key components and their responsibilities.


## Development dependencies

These dependencies are automatically included in our program

- [AXMLPrinter2](https://code.google.com/archive/p/android4me/downloads)
- [dex2jar](https://github.com/pxb1988/dex2jar)
- [cfr](https://www.benf.org/other/cfr/)
- [JavaParser](https://javaparser.org)
- [Gson](https://github.com/google/gson)
- [Apache Commons Compress](https://mvnrepository.com/artifact/org.apache.commons/commons-compress/1.21)
- [Apache Commons Validator](https://mvnrepository.com/artifact/commons-validator/commons-validator/1.7)

## Runtime dependencies

For dataflow graph generation this dependancy should be installed:

- [Graphviz](https://graphviz.org/download/)

## Building the program

**Java version 19** or newer is needed to compile this program!

#### Compile the program using gradle wrapper

On Linux, MacOS & Windows versions that support `shell` scripts

    ./gradlew build

On windows

    gradlew.bat build

#### You can found generated JAR executable inside `/build/libs/` directory

Run the generated _JAR_ file

    java -jar build/libs/sus.jar

Please make sure to also keep the `/tools` folder in the root folder of the project!