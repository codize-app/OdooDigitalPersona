rem @echo off
rem Java sample for DigitalPersona U.are.U SDK

rem check libs
if exist ".\lib\java\dpuareu.jar" set LIB_DIR=.\lib
if exist ".\lib\java\dpuareu.jar"  set LIB_DIR=.\lib

"%JAVA_HOME%\bin\java.exe" -classpath ".\OdooDigitalPersona.jar;%LIB_DIR%\java\dpuareu.jar" OdooDigitalPersona

