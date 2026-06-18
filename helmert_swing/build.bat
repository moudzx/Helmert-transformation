@echo off
if not exist build mkdir build
dir /s /b src\*.java > sources.txt
javac -d build @sources.txt
if %errorlevel% neq 0 (
    echo Build failed.
    exit /b 1
)
echo Build successful.
jar cfe helmert.jar helmert.HelmertApp -C build .
echo JAR created: helmert.jar
