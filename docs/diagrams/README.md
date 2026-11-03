# Developer Guide diagrams

These figures accompany the [Developer Guide](../DeveloperGuide.md). Each figure has a limited
scope: architecture shows responsibilities, class diagrams show types and references, the object
diagram shows an illustrative runtime snapshot, and sequence diagrams show selected interactions.

| Figure | Scope | Source | Display image |
| --- | --- | --- | --- |
| Player run (User Guide) | Five encounter choices followed by both mandatory boss phases | [PlantUML](game-flow.puml) | [SVG](game-flow.svg) |
| Architecture overview | Component responsibilities and dependencies, without claiming strict layers | [PlantUML](architecture-overview.puml) | [SVG](architecture-overview.svg) |
| Live terminal input | Background reader queue and main-thread command/update loop | [PlantUML](runtime-input.puml) | [SVG](runtime-input.svg) |
| Plain input | Background line queue, EOF/failures and safe delayed-event updates without automatic typing submission | [PlantUML](plain-input.puml) | [SVG](plain-input.svg) |
| State structure | State inheritance and the current-state reference | [PlantUML](state-structure.puml) | [SVG](state-structure.svg) |
| Active-run objects | Shared player, typing/event factories, reward pool and generator at a stable combat turn | [PlantUML](active-run-objects.puml) | [SVG](active-run-objects.svg) |
| Storage structure | Store inheritance and the retained high-score store reference | [PlantUML](storage-structure.puml) | [SVG](storage-structure.svg) |
| Saving an attempt | High-score initialization/update, history replacement, then combat or an event response | [PlantUML](history-saving.puml) | [SVG](history-saving.svg) |
| Typing evaluation | Class-level metric calls in code order and result construction | [PlantUML](typing-evaluation.puml) | [SVG](typing-evaluation.svg) |
| Combat entities and skills | Entity inheritance, shared skill references, registry lookups and reward-pool initialization | [PlantUML](combat-skills.puml) | [SVG](combat-skills.svg) |
| Encounter hierarchy | Combat/event inheritance and the puzzle, reaction and dialogue subclasses | [PlantUML](encounter-hierarchy.puml) | [SVG](encounter-hierarchy.svg) |
| Run progression | Pending offers versus a selected Encounter, combat/event subtypes and opponent factory dependency | [PlantUML](run-progression.puml) | [SVG](run-progression.svg) |
| Boss phase transition | Completed victory, nested helper calls and phase-2 creation | [PlantUML](boss-phase-transition.puml) | [SVG](boss-phase-transition.svg) |
| Event lifecycle | Actual EventPhase values, reaction waits, dialogue follow-ups and saveable stages | [PlantUML](event-lifecycle.puml) | [SVG](event-lifecycle.svg) |

The SVGs are generated with PlantUML 1.2026.9 using Java 25 and checked in so GitHub Pages does not
need a diagram plugin. Nonsequence figures select PlantUML's Smetana layout engine. The architecture
figure is a responsibility map; its arrows mean dependencies rather than chronological messages.

To update diagrams, edit the `.puml` sources and render them from the repository root:

```text
java -jar /path/to/plantuml.jar -charset UTF-8 -tsvg "docs/diagrams/*.puml"
```

After regenerating a static SVG, normalize its generated markup (Node.js 24):

```text
node .github/docs/normalize-svg.mjs docs/diagrams/boss-phase-transition.svg
```

Pass the SVG files you regenerated as separate arguments. This removes PlantUML `data-*` metadata,
empty titles/definitions and an unused XLink declaration, plus the default `lengthAdjust="spacing"`
attribute emitted on a group instead of a text element. These trigger strict SVG 1.1 inspections
in editors such as IntelliJ. The cleanup preserves coordinates, text, styles and embedded PlantUML
source. It refuses script- or stylesheet-based figures because those may depend on the metadata.

Use the installed JAR's actual path. PlantUML is a documentation tool, not an application dependency;
Gradle does not regenerate these figures. Check the SVGs visually before rebuilding the DG PDF.
Do not hand-position activation bars in the SVG: regeneration must preserve the source's call and
return nesting. Use `<<class>>` for class-level sequence participants, target-end field names for
association roles, and notes for inline calculations or assignments. The event state diagram uses
phase transitions rather than chronological object messages. Omitted calls and parameters
must be outside the stated scope; do not invent methods to summarize several operations.
