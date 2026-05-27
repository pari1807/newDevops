@echo off
REM Smart File Management System - Build and Run Script (Windows)
REM This script builds the project and starts the server

setlocal enabledelayedexpansion

echo.
echo ======================================================
echo Smart File Management System - Build and Run
echo ======================================================
echo.

REM Check for Java
echo Checking Java installation...
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java is not installed or not in PATH
    echo Please install Java 11 or higher and add it to PATH
    pause
    exit /b 1
)

for /f "tokens=*" %%A in ('java -version 2^>^&1 ^| findstr /R "version"') do set JAVA_VERSION=%%A
echo [OK] Java: %JAVA_VERSION%

REM Check for Maven
echo Checking Maven installation...
mvn -v >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Maven is not installed or not in PATH
    echo Please install Maven and add it to PATH
    pause
    exit /b 1
)

for /f "tokens=*" %%A in ('mvn -v 2^>^&1 ^| findstr /R "Apache Maven"') do set MAVEN_VERSION=%%A
echo [OK] Maven: %MAVEN_VERSION%

REM Create uploads directory
echo.
echo Creating uploads directory...
if not exist "uploads" (
    mkdir uploads
    echo [OK] Created uploads directory
) else (
    echo [OK] uploads directory already exists
)

REM Clean previous builds
echo.
echo Cleaning previous builds...
call mvn clean -q
if errorlevel 1 (
    echo [ERROR] Maven clean failed
    pause
    exit /b 1
)
echo [OK] Clean complete

REM Build project
echo.
echo Building project with Maven...
call mvn package -q -DskipTests
if errorlevel 1 (
    echo [ERROR] Maven build failed
    pause
    exit /b 1
)
echo [OK] Build complete

REM Check if JAR exists
if not exist "target\smart-file-management.jar" (
    echo [ERROR] Build failed! JAR not found.
    pause
    exit /b 1
)

REM Start server
echo.
echo Starting Smart File Management Server...
echo.
echo ======================================================
echo Server is starting... Please wait
echo ======================================================
echo.

java -jar target\smart-file-management.jar

pause
