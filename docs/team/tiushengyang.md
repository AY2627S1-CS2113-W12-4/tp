# Tiu Sheng Yang - Project Portfolio Page

## Overview

Keyboard Warrior is a Java terminal game that turns typing practice into combat. Players choose
skills and type passages against a countdown, with typing speed and accuracy determining attack
damage. A complete run consists of five regular encounters followed by a two-phase final boss.

## Summary of Contributions

**Code contributed:** [tP Code Dashboard - shengyang2010](https://nus-cs2113-ay2627-s1.github.io/tp-dashboard/?search=shengyang2010&sort=groupTitle&sortWithin=title&timeframe=commit&groupSelect=groupByAuthors&breakdown=true&checkedFileTypes=docs~functional-code~test-code~other).

### Enhancements implemented

- **Challenge countdown and typing-result integration**
  ([PR #9](https://github.com/AY2627S1-CS2113-W12-4/tp/pull/9)):
  Implemented `ChallengeClock` to track remaining and elapsed time using `System.nanoTime()`,
  keeping elapsed-time measurements independent of system-clock changes. Integrated the countdown
  into gameplay and the initial terminal display, and connected submitted or timed-out attempts
  to the existing typing evaluator and result formatter so players receive feedback after each attempt.
- **Persistent typing history**
  ([PR #23](https://github.com/AY2627S1-CS2113-W12-4/tp/pull/23)):
  Implemented `SaveStore` to save WPM and accuracy automatically after each evaluated attempt.
  Retained the latest ten records in chronological order across application restarts and added the
  `records` main-menu command to display them. Added empty-history and loading-error messages,
  while allowing combat to continue if saving fails.
- **All-time leaderboard**
  ([PR #36](https://github.com/AY2627S1-CS2113-W12-4/tp/pull/36)):
  Added the `6` / `leaderboard` menu option and `LeaderboardStore` to retain one best typing
  attempt. Scores rank by WPM, with accuracy breaking ties. Stored the best attempt separately
  so it survives restarts and removal from the ten-entry history, and initialized it from existing
  history when no high-score file exists. Updated Records and Leaderboard to redisplay the full
  menu after results, empty messages or loading errors while preserving the selected difficulty.
- **Shared file-storage implementation**
  ([PR #36](https://github.com/AY2627S1-CS2113-W12-4/tp/pull/36)):
  Extracted `FileStore` for shared path handling and file replacement, and reused it in
  `SaveStore`, `LeaderboardStore` and the existing `GameSaveStore`. Writes go to a temporary
  file before replacing the destination, using an atomic move where supported and cleaning up
  temporary files on failure. This reduces duplicated storage code and protects existing data
  when writing new contents fails.

### Contributions to documentation

- **User Guide:** Updated [Saved typing history](../UserGuide.md#saved-typing-history) and
  [Leaderboard](../UserGuide.md#leaderboard) with commands, file locations, retention rules,
  score comparisons and error behaviour. Clarified that each record represents one typing
  attempt and that the leaderboard is shared across difficulty modes and seeds in the same folder
  ([PR #36](https://github.com/AY2627S1-CS2113-W12-4/tp/pull/36),
  [PR #41](https://github.com/AY2627S1-CS2113-W12-4/tp/pull/41)).
- **Developer Guide:** Expanded
  [Records and all-time leaderboard](../DeveloperGuide.md#records-and-all-time-leaderboard)
  to explain class responsibilities, file formats, high-score initialization, save ordering,
  menu behaviour, validation and design alternatives. Added automated-test coverage descriptions
  and manual acceptance checks under [Testing](../DeveloperGuide.md#testing)
  ([PR #36](https://github.com/AY2627S1-CS2113-W12-4/tp/pull/36),
  [PR #41](https://github.com/AY2627S1-CS2113-W12-4/tp/pull/41)).

### Testing and code quality

- Added clock tests for initial state, zero and negative durations, expiry and restarting,
  alongside gameplay and terminal tests for countdown integration and typing feedback.
- Added storage and menu tests for saved metrics, latest-ten retention across reopened stores,
  records display, empty history and loading errors.
- Added leaderboard regressions for score ordering, exact ties, persistence after history rotation,
  initialization from existing history, malformed high-score data and menu redisplay.
- Added a `FileStore` regression test that simulates a partial write failure and verifies that
  the previous save remains intact and the temporary file is removed.
- Added assertions to `ChallengeClock` to check internal duration and elapsed-time invariants
  ([PR #18](https://github.com/AY2627S1-CS2113-W12-4/tp/pull/18)), and removed unused UI methods
  during leaderboard integration.
