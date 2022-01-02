#!/bin/bash

# Compile and package the application with the Gradle build tool.
# Chris Joakim, Microsoft, January 2022

mkdir -p tmp/packaging/
rm   -rf tmp/packaging/
mkdir -p tmp/packaging/

echo 'clean ...'
./gradlew clean --quiet

echo 'build ...'
./gradlew build  --warning-mode all

echo 'creating deployable uberJar ...'
./gradlew uberJar

echo 'done'
