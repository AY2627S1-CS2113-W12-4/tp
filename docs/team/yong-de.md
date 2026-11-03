# Koh Yong De - Project Portfolio Page

## Overview

Keyboard Warrior is a Java terminal game that turns typing practice into combat. Players choose skills and type passages against a countdown, with typing speed and accuracy determining attack damage.

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

- **Shared randomness for enemies and passages**
  ([commit](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/93398d7)):
  Passed the run's random source into `PromptBank` and `TypingChallengeFactory`, sharing it with
  enemy selection. Matching seeds and gameplay actions therefore reproduce enemy and passage
  choices. The current implementation retains this design through `RunRandom`; random-state
  persistence and restoration were added later by a teammate.

- **Puzzle and reaction encounter framework**
  ([reaction encounters](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/902a311),
  [encounter choices](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/73d3c72)):
  Implemented `EventEncounter`, `PuzzleEncounter`, `ReactionEncounter`, puzzle/reaction banks
  and `EventUi`. Added an encounter factory offering combat and event choices using the run's
  shared random source. Separated preparation, periodic updates and answer submission so events
  can support delayed prompts without blocking input. Added lifecycle guards to prevent early
  submissions and repeated rewards or penalties.

- **Skill rewards and performance-scaled time bonuses**
  ([reward pool](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/c845f84),
  [refinements](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/54ea6fd)):
  Added a shared `RewardPool` that excludes already-owned skills and draws rewards without
  replacement, with stable ordering for reproducible seeded draws. Successful puzzles grant a
  skill or fallback healing. Added persistent in-memory player time adjustments and Overclock,
  whose time reward scales with WPM, accuracy and difficulty using a 50-WPM baseline.
  Protected health and countdown arithmetic against integer overflow.

- **Branching dialogue encounters**
  ([implementation](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/18e61bd),
  [robustness and tests](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/3518875)):
  Implemented a two-stage merchant conversation with trade and escape branches, shared skill
  rewards, fallback healing, time penalties and guard damage. Made failed challenge creation
  retryable without advancing the conversation, and prevented duplicate terminal effects.
  Updated health-clamping assertions and UI feedback to report awarded skills and actual healing.
  This work is on `YongDe-DialogueEncounter` and is not yet merged into `master`.

### Contributions to team-based tasks

- **Testing and verification:** Added seven combat and encounter JUnit test classes covering entity state, player loadouts,
  enemy creation, registry lookup, damage application, HP clamping, victory, defeat and guarded
  outcome access. Used recording UI doubles to verify interactions without console input
  ([commit](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/a9421b3)).
- Updated gameplay tests for the additional seed-input step
  ([commit](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/eb9a5fc)) and configured JaCoCo
  HTML/XML coverage reports ([commit](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/6ba7751)).
- Added regression tests for puzzle/reaction lifecycles, reward exhaustion, deterministic random
  consumption, scaled time bonuses, lethal damage and integer boundaries
  ([commit](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/54ea6fd)).
  Extended coverage on the dialogue branch to both conversation paths, challenge-generation
  failures, repeated submissions, shared factory rewards and skill-name feedback. Used failing
  factory doubles and recording UI doubles to verify state transitions and exactly-once effects
  ([commit](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/3518875)).

### Contributions to documentation

- **User Guide:** Updated [Seeds and repeatable runs](../UserGuide.md#seeds-and-repeatable-runs)
  with supported seed inputs, shared-sequence behaviour, and continuation rules for older saves
  ([commit](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/f67bc73)).
- **Developer Guide:** Added [Combat model and encounter lifecycle](../DeveloperGuide.md#combat-model-and-encounter-lifecycle)
  explaining class responsibilities, turn ordering, and damage scaling
  ([commit](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/f67bc73)). Authored the `combat-skills.svg`
  and `encounter-hierarchy.svg` class diagrams to visualize entity inheritance and event polymorphism.
  Documented event lifecycle contracts, reward-pool logic, and added Javadoc across combat/event components to
  support team integration ([1](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/83beb80),
  [2](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/54ea6fd), [3](https://github.com/AY2627S1-CS2113-W12-4/tp/commit/3518875)).
