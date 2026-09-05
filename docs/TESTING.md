# Testing

WildWays is a Fabric mod targeting Minecraft 26.2 and Java 25. Use the Gradle wrapper for all checks.

## Required Tools

- JDK 25
- Windows: `gradlew.bat`
- Fabric/Loom dependencies resolved through Gradle

Check the local Java version with:

```powershell
java -version
javac -version
```

## Build Check

Run from the project root:

```powershell
.\gradlew.bat build
```

This should compile the mod, process resources, validate mixins through Loom tasks, and produce jars under `build/libs/`.

## Client Smoke Test

For gameplay or client-visible changes, run:

```powershell
.\gradlew.bat runClient
```

Suggested smoke checks:

- Minecraft starts without crashing.
- The WildWays mod appears in the loaded mod list.
- A new single-player world can be created.
- The changed feature behaves as expected in survival-like conditions.
- Logs do not show mixin, registry, or missing-resource errors.

### Current Feature Checks

- Hold a compass and verify that the action bar shows `X`, `Y`, `Z`, and the current cardinal or intercardinal direction.
- Hold the clock, Biome Compass, and Light Sensor in turn and verify their action-bar information.
- Open a Fletching Table. Verify the title and feather placeholder, then place a feather, stick, and flint to receive five arrows. Confirm that no placeholder is shown for the stick, flint, or result slots. Test each special-arrow conversion recipe.
- Test Turtle, Range, and Explosive Arrows in a safe world to verify their increased damage, faster flight, and small explosion respectively.
- Open an Endermite Nest with contents and verify its inventory, comparator output, and portable contents. Verify Endermite Brick recipes and shapes.
- Brew Unease and Strong Unease, then test their Endermite-spawning behaviour while mining or around an affected creeper. Check representative Supreme and Fatal potion upgrades with Popped Chorus Fruit.
- Generate or locate a new Quarantine Grounds. Check that its Endermites remain present and that weathered spruce logs in the hospital houses become stripped spruce logs without changing their horizontal or vertical orientation.
- Place a Light Sensor below changing light levels and verify that its redstone output follows the light above it.

## Dedicated Server Check

For common-code or progression changes, run a server check when feasible:

```powershell
.\gradlew.bat runServer
```

Verify that no client-only classes are loaded on the server.

## Data Generation

The project enables Fabric data generation. When generated resources are introduced, run the appropriate Gradle datagen task and review generated files before committing them.

## Manual Balance Notes

Progression changes need playtesting, not only compilation. When testing a feature, record:

- The intended gameplay problem.
- The route a player is expected to take.
- Any boring or repetitive behavior the change may accidentally encourage.
- Whether the change affects multiplayer pacing.
