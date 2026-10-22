# Keyboard Warrior

Keyboard Warrior is a terminal typing combat game. Type `start`, choose a skill and type its
passage while a live countdown shows the time remaining. Character accuracy and typing speed
determine your damage. Survive five regular scenarios, then defeat both phases of the Kernel
Overlord to win. Your 100 starting HP carries through the whole run without healing.

## Setting up in IntelliJ

Prerequisites: JDK 25 (use the exact major version) and an IntelliJ version that supports it.

1. Define JDK 25 as the project SDK, following the [IntelliJ SDK guide](https://www.jetbrains.com/help/idea/sdk.html#set-up-jdk).
2. Import the project [as a Gradle project](https://se-education.org/guides/tutorials/intellijImportGradleProject.html).
3. Set the Gradle JVM to JDK 25. Run the Gradle `check` task to execute the tests and Checkstyle.
4. For gameplay, open IntelliJ's Terminal and use the launcher below. Enter `start` at the main menu,
   or `exit` to close the game.

The launchers enable assertions. For a direct IntelliJ Application configuration, add `-ea` to
**VM options** under `Run > Edit Configurations > Modify options > Add VM options`.
Gradle's `run` and `test` tasks also enable assertions.

Keep Java sources under `src/main/java`; Gradle expects that source layout.

## Running the game

Use Java 25 and run `sh ./run-game.sh` on macOS/Linux or `.\run-game.bat` in Windows PowerShell
from the project folder. These launchers use Gradle to compile, then run Java directly in the
current terminal with assertions enabled. They do not build a JAR.

Use IntelliJ's Terminal or a native terminal for gameplay. Gradle `run` and IntelliJ's Run console
can redirect input, preventing the live countdown and capture of text before Enter. The game checks
terminal capabilities at startup and shows the launcher command when the console is unsupported.
On Windows, try Windows Terminal if IntelliJ's Terminal remains unsupported.

Choose a displayed skill name (`Ping`, `Brute Force`, `Buffer Overflow` or `SIGKILL`), then type its
passage as one line. Skills select different passage difficulties; the same typing performance gives
the same player damage for every skill. At 50 WPM and 95% character accuracy, an attack deals 40 damage.

The timer appears above the passage. Press Enter to submit, or let the deadline submit the text
currently in the input line. Feedback shows WPM, character accuracy, elapsed time and required time;
it does not list individual wrong words. Expiry is announced with "Time's up". Passing the deadline
adds no separate damage penalty.
An enemy that survives the attack retaliates. Defeating an enemy advances automatically, with no
retaliation or healing during the transition. Clearing scenario 5 starts the final boss; victory
requires defeating its second phase. `start` after victory or defeat begins a fresh run.
Type `exit` at any phase to quit.

See the [User Guide](docs/UserGuide.md) for the scoring formula, time limits and progression.

## Build automation

The project uses Gradle 9.6.1 and Java 25. Useful tasks are:

| Task | Purpose |
| --- | --- |
| `check` | Run JUnit tests and Checkstyle |
| `prepareTerminalRun` | Compile and prepare the classpath used by the terminal launchers |
| `shadowJar` | Package the application and its dependencies in `build/libs/KeyboardWarrior.jar` |

For example, use `.\gradlew.bat check` in Windows PowerShell or `./gradlew check` on macOS/Linux.
To package a demo, run the same wrapper with `shadowJar`. Run the resulting JAR in a supported
terminal with Java 25:

```text
java -ea --enable-native-access=ALL-UNNAMED -jar build/libs/KeyboardWarrior.jar
```

The Gradle `run` and `test` tasks enable assertions. Internal assertions check game invariants;
argument validation still throws exceptions when assertions are disabled.

## Testing and CI

Tests live under `src/test/java/seedu/keyboardwarrior` and cover typing feedback, clocks, damage,
combat, scenario and boss progression, and terminal output. Run `check` with Java 25.
[GitHub Actions](.github/workflows/gradle.yml) runs the same checks on Windows, macOS and Linux
for pushes and pull requests. Checkstyle rules are in `config/checkstyle`.

The [Developer Guide](docs/DeveloperGuide.md) explains the design, automated checks and manual
acceptance tests. Introductory guides are available for
[Gradle](https://se-education.org/guides/tutorials/gradle.html),
[JUnit](https://se-education.org/guides/tutorials/junit.html) and
[Checkstyle](https://se-education.org/guides/tutorials/checkstyle.html).

## Documentation

The `docs` folder contains the [User Guide](docs/UserGuide.md),
[Developer Guide](docs/DeveloperGuide.md) and team pages. To publish it through GitHub Pages,
select the repository's `master` branch and `/docs` folder under `Settings > Pages`.
