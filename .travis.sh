#!/bin/bash

set -euo pipefail

# Prevent accidental execution outside of Travis:
if [ -z "${TRAVIS+false}" ]
then
  echo "TRAVIS environment variable is not set"
  exit 1
fi

# Switch to desired JDK, download if required:
function install_jdk {
  JDK_URL=$1

  FILENAME="${JDK_URL##*/}"

  rm -rf /tmp/jdk/$JDK
  mkdir -p /tmp/jdk/$JDK

  if [ ! -f "/tmp/jdk/$FILENAME" ]
  then
    curl -L $JDK_URL -o /tmp/jdk/$FILENAME
  fi

  tar -xzf /tmp/jdk/$FILENAME -C /tmp/jdk/$JDK --strip-components 1

  if [ -z "${2+false}" ]
  then
    export JAVA_HOME="/tmp/jdk/$JDK"
    export JDK_HOME="${JAVA_HOME}"
    export JAVAC="${JAVA_HOME}/bin/javac"
    export PATH="${JAVA_HOME}/bin:${PATH}"
  fi
}

source $HOME/.jdk_switcher_rc
case "$JDK" in
5)
  install_jdk $JDK5_URL false
  ;;
6)
  jdk_switcher use openjdk6
  ;;
7|8)
  jdk_switcher use oraclejdk${JDK}
  ;;
8-ea)
  install_jdk $JDK8_EA_URL
  ;;
9-ea)
  install_jdk $JDK9_EA_URL
  ;;
esac

# Build:
# TODO(Godin): see https://github.com/jacoco/jacoco/issues/300 about "bytecode.version"
case "$JDK" in
5)
  mvn -V -B -e verify -Djdk.version=1.5 --toolchains=./.travis/toolchains.xml
  ;;
6)
  mvn -V -B -e verify -Dbytecode.version=1.6
  ;;
7)
  mvn -V -B -e verify -Dbytecode.version=1.7
  ;;
8 | 8-ea)
  mvn -V -B -e verify -Dbytecode.version=1.8
  ;;
9-ea)
  # "-XMaxPermSize" not supported by JDK 9, so remove it from MAVEN_OPTS set by Travis (https://github.com/travis-ci/travis-ci/issues/3893)
  echo "export MAVEN_OPTS='-Dmaven.repo.local=$HOME/.m2/repository'" > ~/.mavenrc
  # see https://bugs.openjdk.java.net/browse/JDK-8131041 about "java.locale.providers"
  # TODO(Godin): maven-javadoc-plugin doesn't work well due to modularization of JDK 9 - skip it and hence distribution
  mvn -V -B -e verify -Dbytecode.version=1.9 \
    -Dmaven.javadoc.skip -pl !jacoco \
    -DargLine=-Djava.locale.providers=JRE,SPI
  ;;
*)
  echo "Incorrect JDK [$JDK]"
  exit 1;
  ;;
esac
