# Volcanite prototype

Design sources: [Volcanite](https://app.notion.com/p/3e9cef41da7280209304c93977b14c25) and [Tool Progression](https://app.notion.com/p/3decef41da7280babd21e042ffcd5281).

Notion specifies a diamond-equivalent material found in Basalt Deltas and rarely in Bastion chests, with tools and armor as an alternative to diamond. The prototype follows the user's clarification that its ore replaces only regular basalt and drops Raw Volcanite, which is smelted in a furnace or blast furnace before crafting equipment.

## Implemented behavior

- `wildways:volcanite_ore` generates only in Basalt Deltas, replacing `minecraft:basalt`. Blackstone, smooth basalt, and netherrack are not targets. Ore is allowed when exposed to air. Existing chunks are not changed.
- Mining requires an iron-tier or better pickaxe. The normal drop is one `wildways:raw_volcanite`; Fortune increases the drop and Silk Touch returns the ore. The raw ore gives no mining XP.
- Raw Volcanite smelts into one `wildways:volcanite` in 200 ticks in a furnace or 100 ticks in a blast furnace, giving 1 XP.
- Sword, pickaxe, axe, shovel, hoe, helmet, chestplate, leggings, and boots use the corresponding diamond equipment's values, including durability and enchantability. Their material repair uses refined Volcanite and now requires Mending under the [Enchanting prototype](ENCHANTING.md) rules; diamonds and Raw Volcanite do not repair them.
- Equipment uses the normal diamond recipe shapes with Volcanite in place of diamonds. Recipes unlock when all distinct ingredients are held at the same time.
- Each equipment piece can be upgraded to its vanilla Netherite counterpart with a Netherite Upgrade Smithing Template and Netherite Ingot. Vanilla smithing keeps names, enchantments, and other transferable components.
- All four vanilla Bastion chest tables have an additional rare pool for refined Volcanite. Vanilla loot is retained; custom loot-table replacements are respected.
- Items use the usual vanilla equipment and armor-slot tags for enchantment and armor-trim compatibility.

## Provisional balance and visuals

These are prototype choices, not values specified in Notion:

- Sixteen vein attempts per chunk, vein size six, uniformly between Y=32 and Y=112, with no discard for ore exposed to air. This responds to the playtest finding that Volcanite was rarer to find than Netherite. Actual yields still depend on basalt coverage in the chunk.
- A 10% chance per Bastion chest for 1-2 refined Volcanite.
- Volcanite has a first-pass 16×16 texture set for the ore, raw item, refined ingot, equipment icons, and worn armor. The ore uses separate side and top textures. The equipment sprites reuse vanilla diamond silhouettes with a slate-and-ember palette; the ore and material items are drawn pixel by pixel. The texture files were generated with `tools/generate_volcanite_textures.py` and are not AI-generated. A larger [preview sheet](volcanite-texture-preview.png) shows the inventory sprites.
- No fire immunity or special abilities; this is a diamond-level alternative.

## In-game test

1. Run `.\gradlew.bat runClient` and use a disposable world with cheats.
2. Give yourself `/give @s wildways:volcanite_ore 16` and `/give @s wildways:raw_volcanite 64`. Place ore and switch to survival. Compare wooden/stone/copper pickaxes (no drop) with iron/diamond/Volcanite pickaxes (Raw Volcanite). Check Fortune and Silk Touch separately.
3. Smelt Raw Volcanite in both furnace types; confirm the blast furnace takes half the time and both give one refined material per raw item.
4. Hold refined Volcanite and sticks together. Craft the five tools and four armor pieces using vanilla shapes. Compare durability, attack attributes, mining speed, obsidian drops, armor, and toughness with diamond equipment.
5. Damage a tool and armor piece enchanted with Mending. Repair them with Volcanite at an anvil; Raw Volcanite and diamonds must not work. Each material repairs 33% of maximum durability, rounded up, and costs XP equal to the highest enchantment level. Without Mending, material repair must fail. Check enchanting, armor trims, shovel paths, axe stripping, and hoe tilling.
6. Upgrade a named and enchanted Volcanite item in a smithing table; confirm the Netherite result retains the name and enchantments.
7. Use `/locate biome minecraft:basalt_deltas` in the Nether, then explore freshly generated chunks. Inspect basalt at Y=32-112 for ore, including exposed cliff faces and cave walls. Verify that ore does not replace blackstone and does not generate in other Nether biomes. Compare several fresh chunks with the previous build; visible ore should now occur more often.
8. To inspect the configured replacement rule independently of natural generation, fill a safe test volume with basalt and run `/place feature wildways:ore_volcanite` inside it. Repeat with blackstone: no Volcanite ore should be placed.
9. Sample `/loot give @s loot minecraft:chests/bastion_bridge` and the `bastion_hoglin_stable`, `bastion_other`, and `bastion_treasure` tables many times. Volcanite should occasionally appear alongside vanilla loot; a single empty result is expected.

Build and startup checks cannot establish mining feel, visual quality, or long-term balance. Those require the client-world checks above.

## Verification performed

- `.\gradlew.bat build`: successful on Java 25, including shared and client compilation and JAR packaging.
- `.\gradlew.bat runDatagen`: successful; mod initialization and the existing chest-loot provider ran. This does not exercise the new crafting recipes or ore placement in a world.
- All 207 resource JSON files parsed successfully.
- `.\gradlew.bat runServer`: mod bootstrap completed, then startup stopped at the existing `eula=false` setting. No server world or datapack reload was tested.
- Client-world mining, crafting, armor rendering, and generation checks remain manual.
