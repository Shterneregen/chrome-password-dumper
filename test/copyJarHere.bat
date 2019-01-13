set jarFile=dump.jar
echo f | xcopy /y ..\build\libs\%jarFile% .\%jarFile%
REM pause