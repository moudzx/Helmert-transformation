#!/bin/sh
mkdir -p build
find src -name "*.java" > sources.txt
javac -d build @sources.txt
if [ $? -eq 0 ]; then
    echo "Build successful."
    jar cfe helmert.jar helmert.HelmertApp -C build .
    echo "JAR created: helmert.jar"
else
    echo "Build failed."
    exit 1
fi
