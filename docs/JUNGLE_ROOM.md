# Editing the Jungle Temple book alcove

The current Projectile Protection location is a **small carved alcove**, not a separately authored temple room. Its geometry is in `JungleTempleBookRoomMixin.java`; its contents are in `data/wildways/loot_table/chests/jungle_hidden_room.json`. Both apply only when a new Jungle Temple generates.

The mixin runs after Minecraft builds the ordinary temple. All coordinates in its `postProcess` method are **local to the temple**, so Minecraft rotates them with the structure:

```java
generateAirBox(world, bounds, 3, -3, 10, 7, -1, 11);
placeBlock(world, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 6, -4, 11, bounds);
createChest(world, bounds, random, 6, -3, 11, WILDWAYS$ROOM_LOOT);
```

The first call opens the alcove from `(3,-3,10)` through `(7,-1,11)`. The second makes a solid floor under its chest at `(6,-3,11)`. To reshape it, change that volume, add `generateBox(...)` for walls/floor/ceiling, and use `placeBlock(...)` for decorations. Keep an open route from the lower corridor at `x=1..3`, and put the chest in air above a full block. The vanilla final staircase occupies `x=5..6,z=9,y=-3`; the existing traps and hidden chest are mainly farther east. Altering those positions can break temple navigation or redstone.

If you want to design a larger, detailed room directly in Minecraft, build a prototype with blocks and a Structure Block, save it as an `.nbt` template, and provide that template together with where it should connect to the temple. The current mixin does **not** load custom templates yet; integrating one would be a separate code change, including rotation, piece bounds and chunk-safe placement. A sketch with dimensions and an entrance position also works for a Java-built room.

After code or loot edits, run `.\gradlew.bat build` on Java 25 and test in a fresh world or unexplored chunks. `/locate structure minecraft:jungle_pyramid` finds a new temple; check all orientations, the stairs and both original traps, access to the new chest, and its roughly 50% Projectile Protection chance. Existing generated temples do not regenerate.
