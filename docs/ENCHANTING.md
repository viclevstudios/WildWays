# Enchanting prototype

Source: [Enchanting design in Notion](https://app.notion.com/p/3ddcef41da72800abdb1ed9f5a201a53), the user's supplied recipe images, and the user's Mending, Attunement and universal-catalyst clarifications. The planned book acquisition paths are documented in [Book sources](BOOK_SOURCES.md).

## Implemented rules

The vanilla Enchanting Table opens a recipe menu. Insert an equipment item, one Lapis Lazuli, an enchanted recipe book and a rune. The book remains in the menu; taking the result consumes one lapis and one rune. Existing enchantments remain, compatible enchantments can be added or upgraded, and incompatible combinations or downgrades are rejected. Books with several enchantments allow selecting one recipe with the arrow buttons.

| Rune | Applied enchantment level | Required usable bookshelves | XP levels | XP with catalyst |
| --- | --- | --- | --- | --- |
| Copper Rune | 1 | 0 | 1 | 1 |
| Iron Rune | 2 | 5 | 2 | 1 |
| Golden Rune | 3 | 10 | 3 | 2 |
| Diamond Rune | 4 | 15 | 4 | 2 |
| Volcanite Rune | 5 | 20 | 5 | 3 |

The enchantment's normal maximum level still applies. A higher rune may be used, but the applied level, bookshelf requirement and XP price stop at the enchantment's maximum. Mending, Silk Touch, Channeling and Multishot remain level one but require at least a Golden Rune, ten usable bookshelves and three XP levels. The same vanilla bookshelf distance applies; non-full blocks such as torches and carpets in the gap do not block shelves. Shelves are checked again before taking a result. Only the displayed cost is required, with no additional minimum XP threshold.

There is one universal catalyst, `wildways:enchanting_catalyst`. It works with all rune tiers and compatible enchantment recipes, halves the XP cost rounded up, and breaks after four uses. Creative mode retains lapis, runes and catalysts and pays no XP.

## Crafting recipes

The supplied images define seven shaped recipes. Each produces one item and uses Chiseled Stone Bricks in all four corners. The other five slots follow this layout:

```text
Brick   Top     Brick
Side    Center  Side
Brick   Top     Brick
```

| Result | Top and bottom | Left and right | Center |
| --- | --- | --- | --- |
| Copper Rune | Coal | Lapis Lazuli | Copper Ingot |
| Iron Rune | Redstone Dust | Copper Ingot | Iron Ingot |
| Golden Rune | Nether Quartz | Blaze Powder | Gold Ingot |
| Diamond Rune | Emerald | Amethyst Shard | Diamond |
| Volcanite Rune, first variant | Diamond | Shulker Shell | Refined Volcanite |
| Volcanite Rune, second variant | Diamond | Echo Shard | Refined Volcanite |
| Enchanting Catalyst | Block of Lapis Lazuli | Blaze Powder | Sculk Catalyst |

Both rune-5 recipes replace the copper stair in the images with `wildways:volcanite`, as requested. Raw Volcanite and copper stairs are not accepted. Rune-5 variants share a recipe-book group; each recipe unlocks when all its distinct ingredient types are present in the inventory together.

Newly generated enchanted books store their enchantments at level one and their tooltips omit levels. Saved or command-created books carrying older levels still select the same recipe: their stored level never controls the result. Equipment tooltips retain actual levels. Books cannot be used or combined at the anvil.

The Eye of Enchanting (`wildways:enchanted_eye`) requires an Attunement enchanted book, at least a Copper Rune, one lapis and one XP level. No bookshelf is required. A plain book is insufficient. Master librarians sell Attunement books. The other book sources follow the [completed acquisition plan](BOOK_SOURCES.md).

## Mending and anvils

- Mending no longer repairs items through collected XP orbs; its datapack definition has no XP-repair effect.
- Repairing with an item's material at an anvil requires Mending. One material fully repairs the item, regardless of its remaining durability.
- The XP price is the item's highest enchantment level. Mending, Silk Touch, Channeling and Multishot count as at least level three for this calculation. Mending alone costs three levels; Mending with Efficiency V costs five. Renaming adds no XP cost, including when combined with another anvil operation.
- A material repair consumes exactly one material and does not increase the prior-work penalty. Repeated repairs retain the same base XP price.
- Same-item repairs and combining enchanted equipment still work without Mending. Their prior-work penalty grows by one instead of doubling. The forty-level cap is removed, while sufficient XP is still required.
- Volcanite equipment uses refined Volcanite as its repair material. Netherite equipment can use one Netherite Scrap as well as a Netherite Ingot. These repairs still require Mending.

## Librarians

Librarians unlock two offers per level, with three at journeyman. JSON trade sets select variable offers when their level unlocks. Opening the trade screen fills in missing fixed offers and migrates old trades. Existing librarians below journeyman lose their old Chiseled Bookshelf sale, and retained offers keep their uses.

| Profession level | Offer |
| --- | --- |
| Novice | Ordinary Bookshelf for 9 emeralds, and 24 paper for 1 emerald |
| Apprentice | Book purchase; either Copper Rune for 10 emeralds or Iron Rune for 20 emeralds |
| Journeyman | 5 ink sacs for 1 emerald, 2 writable books for 1 emerald, and Chiseled Bookshelf for 5 emeralds |
| Expert | Golden Rune for 30 emeralds, and either a clock for 5 emeralds or a compass for 4 emeralds |
| Master | Attunement enchanted book for 18 emeralds, plus a regional enchanted book for one plain book and a base price of 10–38 emeralds in steps of two |

| Villager type | Regional book |
| --- | --- |
| Snow | Unbreaking |
| Savanna and Jungle | Sharpness |
| Plains and Taiga | Efficiency |
| Swamp and Desert | Protection |

The regional book is based on the villager's type and is first offered at master level. Each book follows the usual WildWays enchanting rules. The trade uses vanilla Mending pricing. The base price is rolled once per offer. Moving the villager or replacing its lectern leaves its specialty tied to its type. Existing masters replace their old candle trade when the trading screen opens.

The Attunement book is always available at master level, without a donation or other unlock. The catalyst is obtained through its crafting recipe, not librarian trading. The Attunement offer restocks through vanilla merchant data.

## Assets and workstation storage

- Runes and the catalyst use the latest user-provided 16×16 item sprites. The runes are named Copper, Iron, Golden, Diamond and Volcanite Rune. The menu uses the gray vanilla container style. Its lapis, book, rune and catalyst slots have replaceable placeholder sprites; the equipment slot has none.
- Closing the Enchanting Table or anvil leaves their input items in that workstation. The table displays its main item hovering and turning above it, with the other ingredients orbiting it. The anvil displays its two input items lying on top. Breaking either workstation returns its stored items.

## Technical approach

JSON defines Mending's disabled orb effect, precious-enchantment tags and librarian trade sets. Fabric interaction events open the small replacement table menu and migrate existing librarian offers. Targeted mixins adjust vanilla anvil calculation and book generation/tooltips; a client mixin removes the anvil's forty-level warning. All costs and output eligibility are checked on the server.

## In-game checks

1. Run `.\gradlew.bat runClient` in a disposable world with cheats. Open a vanilla Enchanting Table and verify that the gray inventory-style screen shows a tinted XP orb close to its number, a bookshelf icon beside the shelf count, short error messages, and no green success message. Check different GUI scales and multiplayer synchronization if available.
2. Give `/give @s wildways:rune_1 16`, `/give @s minecraft:lapis_lazuli 16` and `/give @s minecraft:enchanted_book[minecraft:stored_enchantments={"minecraft:unbreaking":1}]`. Give one XP level with `/experience set @s 1 levels`. Enchant a diamond or Volcanite pickaxe without shelves. Check the one-level charge, one lapis, one rune, retained book and no duplicated equipment, using both ordinary clicks and Shift-click.
3. Test Iron through Volcanite Runes with 5/10/15/20 usable bookshelves for level 2-5 enchantments. Fortune III should also work with Diamond and Volcanite Runes for only ten shelves and three levels. Try one missing shelf, a full-block obstruction, insufficient XP, incompatible books and an equal level already on the item. Remove a shelf after a preview appears and check that taking it is rejected.
4. Craft the runes and catalyst with the layouts above. Confirm the Iron Rune uses Redstone Dust and Copper Ingots, while the Diamond Rune uses Emeralds and Amethyst Shards. The Volcanite Rune accepts either two Shulker Shells or two Echo Shards, with refined Volcanite in the center; copper stairs and Raw Volcanite must fail. Check that each result is one item and that the recipes unlock in the recipe book. Use `/give @s wildways:enchanting_catalyst` if testing only enchanting: verify it halves the effective tier's XP cost, rounded up, and breaks on its fourth use.
5. Try Mending, Silk Touch, Channeling and Multishot with an Iron Rune, then a Golden Rune and ten shelves. Only the latter succeeds, costs three levels without a catalyst, and applies level one. Check that equipment retains numeric enchantment levels while book tooltips omit them.
6. Give `/give @s wildways:enchanted_eye` and `/give @s minecraft:enchanted_book[minecraft:stored_enchantments={"wildways:attunement":1}]`. With rune 1, lapis and one XP level, enchant the eye and verify that it works in an End Portal Frame. A plain book must fail.
7. Damage an item with Mending, collect XP, and confirm it stays damaged. Repair it at an anvil with its material: diamond for diamond equipment, refined Volcanite for Volcanite, or Netherite Scrap for Netherite. Check that one material fully repairs it and that Mending alone costs three levels. Remove Mending and verify material repair is unavailable.
8. Combine damaged copies of the same item at an anvil, including enchanted copies. Repeat to observe linear prior-work growth, and verify combinations above forty levels when enough XP is available. Attempt to apply or combine enchanted books; no output should appear.
9. Level a librarian through the new offers. Check that Novice sells an ordinary Bookshelf and buys paper, and Apprentice sells either a Copper or Iron Rune. Journeyman buys ink sacs and writable books and sells a Chiseled Bookshelf for 5 emeralds. Expert sells a Golden Rune for 30 emeralds and either a clock or a compass. Master sells Attunement for 18 emeralds and the regional book for an even base price of 10–38 emeralds plus one plain book. Check masters from different village types, including an existing master whose candle offer should be replaced. Holding an enchanted book while opening the trade screen must not consume it. Reload the world and check that the offers persist and restock. Old catalyst offers should disappear.
10. Leave ingredients in the Enchanting Table and both inputs in an anvil, close each screen and reload the world. Confirm they remain in the correct workstation and appear outside it, with the anvil items lying flat. Break each block and check that the stored items are returned exactly once.

Automated server integration tests run with `.\gradlew.bat runGameTest`. They exercise actual menu transactions, the universal catalyst at every rune tier, live shelf checks, Mending data and repairs, book normalization, Attunement Shift-click, librarian offer migration, anvil combinations, both rune-5 recipes, the catalyst crafting result, book conversion recipes, custom book loot tables and planned trader offers. Client layout and survival pacing remain manual checks.

## Verification performed

- `.\gradlew.bat build`: successful on Java 25. The most recent GameTest run passed all 25 required tests.
- The resource JSON files parsed successfully. The test server loaded the enchanting recipes and advancements, and the packaged JAR contains the single catalyst's assets with its user-provided sprite.
- The test server loaded the mod, datapack registries, recipes, advancements and ore biome modification successfully. Shared mixins were exercised at runtime.
- Client code compiles. The gray layout and workstation item placement were reviewed in follow-up playtests. Multiplayer synchronization still needs a dedicated manual check.
