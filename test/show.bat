@echo off
chcp 65001 2>nul >nul
if exist "%drive%\jdk\bin" (set javaPath=%drive%\jdk\bin\java.exe) else (set javaPath=java)
%javaPath% "-Dfile.encoding=UTF8" -jar dump.jar -sh
pause