# Wildways - Codex Instructions

## Project

Wildways is a Fabric mod for Minecraft 26.2.

The mod should feel like vanilla Minecraft with a different rhythm: slower, warmer, more exploratory, less grindy, and less focused on rushing progression.

## Technical baseline

- Language: Java
- Loader: Fabric
- Minecraft version: 26.2
- Required Java release: 25
- Mod ID: `wildways`
- Main package: `com.viclev.wildways`

## Development rules

- Make small, testable changes.
- Prefer data-driven solutions first:
  1. JSON recipes, loot tables, tags, advancements
  2. Fabric API events
  3. Access wideners
  4. Mixins only when necessary
- Do not introduce large systems unless the design document calls for them.
- Keep client-only code in `src/client/java`.
- Keep shared gameplay code in `src/main/java`.
- After implementation, run `.\gradlew.bat build`.
- For gameplay changes, explain how to test them in-game.

## Design rules

- Changes should feel vanilla-adjacent, not like a large modpack.
- Avoid feature bloat.
- Encourage:
  - exploration
  - road and rail building
  - slower survival pacing
  - building and settling
  - use of underused vanilla items/features
- Avoid:
  - mandatory grind
  - overly complex tech trees
  - big UI systems
  - hard progression gates unless they feel natural

## Before editing

When asked to implement a feature:
1. Summarize the intended gameplay effect.
2. Identify the smallest technical approach.
3. Mention whether the change is data-driven, event-based, or mixin-based.
4. Then implement.s