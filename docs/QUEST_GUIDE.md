# Customize the WildWays Guide

Use a Minecraft resource pack to edit Guide text and icons. Enable the pack in Resource Packs and reopen the Guide after reloading resources.

For a new pack on Minecraft 26.3, create a folder under `resourcepacks/` and put this `pack.mcmeta` at its root, alongside the `assets/` directory.

```json
{
  "pack": {
    "description": "My WildWays Guide",
    "min_format": [97, 1],
    "max_format": [97, 1]
  }
}
```

## Titles and descriptions

In your resource pack, add entries to `assets/wildways/lang/en_us.json` to customize the English text. You can include only the keys you want to replace. The full list of keys is in `src/main/resources/assets/wildways/lang/en_us.json`. To add a translation, use a separate file for that locale, such as `assets/wildways/lang/de_de.json` for German.

```json
{
  "wildways.guide.group.end": "The Way to the End",
  "wildways.guide.group.end.description": "Find the eyes that open the portal.",
  "wildways.guide.quest.eye_of_darkness": "An Echo in the Dark",
  "wildways.guide.quest.eye_of_darkness.description": "Use echo shards to craft the Eye of Darkness."
}
```

Group descriptions appear when you hover over a group in the overview. Quest descriptions appear in the quest detail view.

## Icons

Add `assets/wildways/quest_guide/icons.json` to the pack. Use an item ID or a PNG texture path for each override. An item ID also works for blocks with an inventory item, such as `minecraft:amethyst_block`.

```json
{
  "group.end": { "item": "minecraft:ender_eye" },
  "quest.eye_of_darkness": { "texture": "wildways:textures/gui/quest_guide/darkness.png" }
}
```

Place the example PNG at `assets/wildways/textures/gui/quest_guide/darkness.png`. The Guide draws it at 16 × 16 pixels before zooming. Use `root` to customize the overview's center icon. To change a Guide icon in the source for everyone, edit its default item in `QuestGuide.java`.

## XP rewards

WildWays creates `config/wildways-quests.json` when you first open a world or start a server. The server or single-player host reads it at world startup.

```json
{
  "defaultXp": 25,
  "questXp": {
    "activate_end_portal": 75,
    "packed_nest": 50,
    "read_clock": 0
  }
}
```

Amounts are XP points. The keys in `questXp` are the quest IDs from the language keys, without the `wildways.guide.quest.` prefix. Use `0` for a quest with sound and chat feedback only. Restart the server or reopen the single-player world after editing the file. Rewards apply on completion and are tracked once per quest and player in each world. A quest shared by two chapters pays once.
