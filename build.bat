rem @echo off
rem Java sample for DigitalPersona U.are.U SDK

set LIB_OUT_DIR=%1
set JAR_NAME=%2
set CLASS_OUT_DIR=classes

if "%LIB_OUT_DIR%" == "" set LIB_OUT_DIR=.\lib\java
if "%JAR_NAME%" == "" set JAR_NAME=OdooDigitalPersona.jar

rem check java
if "%JAVA_HOME%" == "" set JAVA_HOME=%ProgramFiles%\Java\jdk1.7.0_04
set JAVA_BIN=%JAVA_HOME%\bin

if exist "%JAVA_BIN%\javac.exe" goto javac_ok
echo "Cannot find 'javac'. check your JAVA_HOME"
exit /B 1

:javac_ok

rmdir /q /s %CLASS_OUT_DIR%
mkdir %CLASS_OUT_DIR%

set JAVAC=%JAVA_BIN%\javac.exe
set JAR=%JAVA_BIN%\jar.exe

"%JAVAC%" -g -d %CLASS_OUT_DIR% -classpath %LIB_OUT_DIR%\dpuareu.jar *.java

"%JAR%" -cvf %JAR_NAME% -C %CLASS_OUT_DIR% .\

