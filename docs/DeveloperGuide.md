# Keyboard Warrior Developer Guide

This guide explains the design decisions and implementation boundaries needed to maintain Keyboard
Warrior. For commands and player-facing rules, see the [User Guide](UserGuide.md).

## Acknowledgements

The project uses the [CS2113 tP starter template](https://github.com/NUS-CS2113-AY2627-S1/tp),
[JLine](https://github.com/jline/jline3) for terminal input and
[JUnit Jupiter](https://junit.org/junit5/) for tests. Typing evaluation uses the Java standard library.
The documentation website uses [Marked](https://marked.js.org/), under its MIT license. This guide
follows the structure of the [AddressBook-Level3 Developer Guide](https://se-education.org/addressbook-level3/DeveloperGuide.html).

## Design

### Architecture overview

`KeyboardWarrior` selects the input mode and drives a `StateMachine`. Its active state coordinates
UI output, typing evaluation, encounters and persistence. These are responsibility boundaries,
not strict layers: both the state machine and concrete states call the UI directly.

![Architecture overview and component dependencies](diagrams/architecture-overview.svg)

[PlantUML source](diagrams/architecture-overview.puml).

Start reading in [`src/main/java/seedu/keyboardwarrior`](https://github.com/AY2627S1-CS2113-W12-4/tp/tree/master/src/main/java/seedu/keyboardwarrior):

| Area | Developer entry points |
| --- | --- |
| Application and UI | `KeyboardWarrior`, `CountdownReader`, `ui.StateUi`: terminal setup, input and output |
| State coordination | `state.StateMachine`, `state.GamePlayState`: command dispatch and run progression |
| Typing | `typing.TypingEvaluator`, `TypingChallengeFactory`: metrics, prompts and deadlines |
| Combat and events | `combat.SkillResolver`, `encounter.EncounterFactory`: effects and encounter creation |
| Persistence and randomness | `storage.GameSaveStore`, `GameSave`, `seed.RunRandom`: checkpoints and reproducible choices |

### Runtime and terminal input

In live mode, JLine and `CountdownReader` preserve editable input while displaying the countdown.
A background reader queues complete lines; only the main thread dispatches commands and changes
state. It processes input, calls `update()` and sleeps for 100 ms. At expiry, gameplay captures the
current editable buffer before resolving the attempt.

![Live terminal input queue and main-thread state updates](diagrams/runtime-input.svg)

[PlantUML source](diagrams/runtime-input.puml).

Without a capable terminal, a buffered reader queues complete lines, EOF or a reader failure.
The main loop calls only `updateWaiting()`, allowing delayed reaction commands to appear without
Enter. Full `update()` would incorrectly submit empty input because this mode cannot read unfinished
text. Enter submits using actual elapsed time: late combat is scored normally, but late events fail.
Both modes stop on `exit` or EOF.

![Plain input queue with delayed-event updates and no automatic typing submission](diagrams/plain-input.svg)

[PlantUML source](diagrams/plain-input.puml).

### State management

`StateMachine` delegates input and updates to one current `State`; `transitState(...)` exits the old
state before entering its replacement. The menu's seed prompt is a flag within `MainMenuState`.
Difficulty selection returns to the menu; Start and Continue enter gameplay; victory and defeat
return to the menu. Records and Leaderboard display within the existing menu without a transition.

![State hierarchy and active-state reference](diagrams/state-structure.svg)

[PlantUML source](diagrams/state-structure.puml).

### Single Level of Abstraction Principle (SLAP)

Coordinators delegate details to private helpers in the owning class. For example,
`GamePlayState.finishAttempt(...)` evaluates, saves history, resolves the encounter and advances;
`GameSaveStore.save(...)` separates encoding from file replacement. Keep this ordering: feedback and
history saving precede encounter effects, and a history write failure must not prevent resolution.
Private helpers keep these flows readable without introducing extra public interfaces.

## Implementation

### Source launchers and release startup

Use Java 25. `run-game.bat` and `run-game.sh` invoke Gradle's `prepareTerminalRun`, then launch Java
directly with assertions enabled (`-ea`). Direct terminal access lets JLine capture unfinished input,
which Gradle/IDE Run consoles may not support. `shadowJar` builds `build/libs/KeyboardWarrior.jar`;
`java -jar KeyboardWarrior.jar` needs neither Gradle nor the source checkout. The manifest enables
native access for JLine's bundled terminal support.

### Game difficulty selection

`GameDifficulty` controls run modifiers; `TypingChallenge.Difficulty` controls passage content.
Easy, Normal, Hard and Coder use multipliers 4, 2, 1 and 0.8 for player damage, starting health and
countdowns. Hard is the default. `TypingChallengeFactory` scales deadlines last and rounds up;
`SkillResolver` scales damage before rounding down. Enemy HP and attacks are not mode-scaled.

`DifficultySelectionState` returns the selected mode and existing history store to the menu.
Gameplay retains the mode after victory/defeat. Restarting defaults new runs to Hard, while Continue
restores the saved run's mode. The menu preference is not saved independently.

### Seeds and reproducible choices

`SeedGenerator.parseSeed(...)` trims input, parses a signed `long`, or uses the text's `hashCode()`
when parsing fails. Blank input generates a new seed. Text is case-sensitive; out-of-range numbers
are treated as text. At the seed prompt, `3` is a seed, while `exit` still quits.

One `RunRandom` supplies encounter offers, content, delays, rewards and passages. It follows Java
`Random`'s 48-bit sequence. `PromptBank` also remembers the previous passage at each difficulty to
avoid immediate repeats. The object diagram illustrates the run-wide references shared by gameplay
and the current encounter; replacing an encounter preserves these objects.

![Example active-run objects and shared references](diagrams/active-run-objects.svg)

[PlantUML source](diagrams/active-run-objects.puml).

Reproduction requires the same product version, seed, choices, outcomes and skill/turn sequence.
All offers consume random draws, including unselected ones; rewards draw only when earned, and an
empty reward pool draws nothing. Saves retain both generator position and prompt history, so restoring
must rebuild saved content without generating it again.

### Records and all-time leaderboard

`SaveStore` retains ten attempts, oldest first. `LeaderboardStore` independently retains one best
attempt, so history rotation cannot erase it. An immutable `SavedRecord` holds only WPM and character
accuracy: all runs using the same files share these views, without difficulty or player separation.

![Persistence classes, inheritance and the high-score store reference](diagrams/storage-structure.svg)

[PlantUML source](diagrams/storage-structure.puml).

Files are relative to the launch directory. A custom history path determines its sibling high-score
file and the directory containing the run save.

| Default file | Format and purpose |
| --- | --- |
| `data/keyboardwarrior.txt` | UTF-8 `wpm,accuracyPercent` lines; latest ten attempts |
| `data/keyboardwarrior.txt.high-score` | One line in the same format; best attempt |
| `data/game-save.properties` | UTF-8 Java properties; one resumable run |

The first sequence shows history saving before encounter resolution. Storage errors are reported,
but the submitted response still takes effect.

![Saving typing metrics before resolving combat or an event response](diagrams/history-saving.svg)

[PlantUML source](diagrams/history-saving.puml).

The second sequence expands the save: initialize an absent high-score file from existing history,
consider the new candidate, then rotate history. This preserves an old best before it is discarded.

![Sequence diagram showing high-score initialization and update before typing-history rotation](diagrams/typing-history-save.svg)

[PlantUML source](diagrams/typing-history-save.puml).

`SCORE_ORDER` compares full-precision WPM, then accuracy; exact ties do not replace the score. Display
rounding does not affect ranking. Missing history is empty; an absent high score is initialized from
available history. Malformed records produce `IOException`, not silent replacement. Loaded WPM must
be finite and nonnegative; accuracy must be finite and within 0-100. Each record needs two fields,
and the high-score file needs exactly one line. History loading validates the latest ten lines.

All stores inherit `FileStore.writeSafely(...)`: write a temporary file, replace the destination
atomically where supported (regular replacement otherwise), and clean up. This protects each file,
but is not a transaction across files. If the high-score write fails, history is not appended; if
history fails afterward, the new best may remain saved alone. Menu loading errors leave the menu usable.

### Active run saves

`GameSave` version 2 captures difficulty, player HP, signed bonus time, ordered skill IDs, progression,
optional random state/prompt history, and either three pending choices or one unfinished encounter.
`EncounterSnapshot` retains generated content: opponent HP/attack index, puzzle question/answer,
reaction command/remaining delay, or dialogue branch. See `GameSaveStore.toProperties(...)` for keys
and `GameSave` validation for accepted combinations rather than duplicating the schema here.

Only stable stages can be saved: offers, combat without a pending attack, and events before their
effects are consumed. Autosaves follow offer creation, encounter selection, reaction reveal,
dialogue follow-up, surviving turns and progression. Start replaces the slot once new offers are
saved; victory/defeat deletes it; Exit retains it. There is no manual save command.

Restoration rebuilds the player and shared factories without replaying rewards or consuming random
draws. The reward pool excludes restored skills. Clocks and unfinished input are excluded: interrupted
combat returns to skill selection, where the same skill regenerates the same passage; active event
responses receive a fresh deadline. A waiting reaction resumes its saved remaining delay, updated
when explicitly exiting during the wait. HP above starting health is valid after healing.

`GameSaveStore.load()` validates before Continue leaves the menu. Missing files mean no saved run;
invalid fields or unsupported versions produce `IOException`. Version-1 combat saves migrate with
the starting loadout and zero bonus time; without saved random data, future choices cannot be recovered.
Save failures are reported while gameplay remains usable.

### Typing evaluation and damage

`TypingEvaluator` takes expected text, submitted text, elapsed seconds and required seconds, returning
an immutable `TypingResult`. It performs no terminal, clock, combat or file operations, making metrics
independently testable. `GamePlayState` displays/saves the result, then sends it to combat or submits
the event answer. The sequence shows metric calculation after input validation and normalization.

![Sequence diagram for evaluating one typing attempt](diagrams/typing-evaluation.svg)

[PlantUML source](diagrams/typing-evaluation.puml).

Both texts use `trim()` and collapse repeated whitespace; case and punctuation remain significant.
The essential calculations are:

```text
accuracy = 1 - editDistance(expected, typed) / max(expectedLength, typedLength)
wpm = (normalizedTyped.length() / 5.0) / (elapsedSeconds / 60.0)
playerDamage = floor(wpm * accuracy^4 * gameDifficultyMultiplier)
```

`AccuracyCalculator` uses Unicode code-point Levenshtein distance, so a deletion has a local cost
instead of shifting every later comparison. Two dynamic-programming rows give `O(m*n)` time and
`O(min(m,n))` table space. WPM uses UTF-16 code units (`String.length()`); these lengths agree for
ASCII passages. `WordErrorFinder` retains positional word diagnostics for the detailed formatter,
but those diagnostics do not set accuracy and are omitted from gameplay summaries.

The fourth-power factor rewards accuracy: 50 WPM at 95% accuracy deals 40 damage on Hard, versus 20
at 80%. All player skills use this formula; skill base damage applies only to automated enemy attacks
(the resolver's null-result path). Required time does not apply another combat damage penalty.
For events, `elapsed > required` instead makes the response incorrect, even if its text matches.

`TypingChallengeFactory` gives ordinary skills enough time for 50 WPM plus 20%, with passage-difficulty
minimums of 15/30/30/45 seconds. Encryption generates symbols and uses a fixed 60-second base. Both
add the player's signed bonus, clamp to at least ten seconds, then apply run difficulty. Overclock
adds `floor(20 * unroundedDamage / 50)` bonus seconds to later challenges; cumulative adjustments
saturate at integer limits. Event deadlines follow their own rules below.

Null inputs, blank expected text, invalid times or invalid metrics are rejected with exceptions.
Empty submitted text is a valid zero score. Result collections are defensively copied. Assertions
check internal invariants; public validation remains active without assertions.

#### ChallengeClock implementation

`ChallengeClock` uses differences from `System.nanoTime()` so wall-clock changes cannot alter elapsed
time. It neither creates a thread nor submits answers. Remaining seconds round up and clamp at zero;
fractional elapsed seconds continue increasing after expiry. Use elapsed seconds for WPM, not the
rounded countdown. Restarting replaces the timer; zero duration expires immediately, and negative
duration throws `IllegalArgumentException`. Gameplay starts timing after displaying the prompt.

### Combat model and encounter lifecycle

`Entity` owns HP and ordered active skills; `Player` adds accumulated bonus time. `SkillRegistry`
provides shared skill definitions, and `EnemyFactory` supplies opponent identities and attack
rotations. `RewardPool` removes skills from its own available list; the event grants the selected
skill to the player. The diagram shows these shared references and inheritance.

![Combat entity inheritance and shared skill management](diagrams/combat-skills.svg)

[PlantUML source](diagrams/combat-skills.puml).

The state machine uses `CombatEncounter`'s split API so typing never blocks countdown updates:

1. `begin()` opens combat; `begin(savedIndex)` restores the next enemy attack.
2. `preparePlayerTurn(skill)` validates ownership and records one pending skill/challenge.
3. `submitTypingResult(result)` applies the player attack. A defeated enemy cannot retaliate;
   otherwise it uses its next base-damage skill and advances its rotation.
4. Player defeat completes the encounter; if both survive, the next player turn opens.

Lifecycle guards reject duplicate results and invalid turn ordering with `IllegalStateException`.
Outcomes are readable only after completion. Stable combat snapshots retain enemy HP and attack
rotation, excluding pending or completed turns. Skill definitions and enemy rotations belong in
`SkillRegistry` and `EnemyFactory`, rather than a second catalogue in this guide.

### Encounter choices and events

`Encounter` defines completion and outcome access. `CombatEncounter` handles turns; `EventEncounter`
shares lifecycle behavior among puzzles, reactions and dialogue. `EncounterFactory` selects content
from banks before construction, so encounters retain selected values rather than content banks.

![Encounter hierarchy with combat and the three event subclasses](diagrams/encounter-hierarchy.svg)

[PlantUML source](diagrams/encounter-hierarchy.puml).

Each regular scenario offers combat, one event, and an equal-probability combat/event choice. Event
categories are uniform. Invalid selections leave offers and random state unchanged. Gameplay displays
and times challenges; puzzle answers are hidden by `StateUi.showEventChallenge(...)`.

![Event phases and the stages that can be saved](diagrams/event-lifecycle.svg)

[PlantUML source](diagrams/event-lifecycle.puml).

The lifecycle consumes responses before terminal effects, preventing repeated rewards. Dialogue
prepares a follow-up before committing its branch so challenge-creation failure remains retryable.
Snapshots exclude resolving/completed stages. Resumption redisplays saved stages without replaying effects.

| Event | Deadline before mode scaling | Resolution |
| --- | --- | --- |
| Puzzle | `max(1, 30 + bonusTime)` seconds | Correct: skill or +20 HP if rewards exhausted; wrong/late: -2 bonus seconds |
| Reaction | Wait 5,000-29,999 ms, then allow 2 seconds; no bonus-time adjustment | Correct/on time: +10 HP; wrong/late: 30 damage |
| Dialogue | Opening/trade: 10 seconds; escape: 5; no bonus-time adjustment | Correct opening leads to trade, failure to escape. Trade: skill/+20 HP or -5 bonus seconds. Escape: no effect or 20 damage |

Matching ignores case and surrounding whitespace. An event's successful outcome means **survival**,
so a nonlethal penalty still advances the scenario. One run-wide reward pool awards Overclock once;
subsequent puzzle/trade rewards heal. Health may exceed starting HP; earned skills and bonus time
carry across encounters and are saved.

### Scenario progression and two-phase final boss

`GamePlayState` owns five regular scenarios followed by two boss phases. `scenarioIndex` is 0-4
for regular scenarios and 5 for the boss; `bossPhase` is 0, then 1 or 2. `startScenario()`,
`startBossPhase()` and `advanceAfterVictory()` separate creation from progression decisions.

![Class diagram for scenario and boss progression](diagrams/run-progression.svg)

[PlantUML source](diagrams/run-progression.puml).

Each phase uses a fresh enemy and `CombatEncounter` with the existing player and challenge factory.
Reusing normal combat avoids a separate boss implementation; replacing the enemy avoids stale death
flags or pending attacks. Progression stays in the gameplay state because the campaign is a short,
fixed sequence.

![Sequence diagram for moving from boss phase 1 to phase 2](diagrams/boss-phase-transition.svg)

[PlantUML source](diagrams/boss-phase-transition.puml).

Advance only after a completed survival/victory outcome. Transitions neither heal nor attack, and
overkill is discarded. Phase 2 always starts at full HP; only defeating it wins the run. Defeat
returns immediately to the menu without starting another encounter.

Regular combat HP is 30/35/50/60/70; boss phases have 50/70 HP and deal 10/20 damage per retaliation.
At 50 WPM and 95% accuracy on Hard, each attack deals at least 40 damage. The first two enemies
cannot retaliate; the next three deal at most 20 each, and the bosses at most 10 and 20. This leaves
at least 10 of the initial 100 HP on an all-combat route. Extra low-damage turns or failed events
fall outside that balance guarantee.

## Testing

### Automated checks

With Java 25, run `.\gradlew.bat check` (Windows) or `./gradlew check` (macOS/Linux). This builds the
release JAR, runs JUnit including packaged-JAR smoke tests, and runs Checkstyle. Tests live under
[`src/test/java/seedu/keyboardwarrior`](https://github.com/AY2627S1-CS2113-W12-4/tp/tree/master/src/test/java/seedu/keyboardwarrior); storage uses temporary
folders. The main regression entry points are:

| Area | Tests to inspect |
| --- | --- |
| Startup and terminal input | `KeyboardWarriorSmokeTest`, `KeyboardWarriorPlainLoopTest`, `StateUiTerminalTest` |
| Metrics, deadlines and damage | `typing` package tests, `SkillResolverTest`, time-bonus tests |
| Events and one-time effects | `GamePlayStateEventsTest`, event tests, `EncounterSnapshotTest` |
| Progression and balance | `GamePlayStateTest`, `CombatEncounterTest`, `EnemyFactoryTest` |
| Persistence, migration and replay | Storage tests, `MainMenuStateTest`, `RunRandomTest`, save/resume gameplay tests |

Assertions are enabled by Gradle tests and source launchers; add `-ea` to direct development launches.
`java.util.logging` records state/combat diagnostics at `FINE`, hidden by default. Automated terminal
simulations complement, but do not replace, testing the release JAR on each supported OS.

### ChallengeClock unit tests

`typing.ChallengeClockTest` covers an unstarted clock, zero/negative durations, expiry and restart.
Run it alone with `.\gradlew.bat test --tests "seedu.keyboardwarrior.typing.ChallengeClockTest"`
(or `./gradlew` on macOS/Linux). Its real-time expiry test does not establish exact scheduling or
fractional-boundary accuracy; input capture and countdown placement need terminal checks.

## Maintaining the documentation

Update this guide with behavior changes and check it against the release being shipped. Keep player
instructions in the [User Guide](UserGuide.md), implementation contracts here, and exhaustive details
in code/tests. Edit diagrams through their `.puml` sources, regenerate the SVGs and inspect them using
the [diagram maintenance notes](diagrams/README.md). Build the website with
`node .github/docs/build.mjs` after installing its dependencies; the builder checks local links.

## Appendix A: Product Scope

Keyboard Warrior provides terminal-based English and symbol typing practice through short combat,
puzzle, reaction and dialogue encounters. Players complete five scenarios and a two-phase boss,
with one resumable local run, ten recent responses and one all-time best attempt. Online accounts,
network multiplayer and shared online rankings are outside scope.

## Appendix B: User Stories

| As a player, I want to ... | So that ... |
| --- | --- |
| Receive WPM/accuracy feedback and performance-based damage | I can practise speed and accuracy |
| Have unfinished input captured at live-mode expiry | I receive feedback even when time runs out |
| Choose combat or events and earn skills, health or extra time | I can vary my route and improve later chances |
| Face progressing enemies and a two-phase final boss | My run has a clear challenge and conclusion |
| Select Easy, Normal, Hard or Coder | The challenge suits my ability |
| Replay a seed with the same gameplay sequence | I can reproduce generated content |
| Continue saved choices, combat or event stages | I retain earned progress between sessions |
| Review ten recent attempts and a persistent best score | I can track performance and set a target |
| Exit from any prompt | I can stop when needed |

## Appendix C: Non-Functional Requirements

| Quality | Requirement and verification |
| --- | --- |
| Portability | The same executable JAR launches with Java 25 on Windows, macOS and Linux without Gradle; check terminal and redirected-input startup/exit on each OS |
| Reliability | Malformed history, high-score and run-save files produce loading errors without ending the menu or silently replacing the file; compare bytes after each failed load |
| Data integrity | A destination remains untouched until its temporary replacement is fully written; verify failure/cleanup in storage tests. Separate files are not one transaction |
| Input stability | Countdown redraws preserve editable text and corrections; retain an edited line across at least five redraws and check captured input |
| Testability | Metrics accept supplied text/times without terminal, clock or file access and return immutable deterministic results; verify evaluator fixtures |
| Memory use | The edit-distance table uses two integer rows of `min(m,n)+1` code-point cells; inspect `AccuracyCalculator` and test unequal lengths in both orders. Input arrays are additional |

## Appendix D: Glossary

* **WPM**: Gross words per minute; five normalized submitted UTF-16 code units count as one word.
* **Character accuracy**: One minus normalized character edit distance, expressed as a percentage.
* **Edit distance**: Minimum number of character insertions, deletions and substitutions.
* **HP**: Hit points; zero defeats the player or opponent.
* **Scenario / phase**: One regular encounter stage / one boss enemy configuration.
* **Overkill**: Damage beyond a target's remaining HP, discarded at encounter boundaries.
* **Bonus time**: Signed run-wide countdown adjustment applied before minimums and mode scaling.
* **Checkpoint**: A stable saved stage with progress, excluding unfinished typed input.
* **Seed**: Number or text initializing the run's random sequence.
* **Live / plain mode**: Editable terminal input with expiry capture / complete-line input requiring Enter.
* **Survival outcome**: Event completion that permits progression if the player remains alive, even after failure.

## Appendix E: Instructions for Manual Testing

Use Java 25 and the release JAR in a fresh writable test folder, keeping real saves separate. Repeat
startup on Windows, macOS and Linux. Use Hard unless stated otherwise. Enter commands individually
when prompted; the [User Guide](UserGuide.md#command-summary) supplies full syntax. Each seeded event
check starts a **fresh run**, so earlier random draws do not change its content.

### Startup, input and progression

| Check | Inputs/actions | Expected result |
| --- | --- | --- |
| Release and plain startup | Run `java -jar KeyboardWarrior.jar`; also launch with piped `exit`, then empty input | Menu without Gradle; piped Exit/EOF terminates cleanly without terminal setup warnings |
| Menus and difficulty | Use numbers and names for `choose difficulty`; try an invalid choice; start once in each mode | Invalid input keeps the menu; HP is 400/200/100/80. Fresh Encryption allowance is 240/120/60/48 seconds |
| Seed and choice validation | Enter `start`, `42`; at offers enter `0`, `4`, `Ping`, then `1` | Invalid input keeps the same three offers; `1` starts combat. Repeat with a text seed and the same route; generated content repeats. Blank seed generates/displays a new seed |
| Skills and early feedback | Across turns select Ping, Brute Force, Buffer Overflow, SIGKILL and Encryption; submit partial/correct passages | Appropriate passage appears, Encryption uses symbols; WPM, accuracy, elapsed/required time and damage appear without a wrong-word list |
| Live capture | Type a partial passage, correct it with Backspace, retain it across countdown redraws, then let time expire; repeat without typing | Edited input is captured once, next input is clear; nonempty input has positive WPM. Empty input gives zero WPM, accuracy and damage; surviving enemy retaliates once |
| Plain late input | In an IDE Run console, wait beyond a combat allowance before submitting with Enter | No automatic empty attempt; actual elapsed time sets the score, with no separate late-combat penalty |
| Progression and boss | Choose combat `1` through five scenarios, then defeat both boss phases; include a lethal hit exceeding remaining HP | Regular HP is 30/35/50/60/70. Phase 2 starts at 70 HP; no transition healing/attack or carried overkill. Only phase-2 victory returns to the menu as a win |
| Defeat, restart and Exit | Allow enemy attacks to reduce HP to zero; Start again; try `exit` at menu, seed and gameplay prompts | Defeat returns to menu without progression; new run resets progress with selected mode's HP. Exit quits cleanly |

### Events and rewards

| Check | Inputs/actions | Expected result |
| --- | --- | --- |
| Puzzle | `start`, `42`, `2`, then `computer` on time; repeat with a wrong and a late answer | Answer stays hidden; success grants Overclock, failure applies -2 bonus seconds; survival advances once |
| Reaction | `start`, `0`, `2`; wait without Enter, then enter `roll` promptly; repeat with wrong/late input in live and plain modes | Command appears after a delay in both modes; timely match grants +10 HP, failure removes 30 HP. Plain mode resolves on Enter; live expiry resolves automatically |
| Merchant trade | `start`, `3`, `2`; enter `I will not be extorted`, then `Show me your rare wares` within their deadlines | Trade follows opening; only successful trade grants Overclock. Repeat with failed trade: -5 bonus seconds |
| Merchant escape | Repeat seed `3`, choice `2`; fail opening, then enter `flee` promptly; repeat with failed escape | Escape branch appears; success has no effect, failure removes 20 HP; survival still advances |
| Earned effects | After earning Overclock, use it accurately in combat; later succeed at another puzzle/trade | Performance adds time to later skill allowances; no duplicate reward skill, instead +20 HP. Healing may exceed starting HP |
| Mixed route | Complete five scenarios including events, then fight the boss | Events count once each, including two-response dialogue; both boss phases remain mandatory |

### Persistence and error handling

Always restart from the same test folder when checking persistence. For corrupted-file checks,
close the game and replace only the named file in this disposable folder with `invalid`, then relaunch.

| Check | Inputs/actions | Expected result |
| --- | --- | --- |
| Continue offers/combat | Exit at offers, restart and `continue`; repeat after a completed combat turn | Same offers or enemy HP/next attack, mode, skills, HP and bonus time return without extra rewards/attacks |
| Interrupted typing | Choose a skill, type partially, clear the line and enter `exit`; Continue and choose the same skill | Same passage with fresh timer; unfinished input is absent |
| Continue events | Exit during a puzzle, after merchant opening, while reaction waits, and after its command appears; Continue each separately | Same question/branch/command; prior effects do not repeat. Waiting reaction retains remaining delay; active responses get fresh deadlines |
| Save slot lifecycle | Earn healing/Overclock, Exit and Continue; then start a replacement run. Finally win/lose and select Continue | Earned effects survive restoration; Start replaces the slot; completed runs leave no active save. Records/Leaderboard remain |
| Records and Leaderboard | Complete an attempt, Exit/restart, view `records` and `leaderboard`; follow a best attempt with eleven lower-WPM attempts | Records keeps ten entries oldest first; best remains on Leaderboard after rotation and restart. Improve it and check persistence again |
| Empty views | In another fresh folder, choose a nondefault mode, then Records, Leaderboard and Continue | Empty/no-save messages; full menu stays usable with the selected mode |
| Loading errors | Corrupt `data/keyboardwarrior.txt`, `data/keyboardwarrior.txt.high-score` and `data/game-save.properties` one at a time; use Records, Leaderboard and Continue respectively | Loading error, unchanged malformed file and usable menu; no crash or silent reset |

For a balance check, take the all-combat Hard route at at least 50 WPM and 95% accuracy per attempt:
expect at least 40 damage, at most two attacks per enemy/phase and at least 10 HP at victory.
Automated progression tests establish the worst-case enemy rotation; one random manual run cannot.
