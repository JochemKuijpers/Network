@echo off 

if not exist ".\doc" mkdir ".\doc"
del /Q ".\doc\*"
if not exist ".\build" mkdir ".\build"
del /Q ".\build\*-javadoc.jar"

javadoc nl.jochemkuijpers.network -sourcepath src -d doc
cd doc 
jar cf network-xxxxxxxx-javadoc.jar *
cd ..
move .\doc\network-xxxxxxxx-javadoc.jar .\build\network-xxxxxxxx-javadoc.jar