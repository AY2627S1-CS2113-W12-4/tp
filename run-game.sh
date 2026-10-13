#!/bin/sh
# Build through Gradle, then run Java directly so JLine keeps the terminal's input and output.
# The preparation task compiles classes without creating a game JAR.
set -eu

GAME_DIRECTORY=$(cd "$(dirname "$0")" && pwd)
cd "$GAME_DIRECTORY"
sh ./gradlew prepareTerminalRun --console=plain

GAME_CLASSPATH=$(cat build/terminal-run/classpath.txt)
GAME_JAVA_HOME=$(cat build/terminal-run/java-home.txt)
exec "$GAME_JAVA_HOME/bin/java" -ea --enable-native-access=ALL-UNNAMED \
    -cp "$GAME_CLASSPATH" seedu.keyboardwarrior.KeyboardWarrior
