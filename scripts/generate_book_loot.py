"""Generate the Minecraft 26.2 loot-table overrides for WildWays book sources.

Run with the Minecraft common jar as the first argument. The checked-in JSON is
the runtime data; this script keeps vanilla's unrelated entries intact.
"""

import json
import sys
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "src/main/resources/data/minecraft/loot_table"
SOURCE = "data/minecraft/loot_table/"
GENERAL = "#wildways:general_books"
ANCIENT = "#wildways:ancient_city_books"
FISHING = "#wildways:fishing_books"
TRIDENT = "#wildways:trident_books"


def book(enchantment, weight=1):
    return {
        "type": "minecraft:item",
        "name": "minecraft:book",
        "weight": weight,
        "functions": [{"function": "minecraft:enchant_randomly", "options": enchantment}],
    }


def chance_book(enchantment, numerator, denominator):
    return {
        "rolls": 1,
        "entries": [
            book(enchantment, numerator),
            {"type": "minecraft:empty", "weight": denominator - numerator},
        ],
    }


def choice_book(enchantments, chance_numerator, chance_denominator):
    return {
        "rolls": 1,
        "entries": [
            *[book(name, chance_numerator) for name in enchantments],
            {
                "type": "minecraft:empty",
                "weight": chance_denominator - chance_numerator * len(enchantments),
            },
        ],
    }


def is_enchanted_book(entry):
    return entry.get("name") == "minecraft:book" and any(
        function.get("function") in ("minecraft:enchant_randomly", "minecraft:enchant_with_levels", "minecraft:set_enchantments")
        for function in entry.get("functions", [])
    )


def original_books(table):
    return [entry for pool in table["pools"] for entry in pool["entries"] if is_enchanted_book(entry)]


def remove_original_books(table):
    for pool in table["pools"]:
        pool["entries"] = [entry for entry in pool["entries"] if not is_enchanted_book(entry)]


def add(table, *pools):
    table["pools"].extend(pools)


def main(jar_path):
    with zipfile.ZipFile(jar_path) as jar:
        def load(path):
            return json.loads(jar.read(SOURCE + path + ".json"))

        tables = {}

        # Keep the vanilla number and weight of random temple and stronghold books,
        # but give every generated book exactly one eligible recipe enchantment.
        for path in (
            "chests/desert_pyramid",
            "chests/jungle_temple",
            "chests/stronghold_library",
            "chests/stronghold_corridor",
            "chests/stronghold_crossing",
        ):
            table = load(path)
            for entry in original_books(table):
                entry["functions"] = [{"function": "minecraft:enchant_randomly", "options": GENERAL}]
            tables[path] = table

        table = load("chests/ancient_city")
        for entry in original_books(table):
            option = entry["functions"][0].get("options")
            if option == "minecraft:swift_sneak":
                entry["weight"] = 2  # Vanilla: 3.
            else:
                entry["functions"] = [{"function": "minecraft:enchant_randomly", "options": ANCIENT}]
        tables["chests/ancient_city"] = table

        # Existing Trial Chamber book pools are intentionally kept. The first
        # book remains the permanent unlock, so the unique Wind Burst book is
        # modestly less likely and Density/Breach are modestly more likely.
        for path in ("chests/trial_chambers/reward_rare", "chests/trial_chambers/reward_ominous_rare"):
            table = load(path)
            for entry in original_books(table):
                functions = entry["functions"]
                if any("wind_burst" in json.dumps(function) for function in functions):
                    entry["conditions"] = [{"condition": "minecraft:random_chance", "chance": 0.75}]
                if any("breach" in json.dumps(function) or "density" in json.dumps(function) for function in functions):
                    entry["weight"] = 3  # Vanilla: 2.
            tables[path] = table

        table = load("chests/bastion_other")
        for entry in original_books(table):
            entry["weight"] = 8  # Vanilla: 10.
        tables["chests/bastion_other"] = table

        table = load("gameplay/piglin_bartering")
        for entry in original_books(table):
            entry["weight"] = 4  # Vanilla: 5.
        tables["gameplay/piglin_bartering"] = table

        # Replace vanilla random books where WildWays gives those locations
        # particular themes. Normal books and enchanted equipment stay intact.
        themed = {
            "chests/abandoned_mineshaft": {
                "rolls": 1,
                "entries": [
                    book("minecraft:efficiency", 4),
                    book("minecraft:fortune"),
                    book("minecraft:silk_touch"),
                    {"type": "minecraft:empty", "weight": 14},
                ],
            },
            "chests/underwater_ruin_big": chance_book(TRIDENT, 1, 4),
            "chests/pillager_outpost": choice_book(
                ["minecraft:quick_charge", "minecraft:multishot", "minecraft:piercing"], 1, 10
            ),
            "chests/woodland_mansion": choice_book(
                ["minecraft:quick_charge", "minecraft:multishot", "minecraft:piercing"], 5, 100
            ),
            "chests/simple_dungeon": chance_book("minecraft:protection", 3, 10),
            "gameplay/fishing/treasure": chance_book(FISHING, 1, 6),
        }
        for path, pool in themed.items():
            table = load(path)
            remove_original_books(table)
            add(table, pool)
            tables[path] = table

        # Preserve vanilla treasure-fishing odds: the enchanted-book entry is
        # one of six equally weighted options, now restricted to fishing books.
        fishing = tables["gameplay/fishing/treasure"]
        fishing["pools"].pop()
        fishing["pools"][0]["entries"].append(book(FISHING))

        # Chest and archaeology sources that do not have vanilla book entries.
        additions = {
            "chests/shipwreck_treasure": [chance_book("#wildways:water_armor_books", 1, 3)],
            "chests/buried_treasure": [chance_book("#wildways:water_armor_books", 1, 3)],
            "chests/underwater_ruin_small": [chance_book(TRIDENT, 1, 4)],
            "chests/igloo_chest": [chance_book("minecraft:frost_walker", 1, 4)],
            "chests/village/village_snowy_house": [chance_book("minecraft:frost_walker", 1, 8)],
            "chests/village/village_weaponsmith": [chance_book("minecraft:sharpness", 1, 2)],
            "chests/woodland_mansion": [chance_book("minecraft:sharpness", 1, 4)],
            "chests/end_city_treasure": [chance_book("minecraft:mending", 1, 10)],
            "chests/nether_bridge": [chance_book("minecraft:fire_protection", 1, 5)],
            "chests/ruined_portal": [choice_book(["minecraft:fire_aspect", "minecraft:flame"], 1, 5)],
            "archaeology/desert_pyramid": [chance_book("minecraft:protection", 1, 8)],
            "archaeology/desert_well": [chance_book("minecraft:protection", 1, 8)],
            "archaeology/trail_ruins_common": [choice_book(["minecraft:unbreaking", "minecraft:efficiency"], 1, 8)],
            "archaeology/ocean_ruin_cold": [chance_book(TRIDENT, 1, 5)],
            "archaeology/ocean_ruin_warm": [chance_book(TRIDENT, 1, 5)],
            "entities/witch": [
                chance_book("minecraft:vanishing_curse", 1, 20),
                chance_book("minecraft:binding_curse", 1, 20),
            ],
            "entities/guardian": [chance_book("minecraft:thorns", 1, 20)],
            "entities/drowned": [chance_book("minecraft:impaling", 1, 20)],
            "entities/skeleton": [chance_book("minecraft:power", 1, 40)],
            "entities/stray": [chance_book("minecraft:power", 1, 20)],
            "entities/bogged": [chance_book("minecraft:power", 1, 20)],
        }
        for path, pools in additions.items():
            table = tables.get(path) or load(path)
            add(table, *pools)
            tables[path] = table

        # Keep the original hero gifts and add the planned recipe books as a
        # possible gift from the matching profession.
        for profession, enchantment in (
            ("toolsmith", "minecraft:unbreaking"),
            ("armorer", "minecraft:unbreaking"),
            ("weaponsmith", "minecraft:sharpness"),
        ):
            path = f"gameplay/hero_of_the_village/{profession}_gift"
            table = load(path)
            table["pools"][0]["entries"].append(book(enchantment))
            tables[path] = table

        for path, table in tables.items():
            output = OUTPUT / (path + ".json")
            output.parent.mkdir(parents=True, exist_ok=True)
            output.write_text(json.dumps(table, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")

        # Monster rooms get the same base chest, with one extra book tied to
        # their actual spawner. The mixin selects one of these tables.
        dungeon = tables["chests/simple_dungeon"]
        for mob, enchantment in (
            ("zombie", "minecraft:smite"),
            ("spider", "minecraft:bane_of_arthropods"),
            ("skeleton", "minecraft:power"),
        ):
            variant = json.loads(json.dumps(dungeon))
            add(variant, chance_book(enchantment, 1, 2))
            output = ROOT / f"src/main/resources/data/wildways/loot_table/chests/dungeon_{mob}.json"
            output.parent.mkdir(parents=True, exist_ok=True)
            output.write_text(json.dumps(variant, indent=2) + "\n", encoding="utf-8")

        print(f"Generated {len(tables)} vanilla loot-table overrides")


if __name__ == "__main__":
    main(Path(sys.argv[1]))
