"""Generate the Minecraft 26.3 loot-table overrides for WildWays book sources.

Run with the Minecraft common jar as the first argument. The checked-in JSON is
the runtime data; this script keeps vanilla's unrelated entries intact.
"""

import json
import sys
import zipfile
from fractions import Fraction
from math import lcm
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
        "modifier": {"type": "minecraft:enchant_randomly", "options": enchantment},
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
    modifier = entry.get("modifier", {})
    return entry.get("name") == "minecraft:book" and isinstance(modifier, dict) and modifier.get("type") in (
        "minecraft:enchant_randomly", "minecraft:enchant_with_levels", "minecraft:set_enchantments"
    )


def original_books(table):
    return [entry for pool in table["pools"] for entry in pool["entries"] if is_enchanted_book(entry)]


def remove_original_books(table):
    for pool in table["pools"]:
        pool["entries"] = [entry for entry in pool["entries"] if not is_enchanted_book(entry)]


def add(table, *pools):
    table["pools"].extend(pools)


def add_archaeology_finds(table, finds):
    # A brushable block delivers one stack. Give the new finds their exact
    # chances in the same roll and divide the rest in vanilla's proportions.
    entries = table["pools"][0]["entries"]
    vanilla_weight = sum(entry.get("weight", 1) for entry in entries)
    vanilla_chance = 1 - sum(chance for _, chance in finds)
    chance_per_weight = vanilla_chance / vanilla_weight
    total_weight = lcm(chance_per_weight.denominator, *(chance.denominator for _, chance in finds))
    weight_multiplier = int(chance_per_weight * total_weight)
    for entry in entries:
        entry["weight"] = entry.get("weight", 1) * weight_multiplier
    entries.extend({**entry, "weight": int(chance * total_weight)} for entry, chance in finds)


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
                entry["modifier"] = {"type": "minecraft:enchant_randomly", "options": GENERAL}
            tables[path] = table

        table = load("chests/ancient_city")
        for entry in original_books(table):
            option = entry["modifier"].get("options")
            if option == "minecraft:swift_sneak":
                entry["weight"] = 2  # Vanilla: 3.
            else:
                entry["modifier"] = {"type": "minecraft:enchant_randomly", "options": ANCIENT}
        tables["chests/ancient_city"] = table

        # Existing Trial Chamber book pools are intentionally kept. The first
        # book remains the permanent unlock, so the unique Wind Burst book is
        # modestly less likely and Density/Breach are modestly more likely.
        for path in ("chests/trial_chambers/reward_rare", "chests/trial_chambers/reward_ominous_rare"):
            table = load(path)
            for entry in original_books(table):
                modifier = json.dumps(entry["modifier"])
                if "wind_burst" in modifier:
                    entry["condition"] = {"type": "minecraft:random_chance", "chance": 0.75}
                if "breach" in modifier or "density" in modifier:
                    entry["weight"] = 3  # Vanilla: 2.
            add(table, chance_book("#minecraft:on_random_loot", 1, 10))
            tables[path] = table

        table = load("chests/bastion_other")
        for entry in original_books(table):
            entry["weight"] = 8  # Vanilla: 10.
        add(table, {"rolls": 1, "entries": [
            {"type": "minecraft:item", "name": "wildways:volcanite", "modifier": {
                "type": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 2}
            }},
            {"type": "minecraft:empty", "weight": 19},
        ]})
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
                    book("minecraft:efficiency", 3),
                    book("minecraft:fortune"),
                    book("minecraft:silk_touch"),
                    {"type": "minecraft:empty", "weight": 15},
                ],
            },
            "chests/underwater_ruin_big": chance_book(TRIDENT, 1, 4),
            "chests/pillager_outpost": choice_book(
                ["minecraft:quick_charge", "minecraft:multishot", "minecraft:piercing"], 1, 10
            ),
            "chests/woodland_mansion": choice_book(
                ["minecraft:quick_charge", "minecraft:multishot", "minecraft:piercing"], 5, 100
            ),
            "chests/simple_dungeon": chance_book("minecraft:protection", 1, 5),
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

        # Additional chest and mob book drops.
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

        parched = load("entities/parched")
        add(parched, {
            "rolls": 1,
            "condition": {
                "type": "minecraft:all_of",
                "terms": [
                    {"type": "minecraft:killed_by_player"},
                    {
                        "type": "minecraft:random_chance_with_enchanted_bonus",
                        "enchantment": "minecraft:looting",
                        "unenchanted_chance": 0.025,
                        "enchanted_chance": {
                            "type": "minecraft:linear",
                            "base": 0.035,
                            "per_level_above_first": 0.01,
                        },
                    },
                ],
            },
            "entries": [book("minecraft:power")],
        })
        tables["entities/parched"] = parched

        archaeology = {
            "desert_pyramid": [(book("minecraft:protection"), Fraction(1, 10))],
            "desert_well": [(book("minecraft:protection"), Fraction(1, 10))],
            "trail_ruins_common": [
                ({"type": "minecraft:item", "name": "wildways:lost_eye"}, Fraction(2, 47)),
                (book("minecraft:unbreaking"), Fraction(1, 10)),
                (book("minecraft:efficiency"), Fraction(1, 10)),
            ],
            "ocean_ruin_cold": [(book(TRIDENT), Fraction(3, 20))],
            "ocean_ruin_warm": [(book(TRIDENT), Fraction(3, 20))],
        }
        for site, finds in archaeology.items():
            path = f"archaeology/{site}"
            table = load(path)
            add_archaeology_finds(table, finds)
            tables[path] = table

        # Keep the eye drops with these bundled overrides.
        add(tables["chests/igloo_chest"], {"rolls": 1, "entries": [
            {"type": "minecraft:item", "name": "wildways:eye_of_ice"},
        ]})
        add(tables["chests/village/village_snowy_house"], {"rolls": 1, "entries": [
            {"type": "minecraft:item", "name": "wildways:eye_of_ice"},
            {"type": "minecraft:empty", "weight": 9},
        ]})

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
            add(variant, chance_book(enchantment, 1, 5))
            output = ROOT / f"src/main/resources/data/wildways/loot_table/chests/dungeon_{mob}.json"
            output.parent.mkdir(parents=True, exist_ok=True)
            output.write_text(json.dumps(variant, indent=2) + "\n", encoding="utf-8")

        print(f"Generated {len(tables)} vanilla loot-table overrides")


if __name__ == "__main__":
    main(Path(sys.argv[1]))
