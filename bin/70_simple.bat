@echo off

setlocal ENABLEDELAYEDEXPANSION

cd ..
set basedir=C:\Users\U37285\Documents\dev\saxonstreamingchainrepro
set CP=%basedir%/target/classes;%basedir%/lib/Saxon-EE-12.5.jar;%basedir%/lib/xmlresolver-5.2.2.jar;%basedir%/license


java -classpath %CP%% -Xmx200M ch.creagy.SimpleTransformAndSplit

endlocal
echo.
