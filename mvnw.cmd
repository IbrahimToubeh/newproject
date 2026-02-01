# Maven Wrapper script for Windows
@REM This script will download Maven if needed and run the specified goal

@echo off
setlocal

set MAVEN_PROJECTBASEDIR=%~dp0

if "%MAVEN_WRAPPERJAR%"=="" (
    set MAVEN_WRAPPERJAR=".mvn\wrapper\maven-wrapper.jar"
)

if not exist %MAVEN_WRAPPERJAR% (
    echo Error: Unable to find Maven wrapper jar
    exit /b 1
)

java -jar %MAVEN_WRAPPERJAR% %*
