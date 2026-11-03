# Koh Yong De - Project Portfolio Page

## Overview

Keyboard Warrior is a Java terminal game that turns typing practice into combat. Players choose
skills and type timed passages, with speed and accuracy determining attack damage.

## Summary of Contributions

**Code contributed:** [tP Code Dashboard - Yong-De](https://nus-cs2113-ay2627-s1.github.io/tp-dashboard/?search=Yong-De&sort=groupTitle&sortWithin=title&timeframe=commit&groupSelect=groupByAuthors&breakdown=true&checkedFileTypes=docs~functional-code~test-code~other).

### Enhancements implemented

- **Combat model and encounter foundation**
  ([initial structure](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/d1e4d3c),
  [combat functionality](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/36f42ee)):
  Implemented the initial `Entity`, `Player`, `Enemy` and skill components, together with
  `Encounter`, `CombatEncounter` and `EncounterOutcome`. Connected player skill selection and
  typing results to damage, enemy attack rotation and encounter outcomes. Added `CombatUi` and a
  sample combat entry point. Teammates later extended this foundation with nonblocking turns,
  revised damage, difficulty modifiers, boss progression and save snapshots.

- **Shared randomness for enemies and passages**
  ([commit](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/93398d7)):
  Passed the run's random source into `PromptBank` and `TypingChallengeFactory`, sharing it with
  enemy selection so matching seeds and actions reproduce enemies and passages. The current
  `RunRandom` retains this design; a teammate later added random-state persistence.

- **Puzzle and reaction encounter framework**
  ([reaction encounters](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/902a311),
  [encounter choices](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/73d3c72)):
  Implemented `EventEncounter`, `PuzzleEncounter`, `ReactionEncounter`, puzzle/reaction banks
  and `EventUi`. Added seeded combat/event choices through `EncounterFactory`. Separated
  preparation, periodic updates and answer submission to support delayed prompts without blocking,
  with lifecycle guards against early submissions and repeated effects.

- **Skill rewards and performance-scaled time bonuses**
  ([reward pool](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/c845f84),
  [refinements](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/54ea6fd)):
  Added a shared `RewardPool` that excludes already-owned skills and draws rewards without
  replacement in reproducible order. Successful puzzles grant a skill or fallback healing. Added
  persistent player time adjustments and Overclock, whose reward scales with WPM, accuracy and
  difficulty, while protecting health and countdown arithmetic against overflow.

- **Branching dialogue encounters**
  ([implementation](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/18e61bd),
  [robustness and tests](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/3518875)):
  Implemented a two-stage merchant conversation with trade and escape branches, shared skill
  rewards, fallback healing, time penalties and guard damage. Made challenge-creation failures
  retryable and prevented duplicate effects. Added accurate reward/healing feedback and tests.
  This work is on `YongDe-DialogueEncounter` and is not yet merged into `master`.

### Contributions to team-based tasks

- **Testing and verification:** Added seven combat/encounter test classes covering entities,
  loadouts, factories, damage, HP clamping, outcomes and lifecycle guards. Used recording UI doubles
  ([commit](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/a9421b3)).
- Updated gameplay tests for seed input and configured JaCoCo HTML/XML reports
  ([seed tests](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/eb9a5fc),
  [JaCoCo](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/6ba7751)).
- Added event regressions for lifecycles, deterministic randomness, reward exhaustion, time bonuses,
  dialogue branches, failed challenge creation and exactly-once effects
  ([events](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/54ea6fd),
  [dialogue](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/3518875)).

### Contributions to documentation

- **User Guide:** Updated [Seeds and repeatable runs](../UserGuide.md#seeds-and-repeatable-runs)
  with seed inputs, shared-sequence behaviour and legacy-save continuation rules
  ([commit](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/f67bc73)).
- **Developer Guide:** Added [Combat model and encounter lifecycle](../DeveloperGuide.md#combat-model-and-encounter-lifecycle)
  and its combat/entity and encounter-hierarchy diagrams. Documented turn ordering, damage,
  event lifecycle and reward-pool contracts, and added Javadoc across combat/event components
  ([combat docs](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/f67bc73),
  [Javadoc](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/83beb80),
  [events](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/54ea6fd)).
