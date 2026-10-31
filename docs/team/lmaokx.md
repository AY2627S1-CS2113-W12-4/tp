# Nguyen Duc Khoa - Project Portfolio Page

## Overview

Keyboard Warrior is a Java terminal game that turns typing practice into combat: players choose
skills and type passages against a countdown, with speed and accuracy determining attack damage.
A complete run consists of five regular encounters followed by a two-phase final boss.

## Summary of Contributions

**Code contributed:** [tP Code Dashboard - lmaokx](https://nus-cs2113-ay2627-s1.github.io/tp-dashboard/?search=lmaokx&breakdown=true).

### Enhancements implemented

- **Game state and menu foundation**
  ([commit](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/ebff414)):
  Implemented the initial state-driven CLI loop, `State`, `StateMachine`, main menu and gameplay
  states. This separated input handling and transitions so teammates could extend gameplay
  independently. Later [extracted UI output](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/a0b3b8e)
  into a dedicated class to keep presentation separate from state logic.
- **Encryption skill**
  ([commit](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/40de89d)):
  Added special-character typing practice through randomly generated symbol passages, grouped into
  at most six characters, with a 60-second base deadline. Integrated the skill into the player
  loadout, challenge factory and combat flow, and tested symbol generation and timing.
- **Selectable game difficulty**
  ([commit](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/ed3f0d2)):
  Added Easy, Normal, Hard and Coder modes so players can choose a suitable challenge. Implemented
  menu selection by number or name and coordinated health, damage and countdown scaling across
  gameplay components. Tests cover all modes, retained selection and progression through regular
  encounters and boss phases.
- **Saving and continuing a run**
  ([save feature](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/129b832),
  [random-state persistence](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/b008480)):
  Implemented automatic saves at stable turn boundaries and Continue, preserving difficulty,
  health, encounter progress and enemy attack rotation without replaying a turn. Added validated
  save data, temporary-file replacement, error reporting and cleanup after victory or defeat.
  Extended the team's seeded gameplay to persist the random generator's position and passage
  history, preserving future enemies, passages and Encryption symbols after resuming. Tests
  compare resumed and uninterrupted runs at every encounter boundary and cover malformed files,
  failed writes and compatibility with older saves.
- **Earlier combat damage scaling**
  ([commit](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/9779ea2)):
  Implemented an earlier WPM-and-accuracy damage formula with combat regression tests. This
  contributed to the initial playable version; the final damage formula was subsequently revised
  by a teammate.

### Contributions to the User Guide

- Added [Main menu and game difficulty](../UserGuide.md#main-menu-and-game-difficulty) and
  [Saving and continuing a run](../UserGuide.md#saving-and-continuing-a-run), covering commands,
  difficulty effects, autosave boundaries and failure behaviour
  ([difficulty](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/ed3f0d2),
  [saves](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/129b832)).
- Updated [Skills and countdown](../UserGuide.md#skills-and-countdown) for Encryption and
  difficulty scaling, and documented the earlier damage calculation.

### Contributions to the Developer Guide

- Added [Game difficulty selection](../DeveloperGuide.md#game-difficulty-selection) and
  [Active run saves](../DeveloperGuide.md#active-run-saves): component responsibilities, scaling,
  snapshot validation, storage, restoration, error handling and test coverage
  ([difficulty](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/ed3f0d2),
  [saves](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/129b832)).
- Updated [Typing evaluation and damage](../DeveloperGuide.md#typing-evaluation-and-damage)
  for Encryption generation and timing, difficulty modifiers and the earlier damage formula;
  added manual acceptance checks for difficulty selection.

### Contributions to team-based tasks

- Set up project issues and updated necessary documentation, including the README and About Us.
- Resolved [merge conflicts during seed integration](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/a4d546e),
  preserving teammates' seeded enemy and passage selection alongside difficulty and save/resume.
- Fixed [cursor positioning for symbol passages](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/51b8e1f)
  and [typing-runner test failures](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/e3922e4),
  adding regression coverage and a controllable clock for timing tests without sleeps.

### Review/mentoring contributions

### Contributions beyond the project team
