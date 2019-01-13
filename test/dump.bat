@echo off
set folder=.
set key=.\key.pub
REM set key=.\181107.0
if exist "%drive%\jdk\bin" (set javaPath=%drive%\jdk\bin\java.exe) else (set javaPath=java)
%javaPath% -jar dump.jar -d %folder% %key%
REM cls
REM exit
pause