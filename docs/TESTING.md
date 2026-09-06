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
- Locate a newly generated Stronghold and verify that all twelve End Portal Frames are empty. Confirm that a vanilla Eye of Ender cannot be inserted.
- Insert the twelve different WildWays eyes in any order. Confirm that a duplicate is rejected, an unenchanted Enchanted Eye is rejected, and the portal opens only after all twelve valid eyes are present.
- Left-click a filled frame and verify that the exact eye is returned. If the portal was open, verify that the portal blocks disappear and that reinserting the eye opens it again.
- Verify the acquisition paths: guaranteed Eye of Ice in an igloo, roughly 10% in snowy-village chests, roughly one-third Piglin Brute drops, 2/47 Lost Eye weight in common Trail Ruins suspicious gravel, and 5% per accepted Creaking hit with no more than one drop per Creaking.
- Drop an Eye of Ender in water inside an active Conduit's range, and near an idle Evoker. Verify conversion to Eye of Water and, after the Evoker's short warmup, Eye of Illagers.
- Leave several Eyes of Ender in separate Endermite Nest slots and verify that they transform at different random times, averaging about ten minutes over many trials.
- Craft the Storm, Darkness, and Enchanted Eye paths. Brew Eye of Ender + Nether Wart into Awkward Eye, Awkward Eye + Phantom Membrane into Thick Eye, and Thick Eye + Ghast Tear into Eye of Brewing. Enchant the Enchanted Eye at an Enchanting Table and verify that it receives Attunement and can then be inserted.
- For each WildWays crafting recipe, place all distinct ingredients in the inventory at the same time and verify that the recipe appears in the recipe book. Check that partial ingredient sets do not unlock it.

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
