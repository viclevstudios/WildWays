# Testing

WildWays is a Fabric mod targeting Minecraft 26.3 and Java 25. Use the Gradle wrapper for all checks.

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

## Server Integration Tests

Run `.\gradlew.bat runGameTest` for the Fabric test server. This uses a separate test mod and run directory; the test classes are not included in the distributable mod JAR. Tests cover real enchanting/anvil transactions and librarian offers. Compilation alone does not validate gameplay behavior or mixin invocation targets.

## Structure Maps

Use a default Overworld with structures enabled. Vanilla abandoned camps generate in new terrain. Previously unopened containers can use the updated loot tables after upgrading a world.

| Destination | In-game check |
| --- | --- |
| Mineshaft | Open a spawner-dungeon chest (25% when a target is found) or the secret chest in a vanilla abandoned camp (vanilla loot). |
| Spawner dungeon | Load chunks around a dungeon, then check unopened abandoned camp chests or mineshaft chest minecarts in the same area. Each has a 25% roll and needs a known dungeon within 1,024 blocks. Check the marker against the spawner's X and Z coordinates. |
| Trail Ruins | Open an anchor chest in Quarantine Grounds. It should give a map when the search finds Trail Ruins. |
| Igloo | Open unopened snowy village house chests (25% when a target is found). |
| Pillager Outpost | Kill the banner captain of a newly spawned patrol with an outpost in the area. Compare it with an ordinary patrol member and a captain spawned at an outpost. |
| Jungle Pyramid and Swamp Hut | Fish in open water in a jungle biome or a regular swamp and complete the minigame. Approximately one in seven treasure catches in each eligible biome becomes its map when the search succeeds. Check with and without bait. |
| Desert Pyramid | Brush suspicious sand at a desert well near a pyramid. A successful search replaces 20% of ordinary finds. |
| Ancient City | Open Trial Chambers supply chests (25% when a target is found). |

Hold each map to see its marker, then clone it with an empty map in a crafting grid. For a repeatable loot check near a target, run `/loot give @s loot minecraft:chests/trial_chambers/supply` or substitute another chest's loot-table ID.

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

- Cast a fishing rod into water and reel in when the bobber dips. Follow the target and watch the chance rise. A round spent in green reaches 100%, yellow reaches 50%, and outside stays at 0%. Try switching zones mid-round and moving to green just before time runs out. Confirm the screen closes after five seconds and that Escape or either Shift key lets the catch go. Compare several casts with vanilla to check the shorter wait and longer reaction window. Check that successful catches still produce loot, XP and rod wear.
- Craft each bait and combine it with an enchanted, damaged fishing rod. Check the different rod sprites before and after casting. Reel in early and check that the tooltip still reads 16 charges. Wait for a bite and check that it reads 15, including after letting the bite expire or cancelling the round. Confirm that the three-second Candied Bait round uses the wider zones and moves at the same speed as a normal round. Catch fish with Beetroot Dough and compare their sprites and restored hunger with ordinary fish before and after cooking. Check that pufferfish from that bait are ordinary. Compare Amethyst Blinker catches in open water and a confined pond.
- Restart the client for the new Volcanite trim registry entry and ingot component. At a Smithing Table, place an armor-trim template on the left, armor in the middle and one `wildways:volcanite` ingot on the right. Confirm that the preview, result tooltip and inventory icon show Volcanite Material, and that the worn armor uses the selected pattern. Compare Iron, Diamond, Netherite, Quartz, Amethyst, colored Leather armor and Turtle Shells; compare different patterns including Sentry, Silence, Flow and Bolt.
- Compare Redstone and Volcanite trims side by side. Volcanite should have lighter coral/rose highlights. On Volcanite armor, verify the automatically selected red/rose contrast variant in both the inventory and worn layers (including leggings). Existing trims must retain their original colors, and names, enchantments, damage and leather dye must survive trimming. Reapplying an identical material/pattern must give no result. Automated `VolcaniteTrimGameTests` cover all eighteen vanilla patterns, all 41 armor items and the palette ID.

- Reload resources with F3+T. In Ingredients, verify that the five refinement templates have their custom sprites. At a Smithing Table, apply trims to Quartz, Amethyst and Volcanite helmets, chestplates, leggings and boots. Verify that the preview/result and inventory icons show the trim material color, untrimmed copies retain their base sprites, and worn armor retains the selected pattern. Try each of the eleven vanilla trim materials, including Resin; changing the pattern with the same material should keep the same inventory overlay, as in vanilla.

- Reopen the creative inventory after restarting the client. Both equipment tabs must group by material. Tools use shovel/pickaxe/axe/hoe order; Combat must show each material's sword, spear, axe and complete helmet/chestplate/leggings/boots set together, including vanilla equipment. Quartz and Amethyst follow Iron; Volcanite follows Diamond. Refinement templates appear in Ingredients beside the Netherite Upgrade template.
- Hover each refinement and check that it names the correct input tier and says that the refinement is consumed without extra materials. Insert it into a Smithing Table: the middle-slot hint should name the compatible equipment, and the right slot should have no ghost material icon and instruct the player to leave it empty.
- Search for gold tools: only the vanilla Golden Sword/Pickaxe/Axe/Shovel/Hoe should appear, with 400 durability and the updated combat/mining stats. Refine an Iron tool with Gold Refinement and verify that its advanced tooltip shows a `minecraft:golden_*` ID; then refine it into Diamond or Volcanite. Test a gold tool obtained from loot or a trade as well. No separate `wildways:gold_*` tool items should be registered.

- Open the trade screen of an expert or master armorer, toolsmith and weaponsmith. Check that their equipment offers sell the Amethyst equivalents and display enchantments. The toolsmith's journeyman Amethyst Hoe is unenchanted and has a base price of 3 emeralds. On an older save, check that a previously unlocked diamond offer keeps its enchantments and stock and uses 75% of its previous emerald base price, rounded to the nearest emerald. Master toolsmiths and weaponsmiths should have both their enchanted equipment and their book.
- Give yourself `/give @s wildways:quartz_spear`, `/give @s wildways:amethyst_spear`, `/give @s wildways:volcanite_spear` and `/give @s minecraft:golden_spear`. Compare their tooltips with Iron and Diamond Spears: Quartz has 700 durability, Amethyst attacks about 0.2 times per second faster than iron, Gold has 400 durability and stats between iron and diamond, and Volcanite has 1,400 durability.
- In survival, compare jabs and held charged attacks on a stationary target. Volcanite reaches 4.75 blocks instead of 4.5, with the same two-block minimum range; Amethyst's jab recovers sooner. Compare range from the same position and avoid moving targets when checking the reach boundary. Check both custom inventory icons and held spear textures/animations for Quartz, Amethyst and Volcanite.
- At a Smithing Table, refine Iron Spears into Quartz, Amethyst and Gold, then try each of those with Diamond and Volcanite Refinements. Upgrade both upper tiers to Netherite with a Netherite Upgrade template and ingot. Confirm that enchantments and names survive, and that upgraded Amethyst spears retain their speed bonus once. Original direct Gold/Diamond spear crafting recipes are replaced by this progression.
- Apply Lunge, Sharpness, Unbreaking and Mending at the enchanting table. Damage each spear and repair it with its matching material in an anvil; Netherite Spears should also accept a Netherite Scrap when enchanted with Mending. Automated `SpearGameTests` cover components, enchantment tags, the complete smithing routes and Amethyst heritage.

- Follow the [Volcanite prototype checks](VOLCANITE.md) for basalt-only, vertically oriented ore generation (including visible air-exposed ore in fresh Basalt Deltas), Basalt-style placement along all three axes, mining tiers, Fortune/Silk Touch, smelting, diamond-equivalent equipment, Volcanite repairs, Netherite upgrades, and 5%/15% Bastion loot chances.
- Follow the [Enchanting prototype checks](ENCHANTING.md) for the seven rune/catalyst recipes, reusable books, the universal catalyst, shelves, Attunement, Mending, anvil costs and librarian trades. Rune 5 must use refined Volcanite, not copper stairs or Raw Volcanite. Follow the [book-source checks](BOOK_SOURCES.md) for the new exploration, mob, trade and crafting sources.
- Close and reopen an Enchanting Table and an anvil with inputs inside, reload the world, then break each workstation. Check that the inputs persist, render outside the block and drop exactly once when it is broken.

- Hold a compass and verify that the action bar shows `X`, `Y`, `Z`, and the current cardinal or intercardinal direction.
- Hold the clock and Light Sensor in turn and verify their action-bar information.
- Open a Wandering Trader's offers (use `/summon minecraft:wandering_trader` for a quick check). Buy a Biome Compass for 20 emeralds and confirm the offer is sold out, including after reopening the screen. Check a journeyman Librarian, including one from an older save. It should buy ink sacs, and any Biome Compass offer should be gone. Check that the old map-and-iron recipe is absent. Hold the compass and right-click to select a biome, then confirm. Turn around and check that the needle follows the target and the action bar shows both biomes. Verify the target survives a relog and cannot be changed. Try an old stack of blank compasses to confirm only one is bound.
- Compare the blank and bound Biome Compass in the hotbar and in hand. The blank compass uses the standard Biome Compass texture. The bound compass uses the custom sprites with a red needle. Turn through a full circle away from the target and check that the needle tracks its direction in the correct order.
- Find several villages in newly generated chunks. Some should have one trader near the town center when first generated. Revisit a village after the trader leaves and check that the village itself does not produce a replacement.
- Open a Fletching Table. Verify the title and feather placeholder, then place a feather, stick, and flint to receive five arrows. Confirm that no placeholder is shown for the stick, flint, or result slots. Test each special-arrow conversion recipe.
- Test Turtle, Range, and Explosive Arrows in a safe world to verify their increased damage, faster flight, and small explosion respectively.
- Open an Endermite Nest with contents and verify its inventory, comparator output, and portable contents. Verify Endermite Brick recipes and shapes.
- Brew Unease and Strong Unease, then test their Endermite-spawning behaviour while mining or around an affected creeper. Check representative Supreme and Fatal potion upgrades with Popped Chorus Fruit.
- Generate or locate a new Quarantine Grounds. Check that its Endermites remain present, that its Endermite Nest starts with one or two common enchanted books, and that weathered spruce logs in the hospital houses become stripped spruce logs without changing their horizontal or vertical orientation.
- Place a Light Sensor below changing light levels and verify that its redstone output follows the light above it.
- Locate a newly generated Stronghold and verify that all twelve End Portal Frames are empty. Confirm that a vanilla Eye of Ender cannot be inserted.
- Insert twelve different WildWays eyes in any order, leaving out any two types. Confirm that a duplicate and an unenchanted Eye of Enchanting are rejected, that the portal remains closed at eleven eyes, and that the twelfth opens it. Try including both new eyes and leaving out the Eye of the Tiger and Eye of Enchanting.
- Left-click a filled frame and verify that the exact eye is returned. If the portal was open, verify that the portal blocks disappear and that reinserting the eye opens it again.
- Verify the acquisition paths: guaranteed Eye of Ice in an igloo, roughly 10% in snowy-village chests, roughly one-third Piglin Brute drops, 2/47 Lost Eye weight in common Trail Ruins suspicious gravel, and 5% per accepted Creaking hit with no more than one drop per Creaking.
- Drop an Eye of Ender in water within range of an active Conduit. Check for converging runes and the Conduit chime as the eye changes. Bubbles should continue rising from the spot for about one and a half seconds, even after pickup.
- Drop an Eye of Ender near an idle Evoker. Watch for purple wisps during the warmup and a trail of magic from the Evoker to the eye as the spell completes.
- Drop an Eye of Ender into water directly above Potent Sulfur. Look for sulfur bubbles and a geyser at the water surface shortly after the eye changes. Repeat in ordinary water as a control.
- Craft an Eye of Volcanite by placing four Volcanite Ingots on the sides of a central Eye of Ender. Drop it into fire with two blocks of clearance. Watch for two flame spirals around the eye as it rises, then a flare, smoke, and a firecharge sound as it becomes attuned. Repeat with soul fire for blue flames. Verify that only the attuned form can be inserted.
- Leave several Eyes of Ender in separate Endermite Nest slots and verify that they transform at different random times, averaging about ten minutes over many trials.
- Craft the Storm, Darkness, and Eye of Enchanting paths. Brew Eye of Ender + Nether Wart into Awkward Eye, Awkward Eye + Phantom Membrane into Thick Eye, and Thick Eye + Ghast Tear into Eye of Brewing. Enchant the Eye of Enchanting at an Enchanting Table and verify that it receives Attunement and can then be inserted.
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
