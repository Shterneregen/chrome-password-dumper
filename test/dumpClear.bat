@echo off
set folder=.
if exist "%drive%\jdk\bin" (set javaPath=%drive%\jdk\bin\java.exe) else (set javaPath=java)
%javaPath% -jar dump.jar -dump %folder%
cls
exit
REM pause