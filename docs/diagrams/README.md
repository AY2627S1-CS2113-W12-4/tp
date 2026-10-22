# Developer Guide diagrams

These figures accompany enhancements 8.1 and 8.2 in the [Developer Guide](../DeveloperGuide.md).

| Figure | Scope | Source | Display image |
| --- | --- | --- | --- |
| Typing evaluation | Static evaluation calls and immutable result return | [PlantUML](typing-evaluation.puml) | [SVG](typing-evaluation.svg) |
| Run progression | References among the state, encounter, participants and factory | [PlantUML](run-progression.puml) | [SVG](run-progression.svg) |
| Boss phase transition | Phase 1 victory creating and beginning phase 2 | [PlantUML](boss-phase-transition.puml) | [SVG](boss-phase-transition.svg) |

The SVGs use standard UML lifelines, activation boxes, dependencies, associations and multiplicities.
They are checked in so GitHub Pages does not need a diagram plugin. The initial SVG layout was authored
from the corresponding PlantUML descriptions and visually inspected; the build does not regenerate it.

To update a diagram, edit the `.puml` source and regenerate its SVG with PlantUML, or update the SVG
layout to match the source. When using a local PlantUML JAR, run `java -jar plantuml.jar -tsvg <file.puml>`.
Check that labels remain readable and the SVG agrees with the source and implementation. PlantUML is
optional and is not an application dependency. Avoid adding private utility details that obscure the
particular interaction each figure explains.
