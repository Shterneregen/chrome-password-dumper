@echo off
set folder=.
REM set key=.\key.pub
set key=.\181107
if exist "%drive%\jdk\bin" (%drive%\jdk\bin\java -jar dump.jar %folder% %key%) else (java -jar dump.jar %folder% %key%)
cls
exit
REM pause