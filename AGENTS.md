# AGENTS.md

Guidance for Codex and other coding agents working on WildWays.

## Project Context

WildWays is a Fabric mod for Minecraft 26.2. Its goal is to make vanilla progression slower, less grind-driven, and more exploration-oriented while preserving a quiet vanilla feel.

Use the existing Fabric/Loom layout:

- Common code: `src/main/java`
- Common resources: `src/main/resources`
- Client-only code: `src/client/java`
- Client-only resources: `src/client/resources`
- Mod metadata: `src/main/resources/fabric.mod.json`

## Build Environment

- Use Java 25.
- Use the Gradle wrapper, not a globally installed Gradle.
- Preferred Windows build command: `.\gradlew.bat build`
- Preferred client run command: `.\gradlew.bat runClient`
- Keep Fabric, Loom, Loader, and Fabric API versions centralized in `gradle.properties`.

## Coding Rules

- Keep changes scoped to the requested feature or fix.
- Preserve vanilla-style behavior unless the task explicitly calls for a stronger gameplay change.
- Put shared gameplay logic in `src/main`; put rendering, keybinds, screens, and other client-only code in `src/client`.
- Do not introduce broad abstractions until there is repeated code or a clear API boundary.
- Avoid adding dependencies unless they are necessary and appropriate for a Fabric mod.
- Keep comments short and useful. Prefer clear names over explanatory comments.

## Minecraft/Fabric Rules

- Keep `fabric.mod.json` entrypoints aligned with actual package/class names.
- Keep mixin config package names aligned with Java package names.
- Treat mixins as high-risk changes: keep injection points narrow and document why the injection is needed.
- Prefer Fabric API hooks and events over mixins when they cleanly support the behavior.
- Be careful with server/client boundaries. Common code must not reference client-only Minecraft classes.

## Testing And Verification

- For code changes, run `.\gradlew.bat build` when feasible.
- For client-facing behavior, also run `.\gradlew.bat runClient` when feasible and manually check the affected flow.
- For data generation changes, run the configured datagen task before committing generated resources.
- If a check cannot be run, mention that clearly in the final response.

## Documentation

- Update `README.md` or files in `docs/` when a change affects project goals, build steps, testing, or player-visible behavior.
- Keep documentation practical and current. Avoid promising features that are not implemented unless they are clearly marked as planned.
