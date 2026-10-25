# Keyboard Warrior Developer Guide

## Acknowledgements

The project uses the [CS2113 tP starter template](https://github.com/NUS-CS2113-AY2627-S1/tp),
JLine for interactive terminal input and JUnit Jupiter for tests. Typing evaluation uses the Java
standard library. The enhancement sections below follow the
[Week 10 design documentation guidance](https://nus-cs2113-ay2627-s1.github.io/website/schedule/week10/project.html):
explain implementation, rationale and alternatives, with UML placed beside the relevant explanation.

## Design and implementation

### Runtime and terminal input

`KeyboardWarrior.java` initializes the terminal, checks its capabilities, then starts the main menu
and JLine input loop. `CountdownReader.supportsLiveDisplay()` rejects consoles that cannot redraw
input safely. The application displays terminal launcher instructions for unsupported consoles.
A background reader queues complete lines; the main thread processes input and updates the state
machine every 100 milliseconds. This keeps a live countdown above the typing instructions and passage
while the user edits the input line.

`MainMenuState` accepts `start` and enters `GamePlayState`. The gameplay state owns the run's player,
current encounter, clock and challenge factory. The same player and prompt selection history survive
scenario and boss phase transitions. `exit` stops the application from the menu, skill selection or
an active typing challenge.

### Game difficulty selection

`GameDifficulty` is separate from `TypingChallenge.Difficulty`: it describes run modifiers rather
than passage content. Easy, Normal, Hard and Coder use multipliers 4, 2, 1 and 0.8 for player damage,
starting health and countdowns. Hard is the default to preserve the original balance.

`MainMenuState` accepts numbers 1-4 or the names Start, Records, Exit and Choose Difficulty.
`DifficultySelectionState` accepts a mode number or name, then returns to a new menu with the same
`SaveStore` and selected mode. Invalid input leaves the selection menu active. Gameplay passes that
mode back to the menu after victory or defeat, preserving it for the session. The mode is not saved
to disk, and changing it requires returning to the menu.

`GamePlayState` creates the player with 400, 200, 100 or 80 HP and supplies the mode to the shared
`TypingChallengeFactory` and every `CombatEncounter`, including both boss phases. The factory applies
the countdown multiplier after calculating the normal base deadline and rounds up. Encryption uses
a base of 60 seconds, becoming 240, 120, 60 or 48 seconds. `SkillResolver` multiplies typing damage
before rounding down once. Automated enemy attacks keep their original base damage.

`run-game.bat` and `run-game.sh` first invoke Gradle's `prepareTerminalRun` task. That task depends on
`classes`, writes the runtime classpath and the build JVM's Java home, and does not create a JAR.
The launcher starts Java in the foreground with `-ea` and native access enabled. Direct terminal
input avoids Gradle `run` and IDE Run console stream redirection, which can prevent JLine from seeing
unsubmitted keystrokes. On Windows, the entry point selects the native Windows terminal type even
when the launcher supplies `TERM=dumb`.

### Typing history and state transitions

`SaveStore` saves each completed typing attempt as a UTF-8 line containing WPM and accuracy
percentage. It retains the latest ten entries in chronological order and reads existing records
before appending, so history survives application restarts. The default file is
`data/keyboardwarrior.txt`, relative to the launch directory. A missing file means empty history;
unreadable files and invalid loaded metrics produce `IOException`.

`MainMenuState` handles `records` by loading that store and passing its records to
`StateUi.showRecords(...)`. It passes the same store into `GamePlayState` on `start`, and gameplay
passes it back to the menu after victory or defeat. This preserves the selected history location
throughout the session. Tests inject stores under `@TempDir`, including the full balance simulation,
so running the test suite does not replace the player's history.

`GamePlayState` saves the evaluated result before resolving combat. An `IOException` is reported
without discarding the attack or ending the game. Loading errors are likewise reported while
leaving the main menu usable. The store records typing metrics, not a resumable game state.

### Enhancement 8.1: Character feedback and accuracy-based damage

#### Responsibilities and data flow

The typing package evaluates a completed input snapshot. It does not read the terminal, measure time,
apply damage or select an enemy. `GamePlayState` supplies expected text, current input, elapsed seconds
and required seconds to `TypingEvaluator.evaluateTyping(...)`. The returned `TypingResult` is immutable.
The state prints a summary and sends the same result to `CombatEncounter.submitTypingResult(...)`.
The encounter delegates damage to `SkillResolver`, preserving the boundary between measurement and combat.

| Class | Responsibility |
| --- | --- |
| `TypingEvaluator` | Validate and normalize the four inputs; coordinate metrics and retained word diagnostics |
| `AccuracyCalculator` | Compute character accuracy using Levenshtein edit distance |
| `WpmCalculator` | Compute gross WPM from normalized submitted character count and elapsed seconds |
| `WordErrorFinder` | Retain positional wrong, missing and extra word diagnostics for later features |
| `TypingResult` | Store full-precision metrics, supplied times and a defensive copy of word errors |
| `TypingResultFormatter` | Produce the short gameplay summary or optional detailed word report |
| `SkillResolver` | Convert WPM and character accuracy into player damage and apply health changes |
| `TypingChallengeFactory` | Select a skill's prompt and base deadline, then scale by the run difficulty |

The sequence below focuses on evaluation. The calls to `TypingEvaluator` and `AccuracyCalculator`
are static operations; formatting and combat happen after the result returns.

![Sequence diagram for evaluating one typing attempt](diagrams/typing-evaluation.svg)

[Editable PlantUML source](diagrams/typing-evaluation.puml).

#### Normalization and character accuracy

Both strings are normalized with `trim()` and `replaceAll("\\s+", " ")`. This removes leading and
trailing whitespace and collapses repeated internal whitespace. Capitalization and punctuation are
preserved. Let `expected` and `typed` denote those normalized strings.

```text
accuracyPercent = 100 * (1 - editDistance(expected, typed) / max(expectedLength, typedLength))
```

Edit distance is the minimum number of insertions, deletions and substitutions that transforms one
string into the other. `AccuracyCalculator` compares Unicode code points, so a supplementary character
counts as one character for accuracy. A deleted character is one edit rather than a shift that makes
every later character incorrect. Empty typed text is valid and produces zero accuracy; expected text
must remain nonempty after normalization, guaranteeing a positive denominator.

The implementation uses the usual dynamic programming recurrence. Each cell takes the minimum of
insertion, deletion and substitution cost. Only the previous and current row are retained, and the
shorter string occupies the columns. Runtime is `O(m * n)` and working memory is `O(min(m, n))` for
code point counts `m` and `n`. English challenge passages keep these inputs small. Evaluation compares
final text snapshots, so it cannot count mistakes that the player corrected before submission.

Word diagnostics remain separate from accuracy. `WordErrorFinder` still compares words at corresponding
positions and produces `WRONG`, `MISSING` and `EXTRA` entries. These entries are preserved for future
feedback; their count does not determine character accuracy. Gameplay uses `formatTypingSummary`,
which prints WPM, accuracy and the two times without the word list. `formatTypingResult` remains
available for detailed component inspection.

#### Speed, damage and time allowance

Gross WPM remains `(normalizedTyped.length() / 5.0) / (elapsedSeconds / 60.0)`. The existing WPM contract
uses Java `String.length()` (UTF-16 code units), whereas character accuracy uses code points. This
produces the same length for the intended ASCII English passages. Incorrect submitted characters and
normalized spaces count toward gross speed; Enter and discarded surrounding whitespace do not.

Player damage is now independent of the chosen skill's base damage:

```text
accuracyFraction = accuracyPercent / 100
playerDamage = floor(wpm * accuracyFraction^4 * gameDifficultyMultiplier)
```

`SkillResolver` calculates with full precision and rounds down once. On Hard, at 50 WPM and 95% accuracy this
is `floor(50 * 0.95^4) = 40`; at 50 WPM and 80% accuracy it is 20. The fourth power rewards accurate
input and limits the benefit of entering fast, unrelated text. A skill still chooses passage
difficulty. Its base damage continues to apply when the resolver receives no typing result for an
automated enemy attack. This preserves existing enemy skill rotations.

The countdown limits how long an attempt can remain open. For skills other than Encryption,
`TypingChallengeFactory` computes:

```text
minimumSeconds = 15 / 30 / 30 / 45 for Easy / Medium / Hard / Ultimate
baselineSeconds = normalizedPrompt.length() * 60 / (5 * 50)
requiredSeconds = max(minimumSeconds, ceil(baselineSeconds * 1.2))
finalSeconds = ceil(requiredSeconds * gameDifficultyMultiplier)
```

This gives a 50 WPM player a 20% time buffer while retaining the previous minimum allowances.
The buffer increases available typing time; it is not a damage multiplier. Required time therefore
varies by passage. `ChallengeClock` starts after prompt display. Skill selection and previous turn
feedback are outside the measured duration.

`CombatEncounter` calls `TypingChallengeFactory.generateForSkill(...)` so skills sharing a difficulty
can use different passages. Encryption remains Ultimate, with a base deadline of exactly 60 seconds
before applying the game difficulty multiplier.
`PromptBank.getEncryptionPrompt()` uses a randomly selected Ultimate passage's character count as
its target length, then generates random special characters in space-separated groups of one to six.
The resulting passage is between the target length and six characters longer. Other skills continue
to use the difficulty-based generator above.

Both Enter and automatic expiry use the same evaluator. At expiry, `GamePlayState.update()` obtains
`StateUi.takeCurrentInput()` before clearing the editable line and resolving the attempt. The actual
elapsed duration is supplied, so captured partial text retains its measured WPM and accuracy.
`isTimeLimitExceeded()` reports the strict comparison `elapsedSeconds > requiredSeconds`. It is
metadata: overdue attempts receive no additional damage reduction. The retained compatibility getter
`getTimeDamageMultiplier()` returns `1.0`; player damage does not depend on it.

#### Validation and defensive checks

Null text or dependency references cause `NullPointerException`. Blank normalized expected text,
nonpositive or nonfinite times and out-of-range result metrics cause `IllegalArgumentException`.
Empty player text is a valid zero-score attempt. Result fields are immutable; word error lists are
copied and exposed as unmodifiable lists. `WordError` positions begin at 1 and use an empty string
for an absent word.

Assertions check computed accuracy bounds, finite nonnegative WPM, edit distance bounds and health
updates. Public argument checks remain active without `-ea`. The state clears its active challenge
before completing a turn, and the encounter requires exactly one pending skill before accepting a
result. A duplicate result or invalid turn sequence causes `IllegalStateException`, preventing
accidental double attacks. Existing `FINE` logging records combat events without cluttering gameplay.

#### Design choices and alternatives

| Choice | Rationale | Alternative considered |
| --- | --- | --- |
| Character edit distance | Local insertions and deletions produce local accuracy costs | Positional word matching can mark the rest of a passage wrong after one omitted word |
| Two-row dynamic programming | Retains the exact score with bounded working memory | A full matrix stores every cell and is only useful if a later feature needs an edit trace |
| Fourth-power accuracy factor | Makes careful typing matter while keeping one formula across skills | Linear accuracy penalizes fast inaccurate text less; skill base multipliers make equal typing performance deal different damage |
| Length-based deadline with a minimum | Makes longer passages achievable at the balance target | One fixed deadline per skill can demand very different WPM from randomly selected passages |
| Deadline as metadata | Capture still ends the turn, without a second score reduction | An extra late multiplier obscures the direct relationship between text, speed and damage |

### Enhancement 8.2: Scenario progression and two-phase final boss

#### Run structure and ownership

A run starts with one `Player` at the selected mode's starting HP (100 on Hard), passes through five regular
scenarios and ends with two
phases of the Kernel Overlord. Defeating scenario 5 starts the boss rather than winning the run.
Only victory against boss phase 2 triggers the run victory message and return to the main menu.
Player defeat at any point ends the run immediately. A new `start` creates a new player and resets
scenario, phase and challenge state.

`GamePlayState` coordinates progression; `CombatEncounter` continues to resolve only one enemy at a
time. This keeps the existing combat boundary intact. `scenarioIndex` tracks regular scenario
progress, while `bossPhase` is `0` during regular scenarios and `1` or `2` during the boss. The private
helpers `startScenario()`, `startBossPhase()` and `advanceAfterVictory()` separate encounter creation
from transition decisions. `StateUi.showBossPhase(phase, total)` announces the phase without putting
combat rules into the UI.

The focused class diagram shows the run's references and factory dependencies. Multiplicities apply
to active gameplay; old encounters are replaced during progression. The diagram omits unrelated UI,
skill and clock classes.

![Class diagram for scenario and boss progression](diagrams/run-progression.svg)

[Editable PlantUML source](diagrams/run-progression.puml).

`EnemyFactory.createRandomEnemy(random, scenarioNumber)` accepts a one-based number from 1 to 5.
It chooses an existing enemy type and skill rotation, then sets health from the scenario table.
Random names therefore retain variety without changing how many successful attacks the balance
target needs. The overloads that create named or unrestricted random enemies remain usable by
sample code and component tests.

`EnemyFactory.createBossPhase(phase)` accepts only phase 1 or 2 and returns a fresh enemy named for
that phase. Phase 1 has 50 HP and a fixed 10-damage attack; phase 2 has 70 HP and a fixed 20-damage
attack. Each phase uses a new `CombatEncounter` with the existing player and shared prompt factory.
The first phase's dead enemy object is not revived or repurposed.

#### Transition ordering and phase boundaries

`CombatEncounter.submitTypingResult(...)` resolves the player's attack first. If enemy HP reaches
zero, it completes victory and returns before executing an enemy turn. Otherwise the enemy attacks;
player defeat is checked before the next turn is offered. `GamePlayState` inspects the completed
encounter and advances only on victory. It does not infer success merely because a challenge ended.

The following sequence covers the boundary after a lethal attack against boss phase 1. The next
encounter is created only after the first one has completed. Activation boxes show execution;
dashed arrows show returns. The omitted health check is explained in the note.

![Sequence diagram for moving from boss phase 1 to phase 2](diagrams/boss-phase-transition.svg)

[Editable PlantUML source](diagrams/boss-phase-transition.puml).

The same player reference carries remaining HP into the next encounter. Neither a regular scenario
transition nor a boss phase transition heals the player or triggers an extra enemy attack. Damage
is clamped at zero HP on the current target; any excess is discarded. For example, dealing 60 damage
to a phase with 10 HP remaining defeats that phase, then phase 2 begins with its full 70 HP.
A very strong attack cannot skip phase 2 or immediately win.

A timeout uses this same sequence. Nonempty captured input can deal damage, including a lethal
attack. An empty or inaccurate input can deal zero damage and still consume a turn. If the enemy
survives and its response brings player HP to zero, the state returns to the menu without starting
a later scenario or phase.

#### Balance target and verification

The Hard-mode balance target is 50 WPM and at least 95% character accuracy, yielding at least 40 damage per
attempt. The table counts attacks and possible retaliation at exactly 40 damage. Regular enemies
retain their usual skill rotation; the highest first attack among those rotations is 20 damage.
Enemies requiring one attack do not retaliate, and those requiring two only use their first attack.

| Encounter | HP | Player attacks | Maximum retaliation damage |
| --- | --- | --- | --- |
| Scenario 1 | 30 | 1 | 0 |
| Scenario 2 | 35 | 1 | 0 |
| Scenario 3 | 50 | 2 | 20 |
| Scenario 4 | 60 | 2 | 20 |
| Scenario 5 | 70 | 2 | 20 |
| Boss phase 1 | 50 | 2 | 10 |
| Boss phase 2 | 70 | 2 | 20 |
| **Total** | | **12** | **90** |

Thus a target-performance player finishes even the strongest possible regular enemy selection with
at least 10 of the original 100 HP. Random enemy selection cannot invalidate this bound. It is a
balance guarantee for the stated performance, not for arbitrary typing: an additional low-damage
turn can expose a later, stronger enemy skill and lead to defeat. The HP progression introduces
extra turns gradually, and the boss tests the same typing mechanics with a clear phase transition.

#### Validation, invariants and alternatives

The factory rejects invalid scenario numbers and boss phases with `IllegalArgumentException` and
null random sources with `NullPointerException`. Generated enemies must have positive HP and usable
skill loadouts. Combat entities reject invalid health, and the resolver clamps damage at zero HP.
The encounter guards start/prepare/submit sequencing with exceptions and checks living-participant
assumptions through assertions. State and combat diagnostics remain hidden at normal logging levels.

Progression must preserve these invariants: one live player per run; at most one active challenge;
only a completed victorious encounter advances; regular indices stay in the five-scenario range;
boss phase remains 0, 1 or 2; and no run victory occurs before phase 2 is defeated. Resetting all
progression state on entry prevents a new run from inheriting the previous boss phase.

| Choice | Rationale | Alternative considered |
| --- | --- | --- |
| Keep progression in `GamePlayState` | The state already owns the run and its transitions | A separate campaign manager adds another coordinator for a short fixed sequence |
| Reuse `CombatEncounter` per phase | Existing turn ordering and death checks apply unchanged | A special boss encounter subclass duplicates combat logic and increases testing scope |
| Create a fresh enemy for each phase | Fresh HP and loadout have explicit phase boundaries | Mutating a dead enemy risks retaining death flags, pending skills or rotation indices |
| Discard overkill and prohibit transition attacks | Every phase has a predictable initial state | Carrying overflow or adding a transition attack makes timing and difficulty less transparent |
| Set HP by scenario and preserve random identities | Progression is reproducible while encounters remain varied | Unrestricted random HP makes some runs substantially harder at equal typing performance |

### Defensive programming and diagnostics

Assertions expose internal programming errors during development; public validation uses exceptions
and remains active when assertions are disabled. Gradle enables assertions for `run` and `test`,
and the gameplay launchers pass `-ea`. Direct IntelliJ Application configurations should add `-ea`
in **VM options**. See the [README setup steps](../README.md#setting-up-in-intellij).

Diagnostics use `java.util.logging`. Combat events and state transitions use `FINE`, hidden at the
default level. Terminal initialization and input failures use warning or severe levels. This gives
maintainers failure context without filling the typing area with routine debug messages.

## Product scope and requirements

The target users are players who want English typing practice through short combat encounters.
Keyboard Warrior makes speed and accuracy visible through damage and provides a complete run with
regular progression and a final boss.

| Enhancement | As a player, I want to ... | So that ... |
| --- | --- | --- |
| 8.1 | receive character accuracy and WPM feedback | one local typo has a proportional accuracy cost |
| 8.1 | deal damage from speed and accuracy across all skills | I can understand how my typing affects combat |
| 8.1 | retain my current input when time expires | unfinished typing still contributes to my attempt |
| 8.2 | face progressively tougher regular scenarios | the run builds up difficulty predictably |
| 8.2 | fight a final boss with two phases | the run has a clear final challenge before victory |

Non-functional requirements:

* Build, run and test with Java 25.
* Return deterministic metrics and damage for the same texts and supplied times.
* Keep evaluation independent of terminal input, timing and scenario progression.
* Retain immutable results and reject invalid integration data before changing combat state.
* Keep gameplay usable in a supported terminal while countdown updates preserve input.

Glossary:

* **Gross WPM**: Five normalized submitted UTF-16 code units count as one word, including incorrect input.
* **Character accuracy**: One minus normalized character edit distance, expressed as a percentage.
* **Edit distance**: Minimum count of character insertions, deletions and substitutions.
* **Overkill**: Damage beyond the current target's remaining HP; discarded at encounter boundaries.
* **Phase**: One boss enemy configuration with its own HP and attack, resolved as one combat encounter.

## Testing

### Automated checks

Run `./gradlew check` on macOS/Linux or `.\gradlew.bat check` in Windows PowerShell with Java 25.
This runs JUnit and Checkstyle. Tests live under `src/test/java/seedu/keyboardwarrior`.

The relevant regression checks cover character substitution/insertion/deletion, normalization,
capitalization and punctuation, empty input, invalid values and immutable diagnostic lists. Formatter
tests check four summary metrics and retained detailed word reports without a timing penalty line.
Damage tests check the fourth-power formula, rounding, skill independence, empty input and unchanged
enemy base attacks. Factory and gameplay tests cover scenario HP, both boss loadouts, HP carryover,
phase boundaries, defeat and restart. Clock and terminal tests cover countdown output and captured
unfinished input. Simulated terminal tests select JLine's `exec` provider for consistent stream-based
input/output on Windows, macOS and Linux.

Storage tests verify both file contents and loaded records, retention after reopening, and invalid
metric rejection. State integration tests check that starting and finishing a run retain the same
history store. Enemy lookup is also tested under a Turkish default locale so case normalization
cannot depend on the operating system's language.

The progression regressions in `GamePlayStateTest` include:

* `normalPerformance_eachRepeatedEnemyType_survivesFiveScenariosAndBothBossPhases` checks the balance target across each regular enemy type.
* `bossPhaseTransition_preservesPlayerHealthAndDiscardsOverkillWithoutRetaliation` checks the phase boundary.
* `bossPhaseOne_playerDefeat_returnsToMenuWithoutStartingPhaseTwo` and `bossPhaseTwo_playerDefeat_returnsToMenuWithoutVictory` check defeat in either phase.
* `update_liveCountdown_scoresUnsubmittedAnswerAndPreservesTimerOrder` checks unfinished input and countdown placement.

### Manual gameplay acceptance tests

Use a supported terminal with Java 25 and `sh ./run-game.sh` or `.\run-game.bat`.

| Test | Steps | Expected result |
| --- | --- | --- |
| Launch and setup | Start in an unsupported redirected console, then use the suggested terminal launcher | Unsupported console explains the launcher; supported terminal shows menu |
| Countdown and skill allowance | Start a run; choose each skill across several passages | Timer stays above the passage, updates while typing and uses the displayed length-based allowance |
| Early submission | Submit a partly correct passage before expiry | Four metrics and damage appear; no wrong-word list; surviving enemy retaliates once |
| Timeout capture | Type part of a passage, correct a character, then let time expire without Enter | Final current input is scored; nonempty text has nonzero WPM; no separate late penalty; next input is clear |
| Empty timeout | Enter no text before expiry | Zero WPM, accuracy and damage; a surviving enemy takes its turn |
| Regular progression | Defeat enemies through scenarios 1 to 5 | Enemy HP follows 30, 35, 50, 60, 70; same player HP carries; scenario 5 starts boss phase 1 |
| Boss boundary and overkill | Defeat phase 1 with damage exceeding its remaining HP | Phase 2 starts at 70 HP; player HP is unchanged by transition; no extra boss attack; no run victory yet |
| Final victory | Defeat boss phase 2 | Run victory appears and returns to menu |
| Phase-boundary defeat | Reach phase 2 with low HP, then make a nonlethal or empty attempt | Boss response can defeat player; no later phase or victory is announced |
| Restart and exit | Start after victory/defeat; try `exit` during a prompt | New run begins at scenario 1 with the selected mode's starting HP; exit ends the game cleanly |
| Game difficulty | Choose each mode by number or name, then Start | HP is 400/200/100/80; Encryption countdown is 240/120/60/48 seconds; selection persists after a run |
| History and restart | Complete an attempt, return to the menu, enter `records`, then restart from the same folder | Saved WPM and accuracy remain visible; at most the latest ten attempts appear, oldest first |

For a balance check, sustain at least 50 WPM and 95% character accuracy on each attempt. Confirm at
least 40 damage per attack, at most two attacks per regular enemy or boss phase, and at least 10 HP
remaining at victory. Use deterministic automated tests for the strongest regular enemy rotations;
random manual runs demonstrate the flow but cannot establish the worst-case bound by themselves.

### Manual evaluator checks

Compile with the Gradle `classes` task, then start Java 25's JShell from the repository folder:

```text
jshell --class-path build/classes/java/main
```

```java
import seedu.keyboardwarrior.typing.TypingEvaluator;
import seedu.keyboardwarrior.typing.TypingResultFormatter;
var result = TypingEvaluator.evaluateTyping("Strike the goblin", "Strike teh goblin", 6.0, 10.0);
System.out.print(TypingResultFormatter.formatTypingResult(result));
```

Expected: 34.00 WPM, 88.24% accuracy, elapsed 6.00 and required 10.00 seconds. The detailed formatter
still identifies word 2 as expected `the`, typed `teh`. Its positional word diagnostic does not set
the character score. The player damage formula yields 20 for this result.

| Typed text (same expected text, six seconds) | WPM | Character accuracy | Player damage |
| --- | --- | --- | --- |
| `Strike the goblin` | 34.00 | 100.00% | 34 |
| `Strike teh goblin` | 34.00 | 88.24% | 20 |
| `Strike the` | 20.00 | 58.82% | 2 |
| `Strike the goblin now` | 42.00 | 80.95% | 18 |
| Empty string | 0.00 | 0.00% | 0 |

Changing only required seconds to `5.0` makes `isTimeLimitExceeded()` true and leaves the metrics and
damage unchanged. Setting required seconds to `6.0` is on time because the comparison is strict.
Zero, negative, NaN and infinite time values should throw `IllegalArgumentException`; null text should
throw `NullPointerException`. Exit JShell with `/exit`.

## Maintaining diagrams and iteration traceability

The diagrams are ordinary SVG images so GitHub and GitHub Pages display them without Mermaid support
or a server-side renderer. Each image has a neighboring `.puml` source for editing with PlantUML.
The checked-in SVGs were laid out from those sources; keep each source and rendered image aligned
when changing the design. [Diagram maintenance notes](diagrams/README.md) describe their scope.

The Week 10 course workflow also asks the team to record the iteration plan in its issue tracker,
assign work and verify RepoSense attribution. Those external checks require the team's actual issue
links, assignments and dashboard evidence. This document records enhancement design, alternatives,
defensive contracts and tests; it does not claim tracker updates or invent ownership annotations.
