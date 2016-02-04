#!/bin/sh

set -e

rm -rf gh-pages
git clone --single-branch --branch gh-pages https://github.com/jacoco/jacoco.git gh-pages
cd gh-pages/trunk
unzip -o ../../jacoco/target/jacoco*.zip
rm -r lib

git add .
git commit -m "Automatic deployment"

git push "https://${GH_TOKEN}@github.com/jacoco/jacoco.git" gh-pages > /dev/null 2>&1
