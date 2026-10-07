# Keyboard Warrior User Guide

## Introduction

Keyboard Warrior combines typing practice with a planned roguelike adventure. Players will face typing
challenges in situations such as fighting monsters, talking to characters and exploring new areas.

This guide covers the implemented feedback component for v1.0 tasks **3.1** (WPM and accuracy) and
**3.2** (wrong words), including the time limit that determines whether damage is allowed.
Connecting this component to the scenario loop, timer and gameplay CLI is pending.
Launching the current application still asks for your name and prints a greeting; it does not yet run a
typing challenge. The reports below show the output produced by the feedback component.

## Quick Start

1. Install Java 25.
2. From the project folder, run `./gradlew run` on macOS/Linux or `.\gradlew.bat run` in Windows PowerShell.
3. Enter your name when prompted.

The [Developer Guide](DeveloperGuide.md) explains how to try the feedback component before gameplay integration.

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
spent typing and submitting the answer.

* If elapsed time exceeds required time, the attempt deals **zero damage**, even if every word is correct.
* If elapsed time is less than or equal to required time, damage is allowed by the time limit.

WPM, accuracy and word feedback are still shown for late attempts. The feedback component determines
whether the time limit allows damage; the combat component determines the damage amount.
Both time values must be positive and finite.

### Wrong words

The report lists word positions starting at 1 and distinguishes three kinds of error:

| Error | Example report |
| --- | --- |
| Wrong word | `Word 2: expected "the", typed "teh"` |
| Missing word | `Word 3: missing "goblin"` |
| Extra word | `Word 4: unexpected "now"` |

Words are compared at corresponding positions. An inserted or omitted word in the middle can shift
later words, so several positions may be reported as incorrect. Mistakes corrected before submission
are not included: the component evaluates only the final submitted text.

### Example

For the challenge `Strike the goblin` with a **10-second limit**, submitting `Strike teh goblin`
in **6 seconds** produces:

```text
WPM: 34.00
Accuracy: 66.67%
Elapsed time: 6.00 seconds
Required time: 10.00 seconds
Damage allowed: Yes
Wrong words:
  Word 2: expected "the", typed "teh"
```

Submitting `Strike the goblin` in the same time produces:

```text
WPM: 34.00
Accuracy: 100.00%
Elapsed time: 6.00 seconds
Required time: 10.00 seconds
Damage allowed: Yes
Wrong words:
  None
```

If the required time were **5 seconds**, the same correct submission would produce:

```text
WPM: 34.00
Accuracy: 100.00%
Elapsed time: 6.00 seconds
Required time: 5.00 seconds
Damage: 0 (time limit exceeded)
Wrong words:
  None
```

Metrics and times are displayed to two decimal places. The time limit uses the actual values,
so even a small overrun blocks damage when rounded times look equal.
