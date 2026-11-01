# Koh Yong De - Project Portfolio Page

## Overview

Keyboard Warrior is a Java terminal game that turns typing practice into combat. Players choose
skills and type passages against a countdown, with typing speed and accuracy determining attack
damage. A complete run consists of five regular encounters followed by a two-phase final boss.

## Summary of Contributions

**Code contributed:** [tP Code Dashboard - Yong-De](https://nus-cs2113-ay2627-s1.github.io/tp-dashboard/?search=Yong-De&sort=groupTitle&sortWithin=title&timeframe=commit&groupSelect=groupByAuthors&breakdown=true&checkedFileTypes=docs~functional-code~test-code~other).

### Enhancements implemented

- **Combat model and encounter foundation**
  ([initial structure](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/d1e4d3c),
  [combat functionality](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/36f42ee)):
  Implemented the initial `Entity`, `Player`, `Enemy` and skill components, together with
  `Encounter`, `CombatEncounter` and `EncounterOutcome`. Connected player skill selection and
  typing results to damage resolution, cycled through enemy attacks and recorded victory or
  defeat. Added `CombatUi` and a sample combat entry point. This foundation supports the current
  encounter flow; teammates subsequently extended it with nonblocking turns, revised damage,
  difficulty modifiers, boss progression and save snapshots.
- **Typing challenges for combat skills**
  ([commit](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/36f42ee)):
  Added the initial `PromptBank`, `TypingChallenge`, `TypingChallengeFactory` and
  `TypingChallengeRunner` to associate skills with passages and deadlines. The runner connected
  the team's clock and evaluator to combat input, returning a result for damage resolution.
  This separated challenge preparation from evaluation and combat effects. The current factory
  also incorporates teammates' passage-length allowances, Encryption prompts and difficulty scaling.
- **Shared randomness for enemies and passages**
  ([commit](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/93398d7)):
  Passed the run's random source into `PromptBank` and `TypingChallengeFactory`, sharing it with
  enemy selection. Matching seeds and gameplay actions therefore reproduce enemy and passage
  choices. The current implementation retains this design through `RunRandom`; random-state
  persistence and restoration were added later by a teammate.

### Contributions to documentation

- **User Guide:** Updated [Seeds and repeatable runs](../UserGuide.md#seeds-and-repeatable-runs)
  with supported seed inputs, shared-sequence behaviour and the current continuation rules,
  including compatibility with older saves
  ([commit](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/f67bc73)).
- **Developer Guide:** Added
  [Combat model and encounter lifecycle](../DeveloperGuide.md#combat-model-and-encounter-lifecycle),
  explaining class responsibilities, skill definitions, enemy configurations, turn ordering,
  outcome access and stable-turn snapshots. Distinguished factory default HP from scenario HP
  and enemy base damage from the current player damage formula
  ([commit](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/f67bc73)).
- Added explanatory Javadoc across combat, encounters, typing challenges and UI components,
  documenting responsibilities, parameters and return values to support team integration
  ([commit](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/83beb80)).

### Testing and team-based tasks

- Added seven combat and encounter JUnit test classes covering entity state, player loadouts,
  enemy creation, registry lookup, damage application, HP clamping, victory, defeat and guarded
  outcome access. Used recording UI doubles to verify interactions without console input
  ([commit](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/a9421b3)).
- Updated gameplay tests for the additional seed-input step
  ([commit](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/eb9a5fc)) and configured JaCoCo
  HTML/XML coverage reports ([commit](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/6ba7751)).
