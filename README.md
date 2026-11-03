# Keyboard Warrior

Keyboard Warrior is a Java 25 terminal adventure built around typing practice. Complete five
scenarios by choosing combat, puzzles, reaction challenges or a merchant conversation, then defeat
both phases of the Kernel Overlord. Combat damage follows typing speed and character accuracy;
events can award Overclock, heal the player or reduce future typing allowances.

Choose Easy, Normal, Hard or Coder mode, replay a seed, and resume saved progress. The main menu
accepts numbers or names: `1. Start`, `2. Records`, `3. Exit`, `4. Choose Difficulty`, `5. Continue`
and `6. Leaderboard`. Records shows the latest ten typing responses; Leaderboard retains one
all-time best response.

## Running a release

Download a JAR from the [releases page](https://github.com/AY2627S1-CS2113-W12-4/tp/releases).
With JDK 25 installed, open a terminal in the JAR's writable folder and run:

```text
java -jar "KeyboardWarrior.jar"
```

Substitute the downloaded filename if it differs. The same self-contained JAR runs on Windows,
macOS and Linux. It does not require Gradle, the source project or the launch scripts.

Enter `start`, then a seed or a blank line for a random seed. Each regular scenario offers three
encounters: `1` is combat, `2` is an event, and `3` may be either. In combat, enter a displayed skill
name and type its passage. For an event, solve the puzzle or follow the displayed response.
Surviving an event advances the scenario even if its response incurs a penalty. The final boss
starts after scenario five; both phases must be defeated to win.

| Mode | Damage and countdown multiplier | Starting HP |
| --- | --- | --- |
| Easy | x4 | 400 |
| Normal | x2 | 200 |
| Hard (default) | x1 | 100 |
| Coder | x0.8 | 80 |

On Hard, 50 WPM and 95% character accuracy deal 40 combat damage. Overclock is earned from a
successful puzzle or trade and adds time to later combat and puzzle challenges. Event healing
can raise HP above starting health; changing scenario or boss phase gives no automatic healing.

Supported terminals show a live timer above the instructions and capture unfinished input at
expiry. Other consoles use plain-text mode, which waits for Enter and measures the actual elapsed
time. Reaction commands appear automatically in both modes. Combat keeps its WPM/accuracy damage
when late; event responses must meet their deadline. Use a native terminal, Windows Terminal or
IntelliJ's Terminal for live capture. Gradle `run` and IDE Run consoles may use plain mode.

Progress saves automatically, including displayed choices, event stage, learned skills, time
adjustment, HP and future random selections. Enter `exit` to quit, then restart from the same
folder and choose `continue`. Unfinished text and response timers are reset on Continue. Start
replaces the old save; victory or defeat removes it. History and the leaderboard remain.

The [User Guide](docs/UserGuide.md) covers event rules, deadlines, scoring and data-file behavior.

## Setting up in IntelliJ

1. Install JDK 25 and [define it as the project SDK](https://www.jetbrains.com/help/idea/sdk.html#set-up-jdk).
2. [Import the project as Gradle](https://se-education.org/guides/tutorials/intellijImportGradleProject.html)
   and select JDK 25 as the Gradle JVM.
3. Run Gradle's `check` task for tests and Checkstyle.
4. For gameplay, open the terminal and run `sh ./run-game.sh` on macOS/Linux or `.\run-game.bat`
   in Windows PowerShell from the project folder.

The optional source launchers compile through Gradle, then start Java directly with assertions
and native terminal access enabled. They do not build a JAR. For an IntelliJ Application run,
add `-ea` to **VM options** to enable assertions. Keep Java sources under `src/main/java`.

## Build automation and tests

Use JDK 25 with the Gradle wrapper. On Windows, replace `./gradlew` below with `.\gradlew.bat`.

| Command | Purpose |
| --- | --- |
| `./gradlew check` | Build the executable JAR, run JUnit and packaged-JAR smoke tests, and run Checkstyle |
| `./gradlew prepareTerminalRun` | Prepare compiled classes for the source launchers |
| `./gradlew shadowJar` | Package the application and dependencies into `build/libs/KeyboardWarrior.jar` |
| `./gradlew jacocoTestReport` | Generate test-coverage reports |

Run a packaged development build with `java -ea -jar build/libs/KeyboardWarrior.jar`. The JAR
manifest enables the bundled terminal library's native access; a separate command-line
native-access flag is not required. Gradle's `run` and `test` tasks enable assertions.

Tests under `src/test/java/seedu/keyboardwarrior` cover typing, clocks, combat, events, rewards,
encounter choices, both boss phases, saves, seeded continuation, menus and terminal behavior.
Packaged-JAR smoke tests launch from temporary folders with redirected input/output, checking
startup and clean shutdown. [Java CI](.github/workflows/gradle.yml) runs `check` on Windows,
macOS and Linux for pushes and pull requests. Checkstyle rules live under `config/checkstyle`.

## Documentation and website

- [User Guide](docs/UserGuide.md): installation, commands and gameplay.
- [Developer Guide](docs/DeveloperGuide.md): design, UML, persistence and verification.
- [Product website](https://ay2627s1-cs2113-w12-4.github.io/tp/).
- [About Us](docs/AboutUs.md) and individual project portfolios.

The website source is `docs`. The [documentation workflow](.github/workflows/pages.yml) builds
every guide, About Us and PPP directly from those Markdown files, checks local links, and deploys
the checked artifact on pushes to `master`. Set `Settings > Pages > Build and deployment > Source`
to **GitHub Actions** and enable the workflow. No separately maintained HTML copies are needed.
The published `source-manifest.json` records each page's source hash so a stale deployment can be
identified. UG and DG are published as web pages. Your individual PPP also has a PDF for final submission.

To preview the website locally, use Node.js 24 and run:

```text
npm ci --prefix .github/docs --ignore-scripts
node .github/docs/build.mjs docs build/docs-site
```

Open `build/docs-site/index.html`. The build checks links and does not change the Markdown sources.
