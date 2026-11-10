# Recipe-book discovery

Most crafting and cooking recipes use hidden advancements under `data/wildways/advancement/recipes`. Picking up one of the listed items discovers the recipe permanently. Discovery does not require the full crafting quantity, other ingredients, a workstation or fuel. Existing discoveries are retained when updating a world.

## Main materials

| Recipe | Inventory item that discovers it |
| --- | --- |
| WildWays Quest Book | Discovered on first joining the world, alongside the starter copy |
| Copper / Iron / Golden / Diamond Rune | Copper Ingot / Iron Ingot / Gold Ingot / Diamond respectively |
| Both Volcanite Rune variants | Refined Volcanite |
| Enchanting Catalyst | Sculk Catalyst |
| Quartz / Amethyst / Gold / Diamond / Volcanite Refinement | Quartz / Amethyst Shard / Gold Ingot / Diamond / refined Volcanite respectively |
| Eye of Enchanting | Amethyst Shard |
| Eye of Darkness | Echo Shard |
| Eye of Storm | Breeze Rod **or** Wind Charge |
| Eye of Volcanite | Refined Volcanite |
| Endermite Bricks | Endermite Shell |
| Endermite Nest, brick slabs, stairs and walls | Endermite Bricks; also discovers the three stonecutter shape recipes |
| Block of Raw Volcanite / unpacking it | Raw Volcanite / Block of Raw Volcanite respectively |
| Block of Volcanite / unpacking it | Refined Volcanite / Block of Volcanite respectively |
| Light Sensor | Daylight Detector |
| Beetroot Dough | Beetroot |
| Candied Bait | Spider Eye |
| Amethyst Blinker | Amethyst Shard |
| Each bait-loaded fishing rod | Its matching bait |
| Volcanite smelting and blasting | Raw Volcanite |

The Eye of the Endermite is transformed inside an Endermite Nest, rather than crafted or smelted. It has no recipe-book entry. Brewed and other transformed eyes likewise keep their existing acquisition paths.

## Enchanted-book conversions

Each conversion unlocks with **either** its specific source book **or** one secondary material below. Source-book detection accepts any positive stored enchantment level and additional enchantments. An unrelated enchanted book does not qualify. The recipe generator maintains these advancement predicates alongside the recipes.

| Result | Source book | Alternative secondary material |
| --- | --- | --- |
| Mending | Protection | Ghast Tear |
| Fire Protection | Protection | Blaze Powder |
| Blast Protection | Protection | Obsidian |
| Projectile Protection | Protection | Shield |
| Feather Falling | Protection | Phantom Membrane **or** Feather |
| Knockback | Sharpness | Wind Charge |
| Fire Aspect | Sharpness | Blaze Powder |
| Sweeping Edge | Sharpness | Iron Sword |
| Punch | Power | Wind Charge |
| Flame | Power | Blaze Powder |
| Riptide | Impaling | Nautilus Shell |

Flame consumes a Power book, four Lapis Lazuli, three Blaze Powder and one Experience Bottle. Its layout is `LBL / BPB / LEL`, where `L` is Lapis, `B` Blaze Powder, `P` the Power book and `E` the Experience Bottle.

## In-game checks

Use a disposable world with cheats, an empty inventory and the recipe-book craftable-only filter disabled. For repeat checks, revoke both the recipe's hidden advancement and its known recipe, then pick up or give yourself a fresh item. For example:

```mcfunction
/advancement revoke @s only wildways:recipes/misc/book_flame
/recipe take @s wildways:book_flame
/give @s minecraft:blaze_powder 1
```

1. For each row above, acquire exactly one matching main or secondary material without the remaining crafting ingredients. Verify the recipe is discovered. Test both Storm alternatives, both Feather Falling alternatives, and both Volcanite Rune variants.
2. Reset Flame discovery between attempts. A Power book alone must discover it; an Unbreaking book alone and Lapis alone must not. Blaze Powder alone must discover it. Repeat source-book and secondary-material checks for the other conversions.
3. Craft Flame using the stated layout. Replacing the bottom-center Experience Bottle with a fourth Blaze Powder must fail.
4. Confirm that one Endermite Brick discovers the Nest as well as all brick shapes. Endermite Shell alone discovers the base bricks, but not the Nest.
5. One Raw Volcanite must discover both cooking recipes. Smelt one in an otherwise empty furnace and blast one in an otherwise empty blast furnace: expect one Volcanite after 200 ticks (10 seconds) for 4 XP and 100 ticks (5 seconds) for 2 XP respectively. Take the outputs manually to release the stored XP.
6. Reload the world and verify that discovered recipes remain available after removing their ingredients. Existing saves keep previously discovered recipes; changed discovery conditions do not relock them.
