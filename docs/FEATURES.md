# Current Features

This page describes gameplay features currently present in WildWays. Planned work remains in [ROADMAP.md](ROADMAP.md).

## Exploration and information

- Holding a vanilla compass displays block coordinates and one of the eight cardinal or intercardinal directions in the action bar.
- Holding a clock displays the current overworld day.
- The Biome Compass is crafted from a map surrounded by four iron ingots and displays the current biome.
- Holding a Light Sensor displays the local raw light level. It is crafted with a daylight detector at the top centre, redstone at the centre, copper at the bottom centre, iron ingots in the four corners, and planks at the middle left and right. Its top, sides, and bottom use distinct textures.
- Quarantine Grounds can generate in selected taiga biomes. Their terrain-aware pieces follow local ground, their placed Endermites persist, and hospital-house weathering can convert regular spruce logs into correctly orientated stripped spruce logs.

## Volcanite prototype

- Volcanite is a Nether alternative to diamond equipment. Its ore generates vertically in basalt in newly generated Basalt Deltas, can be placed along any axis like Basalt, and uses separate Volcanite side and end textures. It drops Raw Volcanite, which is processed in a furnace or blast furnace.
- Raw and refined Volcanite each pack into a matching storage block in a 3×3 grid; each block unpacks into nine items and requires an iron-tier pickaxe.
- Five tools and four armor pieces match diamond values, use normal crafting shapes, are repaired with refined Volcanite when enchanted with Mending, and can be upgraded to Netherite.
- Bastion Bridge, Hoglin Stable, and Other chests each have a 5% chance for 1-2 refined Volcanite; Treasure chests have a 15% chance. Ore generation rates remain provisional. See [prototype details and testing](VOLCANITE.md).

## Enchanting prototype

- Enchanting Tables use a reusable enchanted recipe book, one lapis and a rune that selects the enchantment level. Higher rune tiers require 5/10/15/20 usable bookshelves and cost up to five XP levels; Mending and Silk Touch require tier 5.
- One universal catalyst halves XP costs, rounded up, for every rune tier and lasts four uses. Books have no displayed levels and cannot be used in an anvil.
- Mending lets one repair material fully restore an item for XP equal to its highest enchantment level; XP orbs no longer repair items. Same-item anvil combinations remain available, with linear prior-work growth and no forty-level cap.
- Librarians add at most two trades per level. They always sell ordinary Bookshelves, choose either Rune 1 or Rune 2 at apprentice level, and always sell an Attunement book at master level alongside one candle color. Chiseled Bookshelves are a possible novice alternative. All five runes and the catalyst are craftable from the supplied designs; Rune 5 has Shulker Shell and Echo Shard variants using refined Volcanite. Other book acquisition changes are deferred. See [recipes, rules and test steps](ENCHANTING.md).
- The Eye of Enchanting requires an Attunement book, rune 1 and one lapis at the new table.

## Fletching Table and arrows

- The Fletching Table has a dedicated crafting screen with a feather placeholder and a result slot. Feather, stick, and flint craft five arrows; one arrow and four glowstone dust craft two spectral arrows.
- Arrow conversion recipes create 1–8 tipped arrows from regular or splash potions, or 1–64 from lingering potions. Turtle scutes convert up to 16 arrows, phantom membranes up to 8, and TNT up to 4.
- Turtle Arrows have 50% increased base damage.
- Range Arrows fly 50% faster while retaining normal arrow damage.
- Explosive Arrows create a small block-breaking explosion with reduced entity damage and respect normal mob-griefing behaviour.

## Endermites and building

- Endermites no longer use vanilla's fixed despawn timer.
- Endermites drop Endermite Shells, which support the Unease brewing path and Endermite-themed recipes.
- Four Endermite Bricks are crafted from four Stone Bricks and one Endermite Shell. They are available as full blocks, slabs, stairs, and walls, with stonecutter recipes for each shape.
- The Endermite Nest is crafted from eight Endermite Shells surrounding a chest. It is a waterloggable, portable twelve-slot container with an animated side texture and a distinct open top. Opening it has a 5% chance to spawn an Endermite nearby, its inventory fullness supplies a comparator signal, and a vanilla Eye of Ender left inside transforms at a random time averaging about ten minutes.

## End progression

- End Portal Frames generate empty and no longer accept vanilla Eyes of Ender.
- Opening an End Portal requires one each of the twelve WildWays portal eyes. Duplicate eye types are rejected.
- Left-clicking a filled End Portal Frame returns its stored eye and closes the nearby portal if it was already open.
- Eyes are found through snowy villages, igloos, Trail Ruins archaeology, Piglin Brutes, Creakings, crafting, enchanting, Evokers, active Conduits, and Endermite Nests. The Eye of the Tiger is registered for the planned Jungle Temple room but is not yet assigned to loot.
- The Eye of Ice is guaranteed in igloo chests and has a 10% chance in snowy-village house chests. Piglin Brutes have a one-in-three chance to drop the Eye of the Brute, and each Creaking has a 5% chance to drop one Eye of the Creaking when successfully hit, limited to one eye per Creaking.
- The Eye of Storm is crafted with an Eye of Ender in the centre, Breeze Rods in the corners, and Wind Charges on the remaining sides. The Eye of Darkness uses an Eye of Ender with four Echo Shards; the Eye of Enchanting uses an Eye of Ender with four Amethyst Shards in the corners and four Lapis Lazuli on the sides, then must be enchanted before it can activate an End Portal Frame.
- The Lost Eye is found through common Trail Ruins suspicious-gravel archaeology with a 2/47 chance. An Eye of Ender in range of an active Conduit transforms into an Eye of Water when it is in water or rain. Dropping an Eye of Ender near an available Evoker transforms it into an Eye of the Illagers after a short casting warm-up.
- The Eye of Brewing chain is performed in a Brewing Stand: Eye of Ender with Nether Wart, Awkward Eye with Phantom Membrane, then Thick Eye with a Ghast Tear.
- WildWays crafting recipes unlock in the recipe book once all of their distinct ingredient types are present in the inventory at the same time.

## Potions and effects

- Normal, splash, and lingering potions stack to eight.
- Brewing an Awkward Potion with an Endermite Shell creates Potion of Unease; glowstone upgrades it to Strong Unease. Unease lasts three minutes and has a 10% Endermite-spawn chance when the affected player breaks a solid block; Strong Unease lasts 90 seconds and has a 20% chance. Uneasy creepers use the same chance for each solid block damaged by their explosions.
- Popped Chorus Fruit upgrades selected strong or long vanilla potions into Supreme variants: Swiftness, Leaping, Strength, Healing, Regeneration, Fire Resistance, Water Breathing, Night Vision, Invisibility, and Slow Falling. The same ingredient produces Fatal Slowness, Harming, Poison, and Weakness variants from their corresponding vanilla potions. Every custom potion tier is available as a regular, splash, lingering, and tipped potion.

| Upgrade | Effects |
| --- | --- |
| Supreme Swiftness / Leaping | Speed IV or Jump Boost IV for 30 seconds; Hunger II for 30 seconds |
| Supreme Strength | Strength III for 30 seconds; Blindness I for 10 seconds |
| Supreme Healing | Instant Health III; Slowness II for 30 seconds |
| Supreme Regeneration | Regeneration III for 20 seconds; Weakness I for 30 seconds |
| Supreme Fire Resistance | Special fire resistance for 8 minutes; Hunger II for 30 seconds |
| Supreme Water Breathing | Water Breathing for 8 minutes; Conduit Power for 60 seconds; Nausea I for 10 seconds |
| Supreme Night Vision | Special night vision and Glowing I for 3 minutes; Nausea I for 10 seconds |
| Supreme Invisibility | Special invisibility for 8 minutes; Blindness I for 30 seconds |
| Supreme Slow Falling | Special slow falling for 4 minutes; Levitation I for 10 seconds |
| Fatal Slowness | Slowness V for 30 seconds; Invisibility I for 10 seconds |
| Fatal Harming | Instant Damage III; Speed II for 30 seconds |
| Fatal Poison | Poison III for 10 seconds; Strength I for 30 seconds |
| Fatal Weakness | Weakness III and Resistance I for 30 seconds |

- Special Supreme effects add behaviour beyond their potion entries: fire resistance immediately extinguishes the user, prevents fire damage, and increases lava-swimming speed; night vision makes nearby living entities glow within 15 blocks; invisibility has no particles and halves normal visibility; slow falling prevents fall damage and slightly accelerates mid-air movement.

## Redstone and utility blocks

- The Light Sensor outputs a redstone signal from 0 to 15 based on the raw light level directly above it and updates once per second.
