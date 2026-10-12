# Keyboard Warrior

Keyboard Warrior is a terminal typing combat game. Type `start` at the main menu, choose a skill,
and type its prompt to attack while a live countdown shows the time remaining. Defeat the enemies
in five scenarios to win. Your typing speed and accuracy determine your damage; exceeding the
time limit reduces that damage by 20%.

## Setting up in Intellij

Prerequisites: JDK 25 (use the exact version), update Intellij to the most recent version.

1. **Ensure Intellij JDK 25 is defined as an SDK**, as described [here](https://www.jetbrains.com/help/idea/sdk.html#set-up-jdk) -- this step is not needed if you have used JDK 25 in a previous Intellij project.
1. **Import the project _as a Gradle project_**, as described [here](https://se-education.org/guides/tutorials/intellijImportGradleProject.html).
1. **Enable assertions for a direct IntelliJ run**: Open `Run > Edit Configurations`, select the `KeyboardWarrior` Application configuration, choose `Modify options > Add VM options`, and enter `-ea` in **VM options**. This is a JVM option, so do not put it in Program arguments. See the [IntelliJ Application configuration guide](https://www.jetbrains.com/help/idea/run-debug-configuration-java-application.html). If IntelliJ runs through Gradle, assertions are already enabled by `build.gradle`.
1. **Verify the setup**: After the importing is complete, locate the `src/main/java/seedu/keyboardwarrior/KeyboardWarrior.java` file, right-click it, and choose `Run KeyboardWarrior.main()`. You should see the main menu. Type `start` and press Enter to begin the first scenario, or `exit` to close the game.

**Warning:** Keep the `src\main\java` folder as the root folder for Java files (i.e., don't rename those folders or move Java files to another folder outside of this folder path), as this is the default location some tools (e.g., Gradle) expect to find Java files.

## Running v1.0

Use Java 25 and run `./gradlew run` on macOS/Linux or `.\gradlew.bat run` in Windows PowerShell.
Choose a skill by its displayed name (`Ping`, `Brute Force`, `Buffer Overflow` or `SIGKILL`),
then submit the displayed prompt as one line with Enter. The live timer shows seconds remaining
above the typing instructions and passage while you type. If time expires before you press Enter,
the game evaluates the text currently in your input line for WPM and accuracy. A missed deadline
reduces the attack's damage by 20% and lets the enemy take its turn. Feedback shows the metrics
without listing individual wrong words. Passages and enemies are selected randomly from varied pools.
Combat repeats until the enemy or player is defeated. Enemy defeats advance automatically to
the next scenario; clearing the fifth wins the run. Your health carries between scenarios.
After victory or defeat, `start` begins a fresh run from the menu. Type `exit` at any phase to quit.

See the [User Guide](docs/UserGuide.md) for the skill time limits and typing feedback.

## Build automation using Gradle

* This project uses Gradle for build automation and dependency management. It includes a basic build script as well (i.e. the `build.gradle` file).
* The Gradle `run` and `test` tasks enable assertions. Internal assertions check game invariants; argument validation still throws exceptions when assertions are disabled.
* If you are new to Gradle, refer to the [Gradle Tutorial at se-education.org/guides](https://se-education.org/guides/tutorials/gradle.html).

## Testing

### JUnit tests

* Run `./gradlew check` or `.\gradlew.bat check` with Java 25 for JUnit tests and Checkstyle.
* Tests are under `src/test/java/seedu/keyboardwarrior`, covering typing feedback, clocks,
  combat, gameplay progression and terminal output.
* If you are new to JUnit, refer to the [JUnit Tutorial at se-education.org/guides](https://se-education.org/guides/tutorials/junit.html).

## Checkstyle

* A sample CheckStyle rule configuration is provided in this project.
* If you are new to Checkstyle, refer to the [Checkstyle Tutorial at se-education.org/guides](https://se-education.org/guides/tutorials/checkstyle.html).

## CI using GitHub Actions

The project uses [GitHub actions](https://github.com/features/actions) for CI. When you push a commit to this repo or PR against it, GitHub actions will run automatically to build and verify the code as updated by the commit/PR.

## Documentation

`/docs` folder contains a skeleton version of the project documentation.

Steps for publishing documentation to the public: 
1. If you are using this project template for an individual project, go your fork on GitHub.<br>
   If you are using this project template for a team project, go to the team fork on GitHub.
1. Click on the `settings` tab.
1. Scroll down to the `GitHub Pages` section.
1. Set the `source` as `master branch /docs folder`.
1. Optionally, use the `choose a theme` button to choose a theme for your documentation.
