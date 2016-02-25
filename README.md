JaCoCo Java Code Coverage Library
=================================

[![Build Status](https://travis-ci.org/jacoco/jacoco.svg?branch=master)](https://travis-ci.org/jacoco/jacoco)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jacoco/org.jacoco.core/badge.svg?style=flat)](http://search.maven.org/#search|ga|1|g%3Aorg.jacoco)

JaCoCo is a free Java code coverage library distributed under the Eclipse Public
License. Check the [project homepage](http://www.eclemma.org/jacoco)
for downloads, documentation and feedback.


此fork的版本主要是为了实现多次读写jacoco数据的能力，并且每次读取的数据，仅仅为被覆盖的类数据（类关联的探针列表中有某个或是某个几个被置位了），
同时增加了jacoco客户端的Id标识，用来区分不同的应用关联的用例组的不同
-------------------------------------------------------------------------
