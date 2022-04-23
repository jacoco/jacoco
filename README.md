JaCoCo Java Code Coverage Library
=================================

[![Build Status](https://dev.azure.com/jacoco-org/JaCoCo/_apis/build/status/JaCoCo?branchName=master)](https://dev.azure.com/jacoco-org/JaCoCo/_build/latest?definitionId=1&branchName=master)
[![Build status](https://ci.appveyor.com/api/projects/status/g28egytv4tb898d7/branch/master?svg=true)](https://ci.appveyor.com/project/JaCoCo/jacoco/branch/master)
[![Maven Central](https://img.shields.io/maven-central/v/org.jacoco/jacoco.svg)](http://search.maven.org/#search|ga|1|g%3Aorg.jacoco)

JaCoCo is a free Java code coverage library distributed under the Eclipse Public
License. Check the [project homepage](http://www.jacoco.org/jacoco)
for downloads, documentation and feedback.

Please use our [mailing list](https://groups.google.com/forum/?fromgroups=#!forum/jacoco)
for questions regarding JaCoCo which are not already covered by the
[extensive documentation](http://www.jacoco.org/jacoco/trunk/doc/).

Note: We do not answer general questions in the project's issue tracker. Please use our [mailing list](https://groups.google.com/forum/?fromgroups=#!forum/jacoco) for this.
-------------------------------------------------------------------------
-------------------------------------------------------------------------
-------------------------------------------------------------------------

Usage: java -jar jacococli.jar report [(execfiles) ...] --classfiles (path) [--csv (file)] [--encoding (charset)] [--help] [--html (dir)] [--name (name)] [--quiet][--sourcefiles (path)] [--tabwith (n)] [--xml (file)]
  
 (execfiles)          : list of JaCoCo *.exec files to read
   
 --classfiles (path)  : location of Java class files
   
 --csv (file)         : output file for the CSV report
   
 --encoding (charset) : source file encoding (by default platform encoding is used)
   
 --help               : show help
   
 --html (dir)         : output directory for the HTML report
   
 --name (name)        : name used for this report
   
 --quiet              : suppress all output on stdout
   
 --sourcefiles (path) : location of the source files
   
 --tabwith (n)        : tab stop width for the source pages (default 4)
   
 --xml (file)         : output file for the XML report
