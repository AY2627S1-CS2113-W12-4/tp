# Keyboard Warrior User Guide

## Introduction

Keyboard Warrior is a terminal typing combat game. Choose a skill, type its passage and deal
damage based on your WPM and character accuracy. Survive five regular scenarios and both phases
of the final Kernel Overlord boss to win. Your starting HP depends on the selected game difficulty and carries
between all encounters;
there is no healing during a run.

## Quick Start

1. Install Java 25.
2. From the project folder, run `sh ./run-game.sh` on macOS/Linux or `.\run-game.bat` in Windows PowerShell,
   using IntelliJ's Terminal or a native terminal.
3. Type `start` and press Enter at the main menu.
4. Type a displayed skill name, such as `Ping`, and press Enter.
5. Type the passage. Press Enter to submit, or let the countdown submit your current text automatically.

The launchers compile through Gradle and start Java directly with assertions enabled. Gradle `run`
and IntelliJ's Run console may redirect input and prevent the live timer from working. If the game
reports an unsupported console, use the displayed launcher command in a native terminal. On Windows,
try Windows Terminal if IntelliJ's Terminal remains unsupported.

Type `exit` and press Enter at any phase to quit. To run an existing packaged demo, use Java 25 in a
supported terminal:

```text
java -ea --enable-native-access=ALL-UNNAMED -jar KeyboardWarrior.jar
```

## Saved typing history

At the main menu, type `records` and press Enter to view the latest ten typing attempts, oldest
first. Each entry shows WPM and accuracy. Viewing records keeps you in the main menu, where you
can enter `start`, `records`, `exit` or `choose difficulty`.

Every submitted attempt is saved automatically, including partial or empty text captured when
the timer expires. History survives restarting the application. The file is
`data/keyboardwarrior.txt`, relative to the folder from which the game is launched.

If there is no history yet, the menu displays an empty-history message. If the file cannot be read
or contains an invalid record, it displays an error. A saving error is reported while the current
combat turn continues.

## Main menu and game difficulty

The main menu has four options. Enter a number or its name (names are case-insensitive):

1. Start
2. Records
3. Exit
4. Choose Difficulty

Choose Difficulty opens a menu with `1. Easy`, `2. Normal`, `3. Hard` and `4. Coder`.
Enter a number or difficulty name to select it and return to the main menu. Invalid input keeps
you in the selection menu; `exit` quits the application.

| Game difficulty | Player damage multiplier | Countdown multiplier | Starting HP |
| --- | --- | --- | --- |
| Easy | x4 | x4 | 400 |
| Normal | x2 | x2 | 200 |
| Hard (default) | x1 | x1 | 100 |
| Coder | x0.8 | x0.8 | 80 |

The current difficulty appears in the main menu and at the start of a run. It applies throughout
all five scenarios and both boss phases. Returning to the menu after winning or losing keeps the
selection for your next run. Restarting the application resets it to Hard.

Starting health is multiplied once at the start of a run; health is not restored between fights.
Enemy health and attacks are unchanged. Game difficulty does not change the passages selected by skills
or your recorded WPM and accuracy.

## Skills and countdown

Each turn begins with a skill choice. Names are case-insensitive.

| Skill | Passage difficulty | Base minimum time limit (Hard) |
| --- | --- | --- |
| `Ping` | Easy | 15 seconds |
| `Brute Force` | Medium | 30 seconds |
| `Buffer Overflow` | Hard | 30 seconds |
| `SIGKILL` | Ultimate | 45 seconds |
| `Encryption` | Ultimate (random special characters) | Exactly 60 seconds |

`Encryption` generates a fresh random sequence using the symbols `!@#$%^&*()\_+/*{}|:"<>?`.
Spaces separate groups of one to six characters. The passage is approximately the same length as
a `SIGKILL` passage, with a base time limit of exactly 60 seconds.
Its final countdown is 240 seconds on Easy, 120 on Normal, 60 on Hard or 48 on Coder.

For the other skills, the selected passage can increase that limit: the game allows enough time to type its normalized
character count at 50 WPM, adds a 20% time buffer and rounds up to a whole second. It uses whichever
is larger: that allowance or the skill's minimum. The game then multiplies that base time by the
selected game difficulty's multiplier and rounds up to a whole second. Check the displayed time
limit for each attempt.

The timer appears above "Type the following..." and updates while you type. Enter submits one
attempt, including an imperfect or empty answer. If time expires first, the game captures your
current input, clears the input line and scores the captured text. Corrections made before submission
or expiry count; previously corrected mistakes do not affect the result.

Every skill uses the same player damage formula. The skill changes the passage and its time
allowance, so choose a difficulty suited to the typing practice you want. Enemy attacks use their
own fixed skill damage.

## Combat and progression

The game shows WPM, character accuracy, elapsed time, required time and attack
damage. Individual wrong words are not printed. There is no separate damage deduction for exceeding
the deadline; your captured text and actual elapsed time still determine speed and accuracy.
Expiry is announced with "Time's up".
If the enemy survives, it attacks before your next skill choice. A defeated enemy does not retaliate.

| Encounter | Starting enemy HP | What happens after victory |
| --- | --- | --- |
| Scenario 1 | 30 | Start scenario 2 |
| Scenario 2 | 35 | Start scenario 3 |
| Scenario 3 | 50 | Start scenario 4 |
| Scenario 4 | 60 | Start scenario 5 |
| Scenario 5 | 70 | Start the final boss |
| Kernel Overlord, phase 1 | 50 | Start phase 2; boss attacks deal 10 damage in phase 1 |
| Kernel Overlord, phase 2 | 70 | Win the run; boss attacks deal 20 damage in phase 2 |

Each regular scenario randomly selects an enemy name and attack rotation. Scenario number determines
its starting HP. Passages are also random within the chosen difficulty, and the same difficulty does
not repeat its previous passage immediately. Enemies and passages can reappear later.

Your remaining HP carries into each scenario and boss phase without healing. Damage beyond the
current enemy's remaining HP is discarded: it does not carry into the next enemy or boss phase.
Starting a new phase does not grant the boss an extra attack. Defeating phase 1 alone does not win.

On Hard, at 50 WPM and 95% character accuracy, each attack deals 40 damage. This is enough to defeat each
regular enemy and each boss phase in at most two attacks. With that performance throughout a run,
the strongest possible regular enemy choices leave you with at least 10 HP after victory.
Slower or less accurate attempts can require extra turns and lead to defeat.

If your HP reaches zero, the run ends immediately. Victory or defeat returns to the main menu;
`start` begins a new run at scenario 1 with the selected difficulty's starting HP.

## Typing feedback

### Words per minute (WPM)

WPM is gross typing speed, using five characters as one word:

```text
WPM = (normalized submitted characters / 5) / (elapsed seconds / 60)
```

Leading and trailing whitespace is removed, and repeated internal whitespace becomes one space.
The remaining letters, punctuation and spaces count toward WPM; Enter does not. Incorrect characters
also count toward gross WPM, so read speed alongside accuracy. Empty or whitespace-only input gives
0 WPM.

### Character accuracy

Accuracy uses the minimum number of single-character insertions, deletions and substitutions needed
to change the normalized expected text into the normalized input:

```text
Accuracy % = 100 * (1 - edit distance / longer normalized text length)
```

Capitalization and punctuation count. A single missing character reduces accuracy for that edit
without shifting every later character into an incorrect position. Matching text gives 100%; empty
input gives 0%. The game evaluates the final text rather than the sequence of keystrokes used to
produce it.

### Damage and the deadline

```text
Damage = floor(WPM * (accuracy percentage / 100)^4 * game difficulty multiplier)
```

`floor` means round down to a whole number once, after applying the multiplier. On Hard, at 50 WPM
and 100% accuracy, damage is 50. At 50 WPM
and 95% accuracy, it is `floor(50 * 0.95^4) = 40`. Accuracy has a strong effect: at 50 WPM and 80%
accuracy, damage is 20. This formula applies to every player skill.

At 50 WPM and 95% accuracy, damage is 162 on Easy, 81 on Normal, 40 on Hard or 32 on Coder.

Elapsed and required times must be positive, finite values. A result is overdue only when elapsed
time is strictly greater than required time. The summary shows both times. Expiry is announced with
"Time's up" rather than a separate status line.
Automatic expiry still ends the attempt and can trigger the surviving enemy's turn. It adds no
separate damage penalty.
Displayed metrics use two decimal places; calculations use the full values.

### Example

On Hard, for `Strike the goblin`, submitting `Strike teh goblin` in 6 seconds gives:

```text
WPM: 34.00
Accuracy: 88.24%
Elapsed time: 6.00 seconds
Required time: 10.00 seconds
```

The swapped letters require two character edits. Damage is **20**, after applying the accuracy
formula and rounding down. The correct text in the same six seconds gives **100.00% accuracy** and
**34 damage**. Supplying a five-second deadline instead would mark the six-second result overdue;
the same text and elapsed time would produce the same damage.
