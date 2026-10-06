# Volcanite prototype

Design sources: [Volcanite](https://app.notion.com/p/3e9cef41da7280209304c93977b14c25) and [Tool Progression](https://app.notion.com/p/3decef41da7280babd21e042ffcd5281).

Notion specifies a diamond-equivalent material found in Basalt Deltas and rarely in Bastion chests, with tools and armor as an alternative to diamond. The prototype follows the user's clarification that its ore replaces only regular basalt and drops Raw Volcanite, which is smelted in a furnace or blast furnace before crafting equipment.

## Implemented behavior

- `wildways:volcanite_ore` generates only in Basalt Deltas, replacing `minecraft:basalt`. Blackstone, smooth basalt, and netherrack are not targets. Ore is allowed when exposed to air. Generated ore has the vertical `axis=y` orientation; when placed by a player, it follows the clicked axis like Basalt. Existing chunks are not changed.
- Mining requires an iron-tier or better pickaxe. The normal drop is one `wildways:raw_volcanite`; Fortune increases the drop and Silk Touch returns the ore. The raw ore gives no mining XP.
- Raw Volcanite smelts into one `wildways:volcanite` in 200 ticks in a furnace or 100 ticks in a blast furnace, giving 1 XP.
- Nine Raw Volcanite craft into `wildways:raw_volcanite_block`, and nine refined Volcanite craft into `wildways:volcanite_block`. Each block unpacks into its nine ingredients. Both require an iron-tier pickaxe and drop themselves.
- Sword, pickaxe, axe, shovel, hoe, helmet, chestplate, leggings, and boots use the corresponding diamond equipment's values, including durability and enchantability. Their material repair uses refined Volcanite and now requires Mending under the [Enchanting prototype](ENCHANTING.md) rules; diamonds and Raw Volcanite do not repair them.
- Equipment uses the normal diamond recipe shapes with Volcanite in place of diamonds. Recipes unlock when all distinct ingredients are held at the same time.
- Each equipment piece can be upgraded to its vanilla Netherite counterpart with a Netherite Upgrade Smithing Template and Netherite Ingot. Vanilla smithing keeps names, enchantments, and other transferable components.
- All four vanilla Bastion chest tables have an additional pool for 1-2 refined Volcanite: 5% per Bridge, Hoglin Stable, or Other chest and 15% per Treasure chest. Vanilla loot is retained; custom loot-table replacements are respected.
- Items use the usual vanilla equipment and armor-slot tags for enchantment and armor-trim compatibility.

## Provisional balance and visuals

Ore frequency remains a prototype choice; the Bastion rates follow the current user specification:

- Fourteen vein attempts per chunk, vein size six, uniformly between Y=8 and Y=112, with no discard for ore exposed to air. The lower limit matches Ancient Debris and allows stripmining through the lower basalt layers. The attempt count is slightly lower than the previous sixteen; actual yields still depend on basalt coverage in the chunk.
- A 5% chance per Bridge, Hoglin Stable, or Other Bastion chest and a 15% chance per Treasure Bastion chest for 1-2 refined Volcanite.
- The ore uses the current Volcanite texture on its four sides and vanilla `minecraft:block/basalt_top` on its two axis ends. The two storage blocks use the latest user-provided 16×16 textures on all six faces. Raw Volcanite, refined ingots, equipment icons, and worn armor use the current textures. The earlier Raw Volcanite sprite was derived from an AI-edited user reference; its replacement was supplied by the user. The [preview sheet](volcanite-texture-preview.png) and `tools/generate_volcanite_textures.py` describe older first-pass assets.
- No fire immunity or special abilities; this is a diamond-level alternative.

## In-game test

1. Run `.\gradlew.bat runClient` and use a disposable world with cheats.
2. Give yourself `/give @s wildways:volcanite_ore 16` and `/give @s wildways:raw_volcanite 64`. Place ore against the top and side of adjacent blocks: its Basalt-textured ends should follow the clicked axis. Then switch to survival. Compare wooden/stone/copper pickaxes (no drop) with iron/diamond/Volcanite pickaxes (Raw Volcanite). Check Fortune and Silk Touch separately.
3. Smelt Raw Volcanite in both furnace types; confirm the blast furnace takes half the time and both give one refined material per raw item.
4. Hold refined Volcanite and sticks together. Craft the five tools and four armor pieces using vanilla shapes. Compare durability, attack attributes, mining speed, obsidian drops, armor, and toughness with diamond equipment.
5. Damage a tool and armor piece enchanted with Mending. Repair them with Volcanite at an anvil; Raw Volcanite and diamonds must not work. Each material repairs 33% of maximum durability, rounded up, and costs XP equal to the highest enchantment level. Without Mending, material repair must fail. Check enchanting, armor trims, shovel paths, axe stripping, and hoe tilling.
6. Upgrade a named and enchanted Volcanite item in a smithing table; confirm the Netherite result retains the name and enchantments.
7. Use `/locate biome minecraft:basalt_deltas` in the Nether, then explore freshly generated chunks. Inspect basalt at Y=8-112 for ore, including lower-level stripmines, exposed cliff faces and cave walls. Check the targeted block state in F3: naturally generated ore should show `axis=y`. Verify that ore does not replace blackstone and does not generate in other Nether biomes.
8. To inspect the configured replacement rule independently of natural generation, fill a safe test volume with basalt and run `/place feature wildways:ore_volcanite` inside it. Generated ore should show `axis=y`. Repeat with blackstone: no Volcanite ore should be placed.
9. Sample `/loot give @s loot minecraft:chests/bastion_bridge` and the `bastion_hoglin_stable`, `bastion_other`, and `bastion_treasure` tables many times. Over a large sample, the first three should yield 1-2 Volcanite in about 5% of chests and Treasure in about 15%. A small sample can differ substantially.
10. Craft each storage block from nine matching materials, then unpack it back into nine. Place and mine both blocks with an iron pickaxe; each should drop itself. Check their inventory icons and world textures, as well as the updated ore icon and placed ore.

Build and startup checks cannot establish mining feel, visual quality, or long-term balance. Those require the client-world checks above.

## Verification performed

- `.\gradlew.bat build`: successful on Java 25, including shared and client compilation, JAR packaging, and all 13 GameTests. The Volcanite test checks iron-tier mining tags and both 9:1/1:9 storage recipes. The three ore-axis blockstate variants and the explicit `axis=y` worldgen state are defined in resources; visual placement still needs the client-world check above.
- `.\gradlew.bat runDatagen`: successful; mod initialization and the existing chest-loot provider ran. This does not exercise the new crafting recipes or ore placement in a world.
- The test server loaded 1630 recipes and 1718 advancements, including the four storage recipes and their unlocks. The new block, item-model, and loot-table JSON files parsed successfully.
- `.\gradlew.bat runServer`: mod bootstrap completed, then startup stopped at the existing `eula=false` setting. No server world or datapack reload was tested.
- Client-world mining, crafting, armor rendering, and generation checks remain manual.
