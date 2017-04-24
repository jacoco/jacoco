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
  jdk_switcher use oraclejdk8
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
9-ea-stable)
  install_jdk $JDK9_EA_STABLE_URL
  ;;
esac

# Do not use "~/.mavenrc" set by Travis (https://github.com/travis-ci/travis-ci/issues/3893),
# because it prevents execution of JaCoCo during integration tests for jacoco-maven-plugin,
# and "-XMaxPermSize" not supported by JDK 9
export MAVEN_SKIP_RC=true

# Build:
# TODO(Godin): see https://github.com/jacoco/jacoco/issues/300 about "bytecode.version"
case "$JDK" in
5)
  if [[ ${TRAVIS_PULL_REQUEST} == 'false' && ${TRAVIS_BRANCH} == 'master' ]]
  then
    # goal "deploy:deploy" used directly instead of "deploy" phase to avoid pollution of Maven repository by "install" phase
    mvn -V -B -e -f org.jacoco.build verify sonar:sonar deploy:deploy -DdeployAtEnd -Djdk.version=1.5 --toolchains=./.travis/toolchains.xml --settings=./.travis/settings.xml -Dsonar.host.url=${SONARQUBE_URL} -Dsonar.login=${SONARQUBE_TOKEN}
    python ./.travis/trigger-site-deployment.py
  else
    mvn -V -B -e verify -Djdk.version=1.5 --toolchains=./.travis/toolchains.xml
  fi
  ;;
6)
  mvn -V -B -e verify -Dbytecode.version=1.6
  ;;
7)
  mvn -V -B -e verify -Dbytecode.version=1.7
  ;;
8 | 8-ea)
  mvn -V -B -e verify -Dbytecode.version=1.8 -Decj=${ECJ:-}
  ;;
9-ea | 9-ea-stable)
  # Groovy version should be updated to get rid of "--add-opens" options (see https://twitter.com/CedricChampeau/status/807285853580103684)
  export MAVEN_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED"

  # see https://bugs.openjdk.java.net/browse/JDK-8131041 about "java.locale.providers"
  mvn -V -B -e verify -Dbytecode.version=1.9 \
    -DargLine=-Djava.locale.providers=JRE,SPI
  ;;
*)
  echo "Incorrect JDK [$JDK]"
  exit 1;
  ;;
esac
