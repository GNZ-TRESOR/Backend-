@echo off
echo Setting up Java 17 environment for Ubuzima Backend...

REM Check if Java 17 is available
java -version 2>&1 | findstr "17\." >nul
if %errorlevel% == 0 (
    echo Java 17 detected, proceeding with build...
    .\mvnw.cmd clean compile
) else (
    echo Java 17 not found. Please install Java 17 or configure your IDE to use Java 17.
    echo Current Java version:
    java -version
    echo.
    echo Please:
    echo 1. Install Java 17 from https://adoptium.net/temurin/releases/?version=17
    echo 2. Or configure your IDE to use Java 17
    echo 3. Or set JAVA_HOME to point to Java 17 installation
)

pause
