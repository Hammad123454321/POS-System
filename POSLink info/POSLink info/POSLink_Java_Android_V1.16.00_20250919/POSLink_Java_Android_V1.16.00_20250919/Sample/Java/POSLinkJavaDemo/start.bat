echo off
echo Make sure you have installed JRE
start javaw -jar POSLinkJavaDemo.jar
echo starting...
REM Sleep
ping -n 4 127.0.0.1>nul
