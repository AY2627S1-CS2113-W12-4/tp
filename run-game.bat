@echo off
setlocal
rem Build with Gradle, then launch Java directly in this terminal without creating a JAR.
rem A batch launcher works from PowerShell without changing script execution policies.
pushd "%~dp0"
if errorlevel 1 exit /b 1

call gradlew.bat prepareTerminalRun --console=plain
if errorlevel 1 goto build_failed

set /p GAME_CLASSPATH=<"build\terminal-run\classpath.txt"
set /p GAME_JAVA_HOME=<"build\terminal-run\java-home.txt"
rem Keep Java in the foreground: piping its input/output would break native terminal access.
"%GAME_JAVA_HOME%\bin\java.exe" -ea --enable-native-access=ALL-UNNAMED -cp "%GAME_CLASSPATH%" seedu.keyboardwarrior.KeyboardWarrior
set "GAME_EXIT_CODE=%ERRORLEVEL%"
popd
exit /b %GAME_EXIT_CODE%

:build_failed
set "GAME_EXIT_CODE=%ERRORLEVEL%"
popd
exit /b %GAME_EXIT_CODE%
