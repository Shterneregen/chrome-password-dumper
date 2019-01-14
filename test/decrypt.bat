@echo off
chcp 65001 2>nul >nul

REM set file=.\text.txt
set key=.\key.key
set /p file=Enter file name with encoded passwords: 

if exist "%drive%\jdk\bin" (set javaPath=%drive%\jdk\bin\java.exe) else (set javaPath=java)
%javaPath% "-Dfile.encoding=UTF8" -jar dump.jar -df %key% %file%
REM java -jar coder.jar -df %key% %file%
cls
exit
REM pause