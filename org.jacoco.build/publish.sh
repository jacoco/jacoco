#!/bin/sh -e

if [ "$1" = "" ]; then
    echo "Usage: $0 <sourceforge_username>"
    exit 1
fi

USERNAME=$1
BASEDIR="$(cd "`dirname $0`" && pwd)"

scp $BASEDIR/../org.jacoco.doc/target/jacoco-*.zip $USERNAME,eclemma@web.sourceforge.net:/home/frs/project/e/ec/eclemma/07_JaCoCo/trunk
ssh $USERNAME,eclemma@shell.sourceforge.net create
ssh $USERNAME,eclemma@shell.sourceforge.net '/home/project-web/eclemma/scripts/publish-jacoco-trunk.sh'
