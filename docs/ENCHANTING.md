# Enchanting prototype

Source: [Enchanting design in Notion](https://app.notion.com/p/3ddcef41da72800abdb1ed9f5a201a53), the user's supplied recipe images, and the user's Mending, Attunement and universal-catalyst clarifications. This is an in-progress prototype; planning is still in progress.

## Implemented rules

The vanilla Enchanting Table opens a recipe menu. Insert an equipment item, one Lapis Lazuli, an enchanted recipe book and a rune. The book remains in the menu; taking the result consumes one lapis and one rune. Existing enchantments remain, compatible enchantments can be added or upgraded, and incompatible combinations or downgrades are rejected. Books with several enchantments allow selecting one recipe with the arrow buttons.

| Rune | Applied enchantment level | Required usable bookshelves | XP levels | XP with catalyst |
| --- | --- | --- | --- | --- |
| 1 | 1 | 0 | 1 | 1 |
| 2 | 2 | 5 | 2 | 1 |
| 3 | 3 | 10 | 3 | 2 |
| 4 | 4 | 15 | 4 | 2 |
| 5 | 5 | 20 | 5 | 3 |

The enchantment's normal maximum level still applies. Mending and Silk Touch require rune 5 and twenty usable bookshelves but produce their normal level-one enchantments. The same vanilla bookshelf distance and unobstructed-gap checks apply; shelves are checked again before taking a result. Only the displayed cost is required, with no additional minimum XP threshold.

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
| Rune 1 | Coal | Lapis Lazuli | Copper Ingot |
| Rune 2 | Amethyst Shard | Amethyst Shard | Iron Ingot |
| Rune 3 | Nether Quartz | Blaze Powder | Gold Ingot |
| Rune 4 | Emerald | Emerald | Diamond |
| Rune 5, first variant | Diamond | Shulker Shell | Refined Volcanite |
| Rune 5, second variant | Diamond | Echo Shard | Refined Volcanite |
| Enchanting Catalyst | Block of Lapis Lazuli | Blaze Powder | Sculk Catalyst |

Both rune-5 recipes replace the copper stair in the images with `wildways:volcanite`, as requested. Raw Volcanite and copper stairs are not accepted. Rune-5 variants share a recipe-book group; each recipe unlocks when all its distinct ingredient types are present in the inventory together.

Newly generated enchanted books store their enchantments at level one and their tooltips omit levels. Saved or command-created books carrying older levels still select the same recipe: their stored level never controls the result. Equipment tooltips retain actual levels. Books cannot be used or combined at the anvil.

The Eye of Enchanting (`wildways:enchanted_eye`) requires an Attunement enchanted book, rune 1, one lapis and one XP level. No bookshelf is required. A plain book is insufficient. Master librarians sell Attunement books; new acquisition locations for other enchanted books are intentionally deferred to later planning.

## Mending and anvils

- Mending no longer repairs items through collected XP orbs; its datapack definition has no XP-repair effect.
- Repairing with an item's material at an anvil requires Mending. One material repairs 33% of maximum durability, rounded up and capped at full repair. A diamond pickaxe repairs 516 durability per diamond.
- The XP price is the item's highest numeric enchantment level. Mending alone costs one level; Mending with Efficiency V costs five. A simultaneous rename adds vanilla's one-level rename cost.
- A material repair consumes exactly one material and does not increase the prior-work penalty. Repeated repairs retain the same base XP price.
- Same-item repairs and combining enchanted equipment still work without Mending. Their prior-work penalty grows by one instead of doubling. The forty-level cap is removed, while sufficient XP is still required.
- Volcanite equipment uses refined Volcanite as its repair material, subject to these same Mending rules.

## Librarians

New librarians use JSON trade sets with at most two offers per level. The variable offer is selected once when that level unlocks; the fixed offers are added when the trading screen opens. Interacting with an existing librarian removes old random enchanted-book sales and previously unlocked catalyst offers, reduces excess offers from earlier WildWays versions, and retains ordinary bookshelf sales without resetting the uses of kept offers.

| Profession level | Offer |
| --- | --- |
| Novice | Ordinary Bookshelf for 9 emeralds; either paper purchase or Chiseled Bookshelf for 5 emeralds |
| Apprentice | Book purchase; either Rune 1 for 10 emeralds or Rune 2 for 20 emeralds |
| Journeyman | Ink sac purchase; Biome Compass for 5 emeralds |
| Expert | Two of writable book purchase, clock sale and compass sale |
| Master | Attunement enchanted book for 18 emeralds; either red or yellow candle |

The Attunement book is always available at master level, without a donation or other unlock. The catalyst is obtained through its crafting recipe, not librarian trading. The Attunement offer restocks through vanilla merchant data.

## Open planning and assets

- Other enchanted-book loot is not relocated in this prototype. Existing vanilla loot sources remain until the new locations are planned.
- Runes and catalysts use vanilla placeholder artwork. Volcanite has first-pass pixel textures. The menu uses the gray vanilla container style with separate, replaceable placeholder sprites in its five input slots. No AI-generated images were added.

## Technical approach

JSON defines Mending's disabled orb effect, precious-enchantment tags and librarian trade sets. Fabric interaction events open the small replacement table menu and migrate existing librarian offers. Targeted mixins adjust vanilla anvil calculation and book generation/tooltips; a client mixin removes the anvil's forty-level warning. All costs and output eligibility are checked on the server.

## In-game checks

1. Run `.\gradlew.bat runClient` in a disposable world with cheats. Open a vanilla Enchanting Table and verify that the gray inventory-style screen keeps its title, recipe, costs, status, placeholder sprites and inventory distinct at different GUI scales. With no rune, the screen should show the count of nearby bookshelves without a misleading `/0` requirement. Test multiplayer synchronization if available.
2. Give `/give @s wildways:rune_1 16`, `/give @s minecraft:lapis_lazuli 16` and `/give @s minecraft:enchanted_book[minecraft:stored_enchantments={"minecraft:unbreaking":1}]`. Give one XP level with `/experience set @s 1 levels`. Enchant a diamond or Volcanite pickaxe without shelves. Check the one-level charge, one lapis, one rune, retained book and no duplicated equipment, using both ordinary clicks and Shift-click.
3. Test runes 2-5 with 5/10/15/20 usable bookshelves. Try one missing shelf, obstructed gaps, insufficient XP, incompatible books and a lower or equal level already on the item. None should produce a collectible result. Remove a shelf after a preview appears and check that taking it is rejected.
4. Craft the runes and catalyst with the layouts above. Confirm that Rune 5 accepts either two Shulker Shells or two Echo Shards, with refined Volcanite in the center; copper stairs and Raw Volcanite must fail. Check that each result is one item and that the recipes unlock in the recipe book. Use `/give @s wildways:enchanting_catalyst` if testing only enchanting: verify the same catalyst works with all five rune tiers for 1/1/2/2/3 XP levels and breaks on its fourth use.
5. Try Mending and Silk Touch books with rune 1, then rune 5 and twenty shelves. Only the latter succeeds. Check that equipment retains numeric enchantment levels while book tooltips omit them.
6. Give `/give @s wildways:enchanted_eye` and `/give @s minecraft:enchanted_book[minecraft:stored_enchantments={"wildways:attunement":1}]`. With rune 1, lapis and one XP level, enchant the eye and verify that it works in an End Portal Frame. A plain book must fail.
7. Damage an item with Mending, collect XP, and confirm it stays damaged. Repair it at an anvil with its material: diamond for diamond equipment, refined Volcanite for Volcanite. Check exactly 33% maximum durability per material and a fixed price equal to its highest enchantment level. Remove Mending and verify material repair is unavailable.
8. Combine damaged copies of the same item at an anvil, including enchanted copies. Repeat to observe linear prior-work growth, and verify combinations above forty levels when enough XP is available. Attempt to apply or combine enchanted books; no output should appear.
9. Level a librarian through the new offers. Check that each level adds at most two trades: Novice always sells an ordinary Bookshelf, Apprentice sells either Rune 1 or Rune 2 (never both), Expert adds two offers, and Master always sells Attunement for eighteen emeralds alongside only one candle color. Holding an enchanted book while opening the trade screen must not consume it. Reload the world and check that the Attunement offer persists and restocks; old catalyst offers should disappear.

Automated server integration tests run with `.\gradlew.bat runGameTest`. They exercise actual menu transactions, the universal catalyst at every rune tier, live shelf checks, Mending data and repairs, book normalization, Attunement Shift-click, librarian offer migration, anvil combinations, both rune-5 recipes and the catalyst crafting result. Client layout and survival pacing remain manual checks.

## Verification performed

- `.\gradlew.bat build runGameTest`: successful on Java 25. All ten required tests passed (nine WildWays tests and the Fabric test suite's own test).
- All 246 resource JSON files parsed successfully. The test server loaded seven additional crafting recipes and seven recipe-book advancements. The packaged JAR contains the single catalyst's assets and no obsolete tiered catalyst assets.
- The test server loaded the mod, datapack registries, recipes, advancements and ore biome modification successfully. Shared mixins were exercised at runtime.
- Client code compiles. The gray layout and its placeholder sprites were inspected in the client and approved in the follow-up playtest. The client-only anvil label change and multiplayer synchronization still need manual checks.
