# Keyboard Warrior

Keyboard Warrior is a terminal typing combat game. Type `start`, choose a skill and type its
passage while a live countdown shows the time remaining in a supported terminal. Character accuracy
and typing speed determine your damage. Survive five regular scenarios, then defeat both phases of the Kernel
Overlord to win. Your starting HP depends on the selected game difficulty and carries through
the whole run without healing.

The main menu accepts numbers or names: `1. Start`, `2. Records`, `3. Exit`, `4. Choose Difficulty`,
and `5. Continue`.
Choose a game difficulty before starting a run:

| Mode | Damage and countdown multiplier | Starting HP |
| --- | --- | --- |
| Easy | x4 | 400 |
| Normal | x2 | 200 |
| Hard (default) | x1 | 100 |
| Coder | x0.8 | 80 |

The selected mode stays active between runs in the current session. Restarting the application
defaults new runs to Hard; Continue restores the saved run's difficulty. Skill choices still
determine passage difficulty.

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

The same release JAR runs on Windows, macOS and Linux with Java 25. From the folder containing
the downloaded JAR, run:

```text
java -jar KeyboardWarrior.jar
```

The release does not require the source project, Gradle or the launch scripts. A native terminal
provides the live countdown and automatic capture of unfinished typing. When a console redirects
input/output or cannot move the cursor, the game starts in plain-text mode instead: menus and
passages remain visible, and the time allowance is printed once. Plain mode waits for Enter and
scores the submitted text using its actual elapsed time, including submissions after the deadline.
It cannot reliably capture keystrokes that the console has not delivered before Enter, so it does
not automatically submit an unfinished answer.

To run from a source checkout, use the optional launchers below.
Use Java 25 and run `sh ./run-game.sh` on macOS/Linux or `.\run-game.bat` in Windows PowerShell
from the project folder. These launchers use Gradle to compile, then run Java directly in the
current terminal with assertions enabled. They do not build a JAR.

Use IntelliJ's Terminal or a native terminal for the full countdown display. Gradle `run` and
IntelliJ's Run console can redirect input and therefore use plain-text mode. On Windows, try
Windows Terminal if IntelliJ's Terminal does not support live display.

After `start`, enter a seed or press Enter on an empty line for a random seed.
Choose a displayed skill name (`Ping`, `Brute Force`, `Buffer Overflow`, `SIGKILL` or `Encryption`), then type its
passage as one line. Skills select different passage difficulties; the same typing performance gives
the same player damage for every skill. On Hard, 50 WPM and 95% character accuracy deal 40 damage.

`Encryption` gives a base 60 seconds to type random special characters in groups of at most six,
with a passage approximately as long as `SIGKILL`. After the game difficulty multiplier, its
countdown is 240 seconds on Easy, 120 on Normal, 60 on Hard, or 48 on Coder.

In a supported terminal, the timer appears above the passage. Press Enter to submit, or let the
deadline submit the text currently in the input line. Feedback shows WPM, character accuracy, elapsed time and required time;
it does not list individual wrong words. Expiry is announced with "Time's up". Passing the deadline
adds no separate damage penalty.
An enemy that survives the attack retaliates. Defeating an enemy advances automatically, with no
retaliation or healing during the transition. Clearing scenario 5 starts the final boss; victory
requires defeating its second phase. `start` after victory or defeat begins a fresh run.
Type `exit` at any phase to quit.

Your run saves automatically when an encounter starts and after each completed turn. To resume,
launch the game again and choose `5` or `continue`. Player HP, difficulty, progression, enemy HP,
and the next enemy attack are restored at skill selection. An unfinished typing challenge is retried
from the last saved turn; its text and remaining time are not saved.
The single save slot is `data/game-save.properties`; Start replaces it with a new
run, and victory or defeat removes it. Save/load errors are reported while keeping the game usable.

At the main menu, type `records` to view the latest ten typing attempts, oldest first. WPM and
accuracy are saved automatically and remain available after restarting the application.

See the [User Guide](docs/UserGuide.md) for the scoring formula, time limits and progression.

## Build automation

The project uses Gradle 9.6.1 and Java 25. Useful tasks are:

| Task | Purpose |
| --- | --- |
| `check` | Run JUnit tests and Checkstyle |
| `prepareTerminalRun` | Compile and prepare the classpath used by the terminal launchers |
| `shadowJar` | Package the application and its dependencies in `build/libs/KeyboardWarrior.jar` |

For example, use `.\gradlew.bat check` in Windows PowerShell or `./gradlew check` on macOS/Linux.
To package a demo, run the same wrapper with `shadowJar`. Run the resulting JAR with Java 25:

```text
java -ea -jar build/libs/KeyboardWarrior.jar
```

The executable JAR enables its bundled terminal library's native access through its manifest;
the additional command-line native-access option is not required for `java -jar`.
The Gradle `run` and `test` tasks enable assertions. Internal assertions check game invariants;
argument validation still throws exceptions when assertions are disabled.

## Testing and CI

Tests live under `src/test/java/seedu/keyboardwarrior` and cover typing feedback, clocks, damage,
combat, scenario and boss progression, and terminal output. Packaged-JAR smoke tests also check
startup and clean exit with redirected input/output. Run `check` with Java 25.
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
