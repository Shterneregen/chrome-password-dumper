@echo off
set folder=.
set key=.\key.pub
if exist "%drive%\jdk\bin" (set javaPath=%drive%\jdk\bin\java.exe) else (set javaPath=java)
%javaPath% -jar dump.jar -dump %folder% %key%
cls
exit
REM pause