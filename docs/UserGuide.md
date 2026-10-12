# Keyboard Warrior User Guide

## Introduction

Keyboard Warrior is a terminal typing combat game. Choose an attack skill, type its prompt, and
deal damage based on your words per minute (WPM) and accuracy. A live countdown shows your
time remaining; exceeding the time limit reduces damage by 20%. Defeat the enemies in five
scenarios to win v1.0.

## Quick Start

1. Install Java 25.
2. From the project folder, run `sh ./run-game.sh` on macOS/Linux or `.\run-game.bat` in Windows PowerShell,
   using IntelliJ's Terminal or a native terminal.
3. At the main menu, type `start` and press Enter to begin the first scenario.
4. Type a displayed skill name, such as `Ping`, and press Enter.
5. Type the displayed prompt as one line and press Enter before the timer reaches zero for full damage.
   If time expires first, the game automatically evaluates what you have typed so far with a 20% damage penalty.

The launchers compile through Gradle, then run the game directly in your terminal with assertions enabled.
Gradle `run` or IntelliJ's Run console can redirect input and prevent the live countdown from working.
If the game reports an unsupported console, use the displayed launcher command in a native terminal;
on Windows, try Windows Terminal if IntelliJ's Terminal remains unsupported.
Type `exit` and press Enter at any phase to quit. If you launch directly from IntelliJ,
add `-ea` to the run configuration's **VM options**, following the [README setup instructions](../README.md#setting-up-in-intellij).

## Combat and Scenarios

Each player turn begins with a skill choice. Skill names are case-insensitive.

| Skill | Base damage | Typing time limit |
| --- | --- | --- |
| `Ping` | 10 | 15 seconds |
| `Brute Force` | 20 | 30 seconds |
| `Buffer Overflow` | 40 | 30 seconds |
| `SIGKILL` | 80 | 45 seconds |

The live timer appears above "Type the following..." and the passage. It starts when the prompt
is shown and displays the seconds remaining while you type.
Enter submits one attack attempt; you do not need perfect accuracy to attack. The game shows
WPM, accuracy, elapsed time, the time limit and damage after your attempt. Individual wrong words
are not listed. If the enemy survives, it attacks and you choose a skill for your next turn.
An expired challenge applies a 20% damage penalty and still consumes your turn. Its feedback uses
the current input, including text you have not submitted with Enter.

Defeating an enemy automatically starts the next numbered scenario. Each scenario randomly
selects an enemy from a pool of different monsters; its name and HP appear when combat starts.
Each attack randomly selects a passage from the chosen skill's difficulty pool, so passages
can differ between turns. The same difficulty will not repeat its previous passage immediately;
enemies and passages can reappear later.

You start with 100 HP, and remaining HP carries between scenarios. Defeating the fifth enemy
wins the run. If your HP reaches zero, the run ends in defeat. Both outcomes return you to the
main menu, where `start` begins a fresh run with 100 HP.

## Typing Feedback

### Words per minute (WPM)

WPM measures typing speed using five characters as one word:

```text
WPM = (submitted characters / 5) / (elapsed seconds / 60)
```

Leading and trailing spaces, tabs and line breaks are removed. Repeated internal whitespace is replaced
with one space. The remaining letters, punctuation and spaces count toward WPM; Enter does not.

This is **gross WPM**: incorrectly submitted characters also count toward speed. Use the accuracy result
alongside WPM to understand your performance. The supplied typing duration must be positive and finite.

### Accuracy percentage

Accuracy measures correctly typed **words**, rather than individual characters:

```text
Accuracy = matching word positions / max(challenge words, submitted words) * 100
```

Words must match exactly, including capitalization and punctuation. Extra and missing words reduce
accuracy. An empty or whitespace-only submission gives 0 WPM and 0% accuracy.

### Time limit and damage

Each challenge supplies a required time, measured in seconds. Your elapsed time is the time you
spent typing until you submit the answer or the countdown expires.

* If elapsed time exceeds required time, the attempt deals **20% less damage**.
* If elapsed time is less than or equal to required time, there is no time penalty.

WPM and accuracy are still shown for late attempts. The feedback component supplies the
time penalty; the combat component determines the damage amount.
Both time values must be positive and finite.

Combat calculates:

```text
Damage = base damage * (accuracy percentage + WPM) / 100 * time multiplier
Time multiplier = 0.8 if elapsed time exceeds the limit, otherwise 1.0
```

The result is rounded down to an integer after the time penalty is applied. For example, `Brute Force`
with 80% accuracy and 40 WPM deals `20 * (80 + 40) / 100 = 24` damage within the time limit.
With the same metrics after the limit, it deals `24 * 0.8 = 19.2`, rounded down to **19 damage**.
Enemy attacks use their skill's base damage.
If no answer is submitted before expiry, the game evaluates the text currently in your input line
and applies the 20% damage penalty. An empty input gives 0 WPM, 0% accuracy and zero damage.
Corrections made before submission or expiry count: the game evaluates the final text in the input line.

### Example

For the challenge `Strike the goblin` with a **10-second limit**, submitting `Strike teh goblin`
in **6 seconds** produces:

```text
WPM: 34.00
Accuracy: 66.67%
Elapsed time: 6.00 seconds
Required time: 10.00 seconds
Damage penalty: 0%
```

Submitting `Strike the goblin` in the same time produces:

```text
WPM: 34.00
Accuracy: 100.00%
Elapsed time: 6.00 seconds
Required time: 10.00 seconds
Damage penalty: 0%
```

If the required time were **5 seconds**, the same correct submission would produce:

```text
WPM: 34.00
Accuracy: 100.00%
Elapsed time: 6.00 seconds
Required time: 5.00 seconds
Damage penalty: 20% (time limit exceeded)
```

Metrics and times are displayed to two decimal places. The time limit uses the actual values,
so even a small overrun applies the 20% penalty when rounded times look equal.
