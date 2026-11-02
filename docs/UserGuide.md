# Keyboard Warrior User Guide

Keyboard Warrior is a terminal game that turns typing practice into combat. Choose a skill, type
its passage, and deal damage based on your words per minute (WPM) and character accuracy. Survive
five regular encounters and both phases of the Kernel Overlord boss to win.

You can choose a game difficulty, replay seeded runs, continue saved progress, review your latest
ten attempts, and try to beat your all-time best typing score. **HP** means health points: when
your HP reaches zero, the run ends. Health carries between encounters without healing.

## Contents

- [Quick start](#quick-start)
- [Features](#features)
  - [Command conventions](#command-conventions)
  - [Starting a new run: `start`](#starting-a-new-run)
  - [Choosing game difficulty: `choose difficulty`](#main-menu-and-game-difficulty)
  - [Seeds and repeatable runs](#seeds-and-repeatable-runs)
  - [Skills and countdown](#skills-and-countdown)
  - [Combat and progression](#combat-and-progression)
  - [Typing feedback](#typing-feedback)
  - [Viewing typing history: `records`](#saved-typing-history)
  - [Viewing your best attempt: `leaderboard`](#leaderboard)
  - [Continuing a saved run: `continue`](#saving-and-continuing-a-run)
  - [Exiting the game: `exit`](#exiting-the-game)
  - [Saving and moving your data](#saving-and-moving-your-data)
- [FAQ](#faq)
- [Known issues](#known-issues)
- [Command summary](#command-summary)

## Quick start

1. Install **Java 25**. In a terminal, run `java -version` and check that the reported major
   version is `25`.
2. Download `KeyboardWarrior.jar` from the project's
   [Releases page](https://github.com/AY2627S1-CS2113-W12-4/tp/releases). Choose the JAR asset,
   rather than the source-code archive.
3. Put the JAR in a folder where you can create files. Open a terminal in that folder and run:

   ```text
   java -jar KeyboardWarrior.jar
   ```

4. The main menu appears. The following excerpt omits the decorative banner:

   ```text
   Current difficulty: Hard
   1. Start
   2. Records
   3. Exit
   4. Choose Difficulty
   5. Continue
   6. Leaderboard
   Enter a number or option name.
   ```

5. For a more forgiving first run, enter `4`, then enter `easy` when the difficulty menu appears.
   You return to the main menu with 400 starting HP selected for your next run.
6. Enter `start`. At the seed prompt, enter `42` or press Enter on an empty line for a random seed.
7. When the game lists your skills, enter `Ping`. Type the displayed passage as one line, then
   press Enter. In a supported terminal, the countdown also submits unfinished text at expiry.
8. Read your typing feedback and combat result, then choose another skill when prompted.
   Enter `exit` to quit; relaunch the game and use `continue` to resume an unfinished run.

**Enter each command separately and wait for the next prompt.** During a typing attempt, your
input is the answer to the passage; menu commands such as `records` do not open a menu there.
The command `exit` is available at every input stage.

The same release JAR runs on Windows, macOS and Linux with Java 25. No IDE, Gradle installation or
source code is needed. Use a native terminal for live countdown support; on Windows, you can use
Windows Terminal. If the game reports that the live countdown is unavailable, you can still play
by pressing Enter to submit. See [Known issues](#known-issues).

If you already have the source project, run `.\run-game.bat` on Windows or `sh ./run-game.sh` on
macOS/Linux from the project folder to build and start the game. See the
[project README](../README.md#running-the-game) for source setup.

## Features

### Command conventions

- Type a command or answer and press **Enter**. Backticks in this guide identify text to type;
  do not type the backticks themselves.
- Menu commands, difficulty names and skill names are case-insensitive. For example, `START`
  and `start` both work. **Capitalization and punctuation in typing passages affect accuracy.**
- Enter the whole option name, including internal spaces: `choose difficulty` and `Brute Force`.
  Leading and trailing spaces around commands are ignored.
- Numbers refer to the menu currently displayed. At the main menu, `3` exits; in the difficulty
  menu, `3` selects Hard; at the seed prompt, `3` is a seed. Skills are selected by name.
- Commands do not take extra arguments. Enter `start`, then `42` at the seed prompt; do not enter
  `start 42`. Likewise, choose a skill only when the skill list appears.
- An invalid menu choice or skill name produces an explanatory message and lets you try again.
  During typing, imperfect or empty answers are valid submissions and consume a combat turn.

<a id="starting-a-new-run"></a>

### Starting a new run: `start`

**Where:** Main menu. **Format:** `start` or `1`.

Starts at scenario 1 with full health for the selected game difficulty. You first enter a seed,
then the game displays your opponent, HP and available skills. All five player skills are
available from the start.

**Example:** Enter `start`, enter `42` at the seed prompt, then enter `Ping` at skill selection.
The game displays a passage and its time allowance, and timing begins after the passage is shown.

> **Starting a new run replaces your existing run save when the new encounter is successfully
> saved.** There is one run-save slot and no overwrite confirmation. Use `continue` to keep
> playing the saved run. Starting again keeps your typing history and leaderboard.

<a id="main-menu-and-game-difficulty"></a>

### Choosing game difficulty: `choose difficulty`

**Where:** Main menu. **Format:** `choose difficulty` or `4`.

Opens a second menu. Enter a number or difficulty name to select a mode and return to the main menu.

| Selection in difficulty menu | Player damage multiplier | Countdown multiplier | Starting HP |
| --- | --- | --- | --- |
| `1` or `easy` | x4 | x4 | 400 |
| `2` or `normal` | x2 | x2 | 200 |
| `3` or `hard` | x1 | x1 | 100 |
| `4` or `coder` | x0.8 | x0.8 | 80 |

**Example:** Enter `choose difficulty`, then `normal`. The main menu now shows
`Current difficulty: Normal`; your next new run starts with 200 HP.

Hard is the default on application startup. Winning or losing returns to the menu with that run's
difficulty selected. Choosing a different mode does not change an existing save: `continue`
restores the saved run's difficulty, even if the main menu currently shows another mode.

Game difficulty changes player damage, time allowances and starting HP. Enemy HP and attacks are
unchanged. It does not change the passage bank or directly multiply recorded WPM and accuracy.
Health is granted once at the start of a run and is not restored between fights.

Invalid input leaves you in the difficulty menu. Enter `exit` to quit the application.

### Seeds and repeatable runs

**Where:** Seed prompt after `start`. Enter one of the following:

| Input | Result |
| --- | --- |
| A signed 64-bit whole number, such as `42` or `-19` | Uses that number as the seed |
| Nonblank text, such as `practice-run` | Converts the trimmed text consistently to a numeric seed |
| An empty or whitespace-only line | Generates a random numeric seed |
| `exit`, in any capitalization | Quits the application; this word cannot be used as a seed |

The game displays the numeric seed before starting. Save that number if you want to replay the
run. Surrounding spaces are ignored; text seeds otherwise preserve capitalization. Numbers outside
the signed 64-bit range are treated as text.

Reusing a seed reproduces the same random choices **when you make the same decisions in the same
order**. Enemy and passage selection share a random sequence. Different skill choices, especially
Encryption, can change later passages and opponents. Your typing performance still determines
combat outcomes, so a seed alone does not guarantee an identical run.

Continuing a current save restores its random sequence and previous passage selections. You do
not need to re-enter the seed. Older saves without this information still restore combat progress,
but future random choices begin from a new sequence.

### Skills and countdown

**Where:** Skill selection during a run. **Format:** Type one of the skill names below.

| Skill name | Passage type | Base time allowance before game-difficulty scaling |
| --- | --- | --- |
| `Ping` | Easy | At least 15 seconds |
| `Brute Force` | Medium | At least 30 seconds |
| `Buffer Overflow` | Hard | At least 30 seconds |
| `SIGKILL` | Ultimate | At least 45 seconds |
| `Encryption` | Random special characters | Exactly 60 seconds |

**Example:** Enter `Brute Force` when the skills appear. The game selects a Medium passage and
shows its time limit. Type the passage itself next; typing another skill name at this point is
scored as your answer.

For the first four skills, longer passages receive more time. The game calculates the time needed
at 50 WPM, adds a 20% buffer and rounds up to a whole second, then takes the larger of that value
and the skill's minimum. It applies the game-difficulty multiplier and rounds up again.
**Use the displayed time limit for each attempt; the table gives minimums, not fixed deadlines.**

Encryption generates groups of one to six symbols separated by spaces, approximately as long as
a SIGKILL passage. Its symbol pool is:

```text
!@#$%^&*()\_+/*{}|:"<>?
```

Its countdown is exactly 240 seconds on Easy, 120 on Normal, 60 on Hard or 48 on Coder.

The game's input mode determines how an attempt ends:

| Behaviour | Supported terminal with live countdown | Plain-text console |
| --- | --- | --- |
| Time display | Updates above the passage while you type | Prints the time allowance without live updates |
| Pressing Enter | Submits your current answer | Submits your current answer |
| Deadline passes before Enter | Captures and scores unfinished input, then clears the input line | Waits for Enter, then scores the submitted answer |
| Late-attempt scoring | Uses captured text and actual elapsed time | Uses submitted text and actual elapsed time |

You can correct text before submission. Only the final captured or submitted text is scored;
mistakes that you corrected do not reduce accuracy. Submit one line even if the passage wraps
across several screen lines. An empty answer gives zero damage and a surviving enemy still attacks.
Expiry is announced with `Time's up!`; there is no separate timeout damage penalty.

All player skills use the same damage formula. Their names select the kind of typing practice
and its time allowance, rather than a fixed player attack strength.

### Combat and progression

Each turn follows this sequence: **choose a skill → type → receive feedback → deal damage**.
If the enemy survives, it retaliates before your next skill selection. A defeated enemy does not
retaliate. Regular enemies use a fixed repeating attack order associated with their type.

| Encounter | Starting enemy HP | After defeating it |
| --- | --- | --- |
| Scenario 1 | 30 | Advance to scenario 2 |
| Scenario 2 | 35 | Advance to scenario 3 |
| Scenario 3 | 50 | Advance to scenario 4 |
| Scenario 4 | 60 | Advance to scenario 5 |
| Scenario 5 | 70 | Begin the final boss |
| Kernel Overlord, phase 1 | 50 | Begin phase 2 |
| Kernel Overlord, phase 2 | 70 | Win the run and return to the main menu |

Regular enemy types are chosen randomly; scenario number determines their starting HP. The boss
deals 10 damage per retaliation in phase 1 and 20 in phase 2. Both phases must be defeated to win.

Your remaining HP carries into every encounter without healing. Excess damage is discarded when
an enemy is defeated, and a transition does not give the next enemy or boss phase a free attack.
If your HP reaches zero, the run ends and you return to the main menu.

Passages are selected within the chosen skill's difficulty. Selection avoids immediately reusing
the previous bank passage for that difficulty, including across encounters. Encryption generates
symbols instead of displaying a bank passage. Enemies and passages can reappear later in a run.

**A useful target on Hard:** 50 WPM and 95% accuracy gives 40 damage per attempt. Maintaining that
performance defeats every encounter in at most two attacks and leaves at least 10 HP after victory,
even against the strongest regular enemy choices. Lower speed or accuracy can require extra turns.

### Typing feedback

After each attempt, the game shows WPM, character accuracy, elapsed time, required time and combat
damage. Individual wrong words are not listed. Metrics display two decimal places, while scoring
uses the full values.

#### Words per minute (WPM)

WPM measures gross typing speed using five characters per word:

```text
WPM = (normalized submitted characters / 5) / (elapsed seconds / 60)
```

Leading and trailing whitespace is removed, and repeated internal spaces, tabs and line breaks
become one space. Letters, punctuation and remaining spaces count; Enter does not. Incorrect
characters also count toward gross WPM, so read speed alongside accuracy. Empty or whitespace-only
input gives 0 WPM.

#### Character accuracy

Accuracy measures the minimum number of character insertions, deletions and substitutions needed
to change the normalized expected passage into your normalized answer:

```text
Accuracy % = 100 * (1 - edit distance / longer normalized text length)
```

Capitalization and punctuation matter. A missing character counts as one edit instead of making
every later character wrong. Matching text gives 100% accuracy; an empty answer gives 0%.

#### Damage and the deadline

```text
Damage = floor(WPM * (accuracy percentage / 100)^4 * game difficulty multiplier)
```

`floor` means round down once, after applying the multiplier. Accuracy has a strong effect: on
Hard, 50 WPM gives 50 damage at 100% accuracy, 40 at 95%, and 20 at 80%.

| At 50 WPM and 95% accuracy | Easy | Normal | Hard | Coder |
| --- | --- | --- | --- | --- |
| Damage | 162 | 81 | 40 | 32 |

Every player skill follows this formula. Passing the deadline adds no separate penalty, although
longer elapsed time lowers WPM for the same submitted text. In a live terminal, expiry ends the
attempt and can trigger the surviving enemy's attack; in plain mode, Enter ends the attempt.

#### Example

For an **illustrative scoring example**, suppose the target were `Strike the goblin` with a
10-second allowance. Submitting `Strike teh goblin` in 6 seconds would display:

```text
WPM: 34.00
Accuracy: 88.24%
Elapsed time: 6.00 seconds
Required time: 10.00 seconds
```

The swapped letters require two edits. On Hard, this gives **20 damage**. Correctly typing the
same target in six seconds gives **100.00% accuracy** and **34 damage**. This short passage and
deadline illustrate the calculation; they are not a selectable in-game challenge. Actual passages
and deadlines are assigned by the game.

<a id="saved-typing-history"></a>

### Viewing typing history: `records`

**Where:** Main menu. **Format:** `records` or `2`.

Shows the latest ten evaluated typing attempts, oldest first, with WPM and accuracy for each.
The `#` column is the row's position in the current display, not a permanent record ID or score rank.
Each row represents one passage attempt, including partial or empty submissions, rather than a whole run.

**Example:** After completing several attempts, quit and relaunch from the same folder, then enter
`records`. Your saved attempts appear under `Typing history (oldest to newest)`. Once you have more
than ten saved attempts, the oldest entries are removed as new ones arrive.

With no records, the game displays `No typing records saved yet. Play a game to record your results.`
After results, an empty message or a loading error, the full main menu appears again with your
selected difficulty unchanged. Saving and loading problems are explained under [Known issues](#known-issues).

<a id="leaderboard"></a>

### Viewing your best attempt: `leaderboard`

**Where:** Main menu. **Format:** `leaderboard` or `6`.

Shows **one all-time best typing attempt**, with its WPM and accuracy. The highest WPM wins;
higher accuracy breaks equal-WPM ties. There is no minimum accuracy requirement. Exact ties
leave the saved best unchanged, and comparisons use full precision rather than the rounded display.

**Example:** If your best is 60 WPM at 95% accuracy, 50 WPM at 100% does not replace it.
60 WPM at 98% does. Enter `leaderboard` to view the current best.

The leaderboard is local and shared across runs, seeds and difficulty modes using the same data
folder. It ranks individual typing attempts, not total combat damage or whole-run scores.
The best attempt survives application restarts and removal from the ten-entry history.

On first use without a high-score file, the game initializes the best from the available history.
Previously discarded attempts cannot be recovered. With no attempts, it displays
`No typing records saved yet. Play a game to appear on the leaderboard.` The full menu reappears
after the result, empty message or loading error, keeping your selected difficulty.

<a id="saving-and-continuing-a-run"></a>

### Continuing a saved run: `continue`

**Where:** Main menu. **Format:** `continue` or `5`.

Restores the unfinished run at skill selection. Your HP, difficulty, scenario or boss phase,
opponent's HP and next attack are preserved. Current saves also preserve the random sequence and
previous passage selections. Continuing does not heal you or grant an extra enemy attack.

**Example:** Finish a typing attempt, wait for the next skill selection, then enter `exit`.
Relaunch from the same folder and enter `continue` to return to that saved turn.

Progress saves automatically at the start of each new encounter and after a completed turn while
the run remains active. There is no manual save command. Quitting during typing retains the last
successfully saved turn: the unfinished answer and remaining timer are not saved. On resuming,
you choose a skill again. With the same choices and combat progression, current saves reproduce
future random selections from that saved point.

There is **one save slot**. Starting a new run replaces it when saving succeeds. Winning or losing
removes the active save; typing history and the leaderboard remain. Changing difficulty at the
main menu does not modify the saved run.

If no save exists, the game displays `No saved game to continue. Choose Start to begin a new run.`
and stays in the menu. Invalid, unsupported or unreadable saves produce a loading error instead.

<a id="exiting-the-game"></a>

### Exiting the game: `exit`

**Where:** Any input stage. **Format:** `exit`; `3` also works at the main menu only.

Closes the application. Enter `exit` by itself and press Enter, including during seed entry,
difficulty selection or a typing attempt. In a live countdown, submit it before expiry to quit
without scoring that unfinished attempt. Merely typing it without Enter does not issue the command.

Exiting preserves the last successfully saved turn of an unfinished run. To view Records or
Leaderboard during an unfinished run, quit, relaunch, and use the main menu; `continue` then
resumes your saved progress. There is no command to return directly to the menu during combat.
End-of-input also closes the application.

### Saving and moving your data

The game creates its data files automatically. Paths are relative to the **folder from which you
launch the game**, which may differ from the JAR's folder if you launch it using a path.

| File | What it stores | Retention |
| --- | --- | --- |
| `data/keyboardwarrior.txt` | WPM and accuracy for evaluated attempts | Latest ten attempts |
| `data/keyboardwarrior.txt.high-score` | WPM and accuracy for the best attempt | Until a better attempt replaces it |
| `data/game-save.properties` | Progress of one unfinished run | Until replaced by a new run or removed after victory/defeat |

To transfer your progress, close the game and copy the **whole `data` folder** into the folder
from which you will launch it on the other computer. Back up any existing destination data before
replacing it. Install Java 25 and use the same game version there.

No manual file editing is required. Keep a backup if you change files yourself; malformed data can
prevent history, leaderboard or run loading. If saving fails, the game reports the error and
combat continues, but that progress or attempt may not be available after restarting. History,
high score and run progress are saved separately; an error can leave only some of them updated.

## FAQ

**Why is my damage low even when my WPM is high?**

Damage uses the fourth power of accuracy. Improving accuracy can help more than typing faster
with mistakes. See [Typing feedback](#typing-feedback) for examples.

**Do stronger-sounding skills automatically deal more damage?**

No. For the same WPM, accuracy and game difficulty, every player skill deals the same damage.
Skills change the passage and time allowance. Choose one suited to the practice you want.

**Why does Continue use a different difficulty from the main menu?**

Continue restores the saved run's mode. The menu selection applies to your next new run.

**Why is my best attempt missing from Records but still on the Leaderboard?**

Records keeps only ten attempts. The leaderboard stores your best separately, so it can outlive
its history row. Scores that look equal can also differ beyond the two displayed decimal places.

**Why did my records or saved game disappear after I moved the JAR?**

Check the folder from which you launched the game. It looks for `data` there. Launch from the
original folder or copy your data using [Saving and moving your data](#saving-and-moving-your-data).

**Can I pause the countdown or save an unfinished answer?**

No. You can exit and continue later, but the game resumes from the last successfully saved turn
at skill selection. It does not restore unfinished text or remaining seconds.

## Known issues

The following limitations and error messages can affect play. Use the suggested action before
trying again; the game does not need to be reinstalled for ordinary menu errors.

| Symptom or message | Meaning and suggested action |
| --- | --- |
| `Live countdown is unavailable in this console. Press Enter to submit each passage.` | Your console cannot provide the live input mode. Play by pressing Enter, or relaunch in a native terminal. IDE Run consoles and redirected input commonly use plain mode. |
| The displayed deadline has passed but the game is waiting | In plain mode this is expected: press Enter. The actual elapsed time is used, with no separate timeout penalty. |
| `java` is not found, or an unsupported class-version error appears | Check `java -version` in the same terminal and make Java 25 available before launching again. |
| `Unable to access jarfile` | Open a terminal in the folder containing `KeyboardWarrior.jar` and check the filename. Download the JAR asset rather than a source-code archive. |
| `Could not load typing history: ...` or `Could not load leaderboard: ...` | A data file may be unreadable or malformed; first-use leaderboard initialization can also fail while reading history or saving the initial best. Check access to the launch folder and restore a known-good backup if needed. The menu stays usable. |
| `Could not load game: ...` | The run save is unreadable, invalid or unsupported. Check file access and whether the save came from the same game version. Preserve a backup before choosing Start, which replaces the save when successful. |
| `Could not save typing history: ...` or `Could not save game: ...` | Combat continues, but the latest attempt or progress may not be saved. Check folder permissions and available disk space. Resume uses the last successful run save, if one exists. |
| `Could not remove finished game save: ...` | The completed run's old save may remain. Check file permissions; Continue may still load that earlier turn until the save is replaced or removed. |

Older saves that lack random-sequence information can restore combat progress but cannot reproduce
the original future passages and enemies. New saves retain that information.

## Command summary

Enter one input at a time and press Enter. Numbers apply only to the menu named in the first column.

| Where | Action | Accepted input | Result |
| --- | --- | --- | --- |
| Main menu | [Start a new run](#starting-a-new-run) | `1` or `start` | Prompts for a seed, then starts scenario 1 |
| Main menu | [View history](#saved-typing-history) | `2` or `records` | Shows the latest ten attempts, oldest first |
| Main menu | [Exit](#exiting-the-game) | `3` or `exit` | Closes the application |
| Main menu | [Choose difficulty](#main-menu-and-game-difficulty) | `4` or `choose difficulty` | Opens the difficulty menu |
| Main menu | [Continue](#saving-and-continuing-a-run) | `5` or `continue` | Loads the unfinished run at skill selection |
| Main menu | [View best attempt](#leaderboard) | `6` or `leaderboard` | Shows one all-time best attempt |
| Difficulty menu | Select a mode | `1` / `easy`, `2` / `normal`, `3` / `hard`, or `4` / `coder` | Selects the mode and returns to the main menu |
| Seed prompt | [Choose a seed](#seeds-and-repeatable-runs) | A number, text other than `exit`, or an empty line | Starts a seeded or random run |
| Skill selection | [Choose a skill](#skills-and-countdown) | `Ping`, `Brute Force`, `Buffer Overflow`, `SIGKILL`, or `Encryption` | Displays and starts a typing challenge |
| Typing attempt | Submit an answer | Type the passage and press Enter | Scores the answer and resolves the combat turn |
| Any input stage | [Exit](#exiting-the-game) | `exit` | Closes the application, retaining the last successful run save |
