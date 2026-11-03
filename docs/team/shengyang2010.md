# Tiu Sheng Yang - Project Portfolio Page

## Overview

Keyboard Warrior is a Java terminal game that turns typing practice into combat: players choose
skills and type passages against a countdown, with speed and accuracy determining damage.
Players survive five regular encounters and a two-phase boss, with selectable difficulty,
repeatable seeded runs and saved progress.

## Summary of Contributions

**Code contributed:** [tP Code Dashboard - shengyang2010](https://nus-cs2113-ay2627-s1.github.io/tp-dashboard/?search=shengyang2010&breakdown=true).

### Enhancements implemented

- **Challenge countdown and result integration** ([PR #9](https://github.com/AY2627S1-CS2113-W12-4/tp/pull/9)).
  Implemented `ChallengeClock` using `System.nanoTime()` to measure remaining and elapsed time
  independently of system-clock changes. Integrated the initial countdown display and connected
  submitted/timed-out attempts to the team's existing evaluator and formatter. This gives players
  timed practice and immediate feedback. Tests cover expiry, restarting, invalid durations and
  gameplay integration; [PR #18](https://github.com/AY2627S1-CS2113-W12-4/tp/pull/18) adds assertions
  for internal timing invariants.
- **Persistent typing history** ([PR #23](https://github.com/AY2627S1-CS2113-W12-4/tp/pull/23)).
  Implemented `SaveStore` to save WPM and accuracy after each evaluated attempt, retain the latest
  ten records across restarts, and display them through `records`. This lets players compare
  recent performance. Covered storage, gameplay and menu integration, including empty history,
  loading errors and combat continuing after a saving failure.
- **All-time leaderboard** ([PR #36](https://github.com/AY2627S1-CS2113-W12-4/tp/pull/36)).
  Added `leaderboard` and `LeaderboardStore` to retain one best attempt, ranked by WPM with accuracy
  breaking ties. A separate high-score file preserves achievements after history rotation and
  restarts. Initialized it from existing history before trimming old records, avoiding loss of
  the previous best. Tests cover migration, rotation, improved scores, exact ties and malformed
  data. Records and Leaderboard redisplay the full menu while preserving the selected difficulty.
- **Reusable file storage** ([PR #36](https://github.com/AY2627S1-CS2113-W12-4/tp/pull/36)).
  Extracted `FileStore` and adapted history, leaderboard and the existing run-save store to reuse
  path handling and temporary-file replacement. Atomic replacement is used where supported,
  with a regular-move fallback. A simulated partial-write failure test verifies that the original
  file survives and temporary files are removed. This reduces duplicated storage logic; the
  separate history and high-score writes are not a single transaction.
- **SLAP refactoring of gameplay and persistence**
  ([commit 0c79083](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/0c790835d17bd3195ea90f5733d9613a4ab88794)).
  Applied the Single Level of Abstraction Principle to `GamePlayState`, `MainMenuState`,
  `DialogueEncounter`, `GameSave`, `GameSaveStore` and `EncounterFactory`. Extracted private helpers
  for attempt processing, menu actions, dialogue effects, save validation, serialization and
  restoration so coordinating methods expose the order of operations. Preserved gameplay and
  save formats, including history-write error handling and one-time rewards. Verified the changes
  with all 263 existing tests and Checkstyle passing on Java 25.

### Contributions to the User Guide

- Updated [Saved typing history](../UserGuide.md#saved-typing-history) and
  [Leaderboard](../UserGuide.md#leaderboard), explaining commands, retention, ranking, shared local
  data and error behaviour ([PR #36](https://github.com/AY2627S1-CS2113-W12-4/tp/pull/36),
  [PR #41](https://github.com/AY2627S1-CS2113-W12-4/tp/pull/41)).
- Reorganized the guide around AB3's structure. Expanded [Quick start](../UserGuide.md#quick-start),
  added [command conventions](../UserGuide.md#command-conventions), command formats and examples,
  [FAQ](../UserGuide.md#faq), [Known issues](../UserGuide.md#known-issues) and a
  [Command summary](../UserGuide.md#command-summary). Clarified input stages, save replacement,
  live/plain countdown behaviour and the illustrative scoring example for peer testers.

### Contributions to the Developer Guide

- Expanded [ChallengeClock implementation](../DeveloperGuide.md#challengeclock-implementation)
  with monotonic timing, remaining-time rounding, expiry and restart behaviour, fractional elapsed
  time and assertion checks. Documented the five existing
  [clock unit tests](../DeveloperGuide.md#challengeclock-unit-tests), their expected results,
  the focused test command and coverage limitations.
- Expanded [Records and all-time leaderboard](../DeveloperGuide.md#records-and-all-time-leaderboard)
  with responsibilities, formats, initialization, save ordering, validation, menu behaviour and
  design alternatives. Documented regression coverage and manual checks in
  [Testing](../DeveloperGuide.md#testing)
  ([PR #36](https://github.com/AY2627S1-CS2113-W12-4/tp/pull/36),
  [PR #41](https://github.com/AY2627S1-CS2113-W12-4/tp/pull/41)).
- Added the [typing-history save UML sequence diagram](../diagrams/typing-history-save.svg) and
  [editable PlantUML source](../diagrams/typing-history-save.puml). It explains initialization
  and high-score updates before history rotation, plus the boundary between storage errors and
  combat. Updated the diagram maintenance index.
- Documented the [SLAP decomposition](../DeveloperGuide.md#single-level-of-abstraction-principle-slap),
  including helper responsibilities and the ordering needed to preserve gameplay. Updated the
  save/restoration explanation and boss-transition sequence to match the extracted helpers.

### Contributions to team-based tasks

- Created implementation tasks for the [countdown](https://github.com/AY2627S1-CS2113-W12-4/tp/issues/8)
  and [score saving](https://github.com/AY2627S1-CS2113-W12-4/tp/issues/21).
- Integrated storage with teammates' gameplay, menu and run-save components; updated affected tests
  and removed unused UI methods during [leaderboard integration](https://github.com/AY2627S1-CS2113-W12-4/tp/pull/36).

### Review and mentoring contributions

- Left brief feedback on a teammate's
  [DG update](https://github.com/AY2627S1-CS2113-W12-4/tp/pull/13#discussion_r4357678123)
  and [bug-fix PR](https://github.com/AY2627S1-CS2113-W12-4/tp/pull/25#discussion_r4357709481),
  acknowledging the documentation improvement and reporting that the fix worked. Also left a
  [brief comment on the switch-case implementation](https://github.com/AY2627S1-CS2113-W12-4/tp/pull/59#discussion_r4449057066)
  in `EncounterSnapshot` during the v2.1 finalization PR.

### Contributions beyond the project team

- Reviewed TripJio's Developer Guide and
  [recommended shorter, more concise explanations](https://github.com/NUS-CS2113-AY2627-S1/tp/pull/1#discussion_r4396927065).
  Also [commented on diagram readability in HTML](https://github.com/NUS-CS2113-AY2627-S1/tp/pull/1#discussion_r4396935158)
  during the cross-team documentation review.
