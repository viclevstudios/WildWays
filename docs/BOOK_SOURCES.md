# Enchanted book sources

The [Notion enchanting plan](https://app.notion.com/p/3ddcef41da72800abdb1ed9f5a201a53) is implemented as level-one, single-enchantment recipe books. A book is reusable at the Enchanting Table; a book used as a crafting ingredient is consumed. The three special books Soul Speed, Swift Sneak and Wind Burst stay outside the broad random pool and are somewhat rarer than vanilla because one book now unlocks every applicable rune level.

## Broad ways to find books

- Desert and Jungle Temple chests and Stronghold chests retain their vanilla enchanted-book slots, but select one enchantment from the general random-loot tag.
- A Wandering Trader adds one randomly chosen general book to its offers. Ancient City generic books select from the 23-book list in `data/wildways/tags/enchantment/ancient_city_books.json`.
- Trial Chamber reward tables retain their random books. Density and Breach have higher weights in rare rewards; Wind Burst has a lower chance there. Bastion chests and Piglin bartering remain sources of Soul Speed at slightly lower weights; Ancient Cities remain the source of Swift Sneak at a lower weight.
- The Endermite Nest in Quarantine Grounds uses its existing structure loot-table reference to provide one or two level-one books from Unbreaking, Protection, Efficiency, Fortune, Silk Touch, Sharpness and Power. Each roll selects independently, so two identical books are possible.

## Specific sources

| Book | Planned source now implemented |
| --- | --- |
| Unbreaking | Trail Ruins archaeology; possible Hero of the Village gift from a Toolsmith or Armorer |
| Mending | Recipe using Unbreaking, a Ghast Tear, an XP Bottle, two Volcanite Ingots and four Lapis; 10% in End City treasure chests |
| Curse of Binding / Vanishing | Each has a 5% Witch drop chance |
| Protection | Desert Pyramid or Desert Well archaeology; 30% in monster-room chests |
| Fire Protection | Crafted from Protection, four Blaze Powder and four Lapis; Nether Fortress chests |
| Blast Protection | Crafted from Protection, Obsidian, three Iron Ingots and four Lapis; guaranteed from a Ghast killed by its reflected fireball |
| Projectile Protection | Crafted from Protection, a Shield, three Iron Ingots and four Lapis; 50% in the [Jungle Temple alcove chest](JUNGLE_ROOM.md) |
| Thorns | 5% Guardian drop; 50% chance that a master Armorer sells it |
| Aqua Affinity / Respiration | Shipwreck treasure and Buried Treasure chests |
| Depth Strider | Two new supply chests in each Ocean Monument entry room, each with a 75% book chance and Sponge, Nautilus Shell, Prismarine Shard or Gold Ingot loot |
| Frost Walker | Igloo and snowy-village house chests |
| Feather Falling | Crafted from Protection, Phantom Membranes, Feathers and Lapis |
| Efficiency | Mineshaft minecart chests and Trail Ruins archaeology |
| Fortune / Silk Touch | Rare Mineshaft minecart chests; guaranteed master Toolsmith / Mason trade respectively |
| Sharpness | 50% Weaponsmith village chest, 25% Woodland Mansion chest, possible Hero of the Village Weaponsmith gift |
| Smite / Bane of Arthropods / Power | 50% in a Zombie / Spider / Skeleton monster-room chest, selected from the actual spawner; Power also drops from Skeletons (2.5%) and Strays or Bogged (5%) |
| Knockback / Fire Aspect / Sweeping Edge | Crafted from Sharpness; Fire Aspect also occurs in Ruined Portal chests |
| Looting | Guaranteed master Weaponsmith trade |
| Lunge | 50% drop from a Zombie carrying a spear |
| Punch / Flame | Crafted from Power; Flame also occurs in Ruined Portal chests |
| Infinity | Guaranteed master Fletcher trade |
| Quick Charge / Multishot / Piercing | Pillager Outpost chests (10% each), Woodland Mansion chests (5% each); Pillagers and Vindicators can drop Quick Charge or Multishot, with Piercing drops during active raids |
| Impaling / Loyalty / Riptide / Channeling | Ocean Ruin chests and archaeology; Impaling also drops from Drowned (5%); Riptide can be crafted from Impaling; an ordinary dropped Book struck by lightning becomes Channeling |
| Luck of the Sea / Lure | Fishing treasure and a master Fisherman, who sells one of the two |

All eleven conversion recipes live in `data/wildways/recipe/book_*.json`. Their component ingredients require the named input book, so a generic enchanted book cannot replace it. `scripts/generate_book_loot.py` produces the Minecraft 26.2 table overrides from the original game tables while preserving unrelated vanilla loot. The three monster-room variants and the two new structure chests have separate WildWays tables.

## In-game checks

Use a **new world or new chunks** for structure changes. Open a Zombie, Spider and Skeleton spawner dungeon: its chest should retain ordinary dungeon loot, sometimes include Protection, and have a 50% chance for the spawner's matching book. Find a Jungle Temple and follow the lower corridor to its extra alcove and chest; approximately half contain Projectile Protection. An Ocean Monument's entry room has two supply chests, with Depth Strider frequently present. Find the Endermite Nest in Quarantine Grounds and check that it starts with one or two of the seven listed common books. Try a master Toolsmith, Mason, Weaponsmith, Fletcher, Fisherman and Armorer, then a Wandering Trader; reopening trade screens must not create extra offers. For drops, test a spear Zombie, a reflected Ghast fireball and a book dropped into a lightning strike.

Run `.\gradlew.bat build runGameTest` with Java 25 for server-side checks. The automated tests cover actual recipe matching, the three custom chest tables, preservation of Volcanite Bastion loot, master-trade replacement, the wandering book offer and lightning conversion. Structure layout and rare drop rates still benefit from an in-game playtest.
