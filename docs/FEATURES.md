# Current Features

This page describes gameplay features currently present in WildWays. Planned work remains in [ROADMAP.md](ROADMAP.md).

## Exploration and information

- Holding a vanilla compass displays block coordinates and one of the eight cardinal or intercardinal directions in the action bar.
- Holding a clock displays the current overworld day.
- The Biome Compass displays the current biome.
- Holding a Light Sensor displays the local raw light level.
- Quarantine Grounds can generate in selected taiga biomes. Their terrain-aware pieces follow local ground, their placed Endermites persist, and hospital-house weathering can convert regular spruce logs into correctly orientated stripped spruce logs.

## Fletching Table and arrows

- The Fletching Table has a dedicated crafting screen with a feather placeholder and a result slot. Feather, stick, and flint craft five arrows.
- Arrow conversion recipes create spectral arrows, tipped arrows from regular, splash, or lingering potions, Turtle Arrows from turtle scutes, Range Arrows from phantom membranes, and Explosive Arrows from TNT.
- Turtle Arrows have increased base damage.
- Range Arrows fly 50% faster while retaining normal arrow damage.
- Explosive Arrows create a small block-breaking explosion with reduced entity damage and respect normal mob-griefing behaviour.

## Endermites and building

- Endermites no longer use vanilla's fixed despawn timer.
- Endermites drop Endermite Shells, which support the Unease brewing path and Endermite-themed recipes.
- Endermite Bricks are available as full blocks, slabs, stairs, and walls.
- The Endermite Nest is a waterloggable, portable twelve-slot container. Opening it has a 5% chance to spawn an Endermite nearby, and its inventory fullness supplies a comparator signal.

## Potions and effects

- Normal, splash, and lingering potions stack to eight.
- Brewing an Awkward Potion with an Endermite Shell creates Potion of Unease; glowstone upgrades it to Strong Unease.
- Unease can cause Endermites to appear when affected players break solid blocks. Uneasy creepers can also produce Endermites from solid blocks damaged by their explosions.
- Popped Chorus Fruit upgrades selected strong or long vanilla potions into Supreme variants: Swiftness, Leaping, Strength, Healing, Regeneration, Fire Resistance, Water Breathing, Night Vision, Invisibility, and Slow Falling.
- The same brewing path produces Fatal Slowness, Harming, Poison, and Weakness variants from their corresponding vanilla potions.
- Supreme effects add specialised behaviour: fire resistance extinguishes the user, night vision reveals nearby living entities, invisibility suppresses ordinary visibility, and slow falling prevents fall damage.

## Redstone and utility blocks

- The Light Sensor outputs a redstone signal from 0 to 15 based on the raw light level directly above it and updates once per second.
