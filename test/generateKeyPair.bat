@echo off
if exist "%drive%\jdk\bin" (set javaPath=%drive%\jdk\bin\java.exe) else (set javaPath=java)
%javaPath% -jar dump.jar -g
cls
exit
REM pause