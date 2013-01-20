#!/bin/sh -e

if [ "$1" = "" ]; then
    echo "Usage: $0 <sourceforge_username>"
    exit 1
fi

USERNAME=$1

ssh $USERNAME,eclemma@shell.sourceforge.net create
ssh $USERNAME,eclemma@shell.sourceforge.net '/home/project-web/eclemma/scripts/publish-jacoco-snapshot.sh'
