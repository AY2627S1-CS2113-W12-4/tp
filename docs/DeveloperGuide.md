# Keyboard Warrior Developer Guide

## Acknowledgements

The project uses the [CS2113 tP starter template](https://github.com/NUS-CS2113-AY2627-S1/tp).
Typing feedback uses the Java standard library; its automated tests use the template's JUnit Jupiter dependency.

## Design and Implementation

### v1.0 gameplay loop

`KeyboardWarrior.java` checks terminal support before starting the main menu and JLine input loop.
`CountdownReader.supportsLiveDisplay()` checks the capabilities needed to redraw the prompt.
Basic or redirected consoles are rejected with a friendly message and gameplay launcher commands,
so countdown updates cannot append digits to input or evaluate an unavailable input buffer at expiry.
A background reader queues complete lines, while the main thread handles input and updates the state machine
every 100 milliseconds. This lets the countdown refresh while the player edits a submission.

`MainMenuState` accepts `start` to enter `GamePlayState`, which creates a player with 100 HP.
Gameplay starts five combat scenarios in sequence, choosing a random enemy from `EnemyFactory`
for each scenario. The same player continues across scenarios, so health carries over.
Enemy defeat advances immediately; player defeat or clearing
all five scenarios returns to the menu. A new `start` creates a fresh run. `exit` stops the game
from the menu, skill selection or typing challenge.

On each player turn, `GamePlayState` accepts a skill's display name and calls
`CombatEncounter.preparePlayerTurn()` to generate its prompt and time limit. It displays the
prompt, starts `ChallengeClock`, and refreshes the remaining seconds through `StateUi` and
`CountdownReader`. The timer, time limit, typing instructions, passage and input line appear
together in the JLine prompt, in that order, so the live timer stays above the passage.
Submitting one line evaluates that attempt, including imperfect answers. On expiry, gameplay
captures the current editable input and clears it before asking for the next skill. That captured
text is evaluated as an overdue attempt: WPM and accuracy reflect what the player typed,
and the resulting damage is reduced by 20%.

`CombatEncounter.begin()`, `preparePlayerTurn()` and `submitTypingResult()` let the state machine
drive combat without blocking input or timer updates. The encounter resolves the player's attack,
checks for victory, then resolves the enemy's attack if it survives and checks for defeat.
The synchronous `CombatEncounter.start()` path remains available to `SampleCombatRun`.

`SkillResolver` calculates player damage as
`(int) (baseDamage * (accuracyPercent + wpm) / 100.0 * result.getTimeDamageMultiplier())`.
The multiplier is `0.8` when elapsed time exceeds required time and `1.0` otherwise. Damage is
rounded down once, after the penalty is applied. Enemy turns apply base damage directly.
`TypingChallengeFactory` gives Ping 15 seconds, Brute Force and Buffer Overflow 30 seconds,
and SIGKILL 45 seconds. It randomly selects a passage from the skill's difficulty pool.
`EnemyFactory` retains named enemy creation and supplies random enemy selection for gameplay.
The run shares one prompt factory, avoiding consecutive repeated passages at the same difficulty.
Enemies and previously used passages can appear again later. `CombatUi` displays combat messages through the terminal-aware
`StateUi`, preserving the player's current input while messages and countdown updates appear.
On a Windows console, the entry point selects the native Windows terminal type even if a launcher
sets `TERM=dumb`. Native input delivers keystrokes before Enter, allowing timeout capture.

`run-game.bat` (Windows PowerShell or Command Prompt) and `run-game.sh` (macOS/Linux) first run Gradle's
`prepareTerminalRun` task. The task depends on `classes`, writes `sourceSets.main.runtimeClasspath`
and the build JVM's Java home to files, and does not build a JAR. The launcher reads these files,
then starts Java in the foreground with `-ea`, retaining the current terminal's input and output.
This avoids the redirected streams used by Gradle `run` or an IDE Run console. IntelliJ's Terminal
can use the launcher; if it lacks the required cursor capabilities, use a supported native terminal.

### Typing feedback: tasks 3.1 and 3.2

Typing features are separate classes in the `seedu.keyboardwarrior.typing` package,
under `src/main/java/seedu/keyboardwarrior/typing`.

| File | Responsibility |
| --- | --- |
| `WpmCalculator.java` | Calculates gross WPM from character count and elapsed seconds |
| `AccuracyCalculator.java` | Calculates accuracy from matching positions and total positions |
| `WordErrorFinder.java` | Finds wrong, missing and extra words |
| `TypingEvaluator.java` | Validates the four inputs and combines the typing metrics with a time limit |
| `TypingResultFormatter.java` | Formats gameplay summaries and optional detailed reports with word errors |
| `TypingResult.java` | Stores immutable metrics, the time limit and word errors; exposes time-limit status and a damage multiplier |
| `WordError.java` | Stores one positional word error |
| `ErrorType.java` | Defines the wrong, missing and extra error categories |

The calculators and finder are package-private implementation helpers. Other game components use
the public evaluator and formatter; result types are public top-level classes rather than nested in the entry class.

The gameplay integration methods are:

```java
import seedu.keyboardwarrior.typing.TypingEvaluator;
import seedu.keyboardwarrior.typing.TypingResult;
import seedu.keyboardwarrior.typing.TypingResultFormatter;

TypingResult result = TypingEvaluator.evaluateTyping(expectedText, typedText, elapsedSeconds, requiredSeconds);
String report = TypingResultFormatter.formatTypingSummary(result);
```

The scenario component supplies `expectedText` and `requiredSeconds`, and the input/timer component
supplies `typedText` and `elapsedSeconds`. Both time values are in seconds. The CLI can print `report`.
The gameplay summary omits individual wrong words, but `result.getWordErrors()` and
`TypingResultFormatter.formatTypingResult(result)` remain available for future detailed feedback.
Combat code uses `result.getTimeDamageMultiplier()` in its damage calculation. This returns `0.8`
for late attempts and `1.0` otherwise; `result.isTimeLimitExceeded()` reports whether the attempt is late.
For example, an attack worth 40 damage before the penalty deals 32 damage when late.
Evaluating and formatting do not read input, measure time, print output,
change gameplay state or save files. `GamePlayState` supplies the prompt, submitted or captured input and
elapsed duration, and forwards the result to the encounter for damage resolution.

#### Input and output contract

| Input | Requirement |
| --- | --- |
| `expectedText` | Non-null and must contain a word after normalization |
| `typedText` | Non-null; empty or whitespace-only submissions are allowed |
| `elapsedSeconds` | A positive, finite `double` in seconds, supplied by the timer |
| `requiredSeconds` | A positive, finite `double` defining the challenge's time limit in seconds |

The gameplay timer starts when the typing prompt is shown and stops on submission or expiry.
Skill selection, previous feedback and scenario transitions are outside that duration.
The evaluator consumes the supplied value without performing timing itself.

| Result getter | Meaning |
| --- | --- |
| `getWpm()` | Gross WPM as a full-precision `double` |
| `getAccuracyPercent()` | Word accuracy between 0 and 100 |
| `getElapsedSeconds()` | The supplied typing duration |
| `getRequiredSeconds()` | The supplied challenge time limit |
| `isTimeLimitExceeded()` | `true` when elapsed time is strictly greater than required time |
| `getTimeDamageMultiplier()` | `0.8` when the limit is exceeded, otherwise `1.0` |
| `getWordErrors()` | An unmodifiable list of errors in ascending position order |

Each error exposes `getPosition()`, `getExpectedWord()`, `getTypedWord()` and `getType()`.
Positions start at 1. Types are `WRONG`, `MISSING` and `EXTRA`; an absent word is represented by `""`.
Result fields and error fields are immutable, and the result retains a defensive copy of the error list.

#### Evaluation rules

1. Normalize both texts with `trim()` and `replaceAll("\\s+", " ")`. Spaces, tabs and line breaks are
   collapsed; capitalization and punctuation are preserved.
2. Calculate gross WPM as `(normalizedTyped.length() / 5.0) / (elapsedSeconds / 60.0)`.
   Normalized internal spaces and incorrect characters are included. The current character count
   uses Java `String.length()` (UTF-16 code units), appropriate for the intended English challenges.
3. Split normalized text into words. An empty submission has zero words.
4. Compare words at the same index, counting exact matches and recording wrong, missing or extra words.
5. Calculate accuracy as `matchingPositions * 100.0 / max(expectedWordCount, typedWordCount)`.
   The nonempty challenge guarantees that the denominator is positive.
6. Use a time damage multiplier of `0.8` when `elapsedSeconds > requiredSeconds`, and `1.0` otherwise.
   Finishing exactly at the time limit has no penalty. Late submissions preserve WPM, accuracy and
   word feedback, and combat reduces their calculated damage by 20%. Empty input still deals zero
   damage because both WPM and accuracy are zero.

This deliberately uses positional matching. Missing or inserted middle words can cause later errors;
the component does not attempt to realign the text. Only final submitted or captured text is evaluated, so
previously corrected keystrokes are not available to this component.

Null text inputs cause `NullPointerException`. Blank challenges and zero, negative, NaN or infinite
elapsed or required times cause `IllegalArgumentException`. These indicate invalid integration data; empty player
input is a valid result instead of an exception.

`TypingResultFormatter.formatTypingSummary` uses `Locale.ROOT` for consistent decimal points and the system
line separator for console output. It displays WPM, accuracy and both times to two decimal places, followed by
`Damage penalty: 0%` or `Damage penalty: 20% (time limit exceeded)`. Gameplay prints this summary without word errors.
The retained `formatTypingResult` method appends the word errors, or `None` when the input is correct.
Formatting leaves the stored numeric values and error list unchanged;
the time limit is checked using full precision rather than rounded display values.

### Defensive programming and diagnostics

Combat entities, skills, typing challenges and results validate their arguments before storing data. Null references cause
`NullPointerException`; invalid values such as blank names, negative damage or health, and invalid
typing metrics or times cause `IllegalArgumentException`. Invalid combat or state-machine sequencing causes
`IllegalStateException`. These checks are active even when assertions are disabled. Empty player
input remains valid and is evaluated normally.

Assertions check internal assumptions about computed metrics, health and gameplay state. They
help expose programming errors during development and do not replace argument validation.
`build.gradle` enables assertions for both `run` and `test`, and the gameplay launchers pass `-ea`.
When IntelliJ uses a direct Application run configuration, add `-ea` to **VM options** through
`Run > Edit Configurations > KeyboardWarrior >
Modify options > Add VM options`; Gradle-delegated runs use the build configuration. See the
[Gradle JavaExec reference](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.JavaExec.html)
and [IntelliJ Application configuration guide](https://www.jetbrains.com/help/idea/run-debug-configuration-java-application.html).

Diagnostics use `java.util.logging`. State transitions and combat events are logged at `FINE`,
which is hidden at the default logging level. Terminal initialization and input failures are logged
at warning or severe level to help diagnose failures without adding routine debug messages to gameplay.

## Product Scope

### Target user profile

Players who want English typing practice with an adventure-game setting and feedback on speed and accuracy.

### Value proposition

Keyboard Warrior makes typing practice engaging through five combat scenarios. Speed and accuracy
show players how their typing affects attack damage.

## User Stories

| Version | As a ... | I want to ... | So that I can ... |
| --- | --- | --- | --- |
| v1.0 | player | defeat enemies through timed typing attacks | progress through five scenarios and win |
| v1.0 | player | see how many seconds remain while typing | submit my attack before its deadline |
| v1.0 | player | see my WPM and accuracy after a typing test | understand my typing performance |
| v1.0 | player | see feedback for my current text when time expires | understand my performance even without pressing Enter |
| v1.0 | player | face varied enemies and passages | keep successive runs interesting |

## Non-Functional Requirements

* Use Java 25 when building, running and testing the application.
* Given the same texts and time values, evaluation must return the same metrics, errors and time damage multiplier.
* Keep feedback independent of console input, timing, scenario selection and file storage.

## Glossary

* **Gross WPM**: Typing speed based on five submitted characters per word, including incorrect characters.
* **Word accuracy**: The percentage of matching word positions relative to the longer word list.
* **Positional matching**: Comparing the first expected word to the first submitted word, and so on.

## Testing

### Automated testing

Run `./gradlew check` on macOS/Linux or `.\gradlew.bat check` in Windows PowerShell, using Java 25.
This runs JUnit tests and Checkstyle. Tests are under `src/test/java/seedu/keyboardwarrior`.
Combat, gameplay and terminal tests cover turn resolution, scenario progression, timeout damage
and countdown output. Simulated terminals use JLine's `exec` provider for consistent stream-based
input and output on every platform, avoiding paused POSIX output pumps in feedback assertions.
Typing feedback tests include:

* `TypingEvaluatorTest.java` covers correct input, fractional WPM, wrong/missing/extra words, empty
  submissions, whitespace normalization, case and punctuation, positional matching, repeated words,
  invalid inputs, immutable errors and time damage multipliers before, at and after the time limit.
* `TypingResultFormatterTest.java` covers summary formatting, retained word errors, detailed reports
  and time-penalty reports.

### Manual gameplay testing

1. Run `sh ./run-game.sh` or `.\run-game.bat` with Java 25 in IntelliJ's Terminal or a native terminal.
   Confirm that an unsupported basic/redirected console shows the launcher instructions before gameplay.
2. Enter `start`; check that scenario 1 shows an enemy and the player has 100 HP.
3. Select `Ping`; check that a 15-second countdown appears above the typing instructions and passage
   and updates while typing.
4. Submit a partly correct answer before expiry; check feedback, damage, the enemy response
   and the next skill choice. Select another skill to confirm its configured time limit.
5. Type part of a passage and let it expire without pressing Enter; check nonzero WPM for nonempty text,
   accuracy for the captured input, a 20% damage penalty, an enemy turn and a clear next skill input.
   Damage is calculated from the captured metrics and rounded down after the penalty; empty input deals zero damage.
   Confirm that neither early submissions nor timeouts print a wrong-word list.
6. Defeat an enemy; check automatic advancement and carried player HP. Clear scenario 5 to
   check the win message and return to the menu.
7. Lose a run; check the defeat message and return to the menu. Enter `start` to check fresh HP
   and scenario 1. Check varied passages and enemies across several attacks and runs, allowing repeats.
   Enter `exit` to check shutdown, including during an active prompt.

### Manual component testing

To exercise the evaluator directly without console gameplay:

1. Compile with `./gradlew classes` or `.\gradlew.bat classes`.
2. Start Java 25's JShell from the project folder:

   ```text
   jshell --class-path build/classes/java/main
   ```

3. Enter:

   ```java
   import seedu.keyboardwarrior.typing.TypingEvaluator;
   import seedu.keyboardwarrior.typing.TypingResultFormatter;
   var result = TypingEvaluator.evaluateTyping("Strike the goblin", "Strike teh goblin", 6.0, 10.0);
   System.out.print(TypingResultFormatter.formatTypingResult(result));
   ```

   Expected: 34.00 WPM, 66.67% accuracy, `Damage penalty: 0%`, and word 2 expected `the` but typed `teh`.

4. Try `"Strike the"` as the submitted text: expect 20.00 WPM, 66.67% accuracy, and missing word 3 `goblin`.
5. Try `"Strike the goblin now"`: expect 42.00 WPM, 75.00% accuracy, and extra word 4 `now`.
6. Try `""`: expect 0.00 WPM, 0.00% accuracy, and all three expected words listed as missing.
7. Change the required time to `5.0`: expect `Damage penalty: 20% (time limit exceeded)`,
   `result.isTimeLimitExceeded()` to return `true` and `result.getTimeDamageMultiplier()` to return `0.8`.
8. Set both times to `6.0`: expect no penalty at the exact limit, `isTimeLimitExceeded()` to return
   `false` and `getTimeDamageMultiplier()` to return `1.0`.
9. Exit JShell with `/exit`.
