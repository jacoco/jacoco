#!/bin/bash

set -euo pipefail

# Prevent accidental execution outside of Travis:
if [ -z "${TRAVIS+false}" ]
then
  echo "TRAVIS environment variable is not set"
  exit 1
fi

function jdk_switcher {
  DIR=$1
  if [ ! -d "$DIR" ]; then
    echo "Not found: $DIR"
    exit 1
  fi
  export JAVA_HOME="$DIR"
  export JDK_HOME="${JAVA_HOME}"
  export JAVAC="${JAVA_HOME}/bin/javac"
  export PATH="${JAVA_HOME}/bin:${PATH}"
}

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
    jdk_switcher "/tmp/jdk/$JDK"
  fi
}

case "$JDK" in
5)
  install_jdk $JDK5_URL false
  ;;
6)
  ;;
7)
  jdk_switcher /usr/lib/jvm/java-7-openjdk-amd64
  ;;
8)
  jdk_switcher /usr/lib/jvm/java-8-oracle
  ;;
8-ea)
  install_jdk $JDK8_EA_URL
  ;;
9)
  install_jdk $JDK9_URL
  ;;
10-ea)
  install_jdk $JDK10_EA_URL
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
    mvn -V -B -e -f org.jacoco.build verify deploy:deploy -DdeployAtEnd -Djdk.version=1.5 --toolchains=./.travis/toolchains.xml --settings=./.travis/settings.xml -Dsonar.host.url=${SONARQUBE_URL} -Dsonar.login=${SONARQUBE_TOKEN}
    python ./.travis/trigger-site-deployment.py
  else
    mvn -V -B -e verify -Djdk.version=1.5 --toolchains=./.travis/toolchains.xml
  fi
  ;;
6)
  mvn -V -B -e verify -Djdk.version=1.6 -Dbytecode.version=1.6 --toolchains=./.travis/travis-toolchains.xml
  ;;
7)
  mvn -V -B -e verify -Dbytecode.version=1.7
  ;;
8 | 8-ea)
  mvn -V -B -e verify -Dbytecode.version=1.8 -Decj=${ECJ:-}
  ;;
9)
  export MAVEN_OPTS="-Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts"
  mvn -V -B -e verify -Dbytecode.version=1.9 \
    -Dinvoker.mavenOpts="-Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts"
  ;;
10-ea)
  mvn -V -B -e verify -Dbytecode.version=1.9
  ;;
*)
  echo "Incorrect JDK [$JDK]"
  exit 1;
  ;;
esac
