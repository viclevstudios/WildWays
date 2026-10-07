"""Write the component-aware enchanted-book crafting recipes from the plan."""

import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "src/main/resources/data/wildways/recipe"
ADVANCEMENTS = ROOT / "src/main/resources/data/wildways/advancement/recipes/misc"
L = "minecraft:lapis_lazuli"
B = "minecraft:blaze_powder"
W = "minecraft:wind_charge"
E = "minecraft:experience_bottle"
I = "minecraft:iron_ingot"


def source_book(enchantment):
    return {
        "fabric:type": "fabric:components",
        "base": "minecraft:enchanted_book",
        "components": {
            "minecraft:stored_enchantments": {f"minecraft:{enchantment}": 1},
        },
    }


def result(enchantment):
    return {
        "id": "minecraft:enchanted_book",
        "count": 1,
        "components": {
            "minecraft:stored_enchantments": {f"minecraft:{enchantment}": 1},
        },
    }


def shaped(name, source, pattern, ingredients):
    key = {letter: (source_book(source) if item == "source" else item) for letter, item in ingredients.items()}
    return name, {
        "type": "minecraft:crafting_shaped",
        "category": "misc",
        "pattern": pattern,
        "key": key,
        "result": result(name),
    }


def shapeless(name, source, ingredients):
    return name, {
        "type": "minecraft:crafting_shapeless",
        "category": "misc",
        "ingredients": [source_book(source), *ingredients],
        "result": result(name),
    }


RECIPES = [
    shaped("mending", "unbreaking", ["LGL", "VUV", "LEL"], {
        "L": L, "G": "minecraft:ghast_tear", "V": "wildways:volcanite", "U": "source", "E": E,
    }),
    shaped("fire_protection", "protection", ["LBL", "BPB", "LBL"], {
        "L": L, "B": B, "P": "source",
    }),
    shaped("blast_protection", "protection", ["LOL", "IPI", "LIL"], {
        "L": L, "O": "minecraft:obsidian", "I": I, "P": "source",
    }),
    shaped("projectile_protection", "protection", ["LSL", "IPI", "LIL"], {
        "L": L, "S": "minecraft:shield", "I": I, "P": "source",
    }),
    shaped("feather_falling", "protection", ["LML", "FPF", "LML"], {
        "L": L, "M": "minecraft:phantom_membrane", "F": "minecraft:feather", "P": "source",
    }),
    shaped("knockback", "sharpness", ["LWL", "WSW", "LEL"], {
        "L": L, "W": W, "S": "source", "E": E,
    }),
    shaped("fire_aspect", "sharpness", ["LBL", "BSB", "LEL"], {
        "L": L, "B": B, "S": "source", "E": E,
    }),
    shapeless("sweeping_edge", "sharpness", ["minecraft:iron_sword", W, L]),
    shaped("punch", "power", ["LWL", "WPW", "LEL"], {
        "L": L, "W": W, "P": "source", "E": E,
    }),
    shaped("flame", "power", ["LBL", "BPB", "LBL"], {
        "L": L, "B": B, "P": "source",
    }),
    shaped("riptide", "impaling", ["LWL", "WIW", "LNL"], {
        "L": L, "W": W, "I": "source", "N": "minecraft:nautilus_shell",
    }),
]


def main():
    OUTPUT.mkdir(parents=True, exist_ok=True)
    ADVANCEMENTS.mkdir(parents=True, exist_ok=True)
    for name, recipe in RECIPES:
        (OUTPUT / f"book_{name}.json").write_text(
            json.dumps(recipe, indent=2, ensure_ascii=False) + "\n", encoding="utf-8"
        )
        ingredients = (recipe["key"].values() if "key" in recipe else recipe["ingredients"])
        item_ids = sorted({item["base"] if isinstance(item, dict) else item for item in ingredients})
        advancement = {
            "parent": "minecraft:recipes/root",
            "criteria": {
                "has_ingredients": {
                    "trigger": "minecraft:inventory_changed",
                    "conditions": {"items": [{"items": [item]} for item in item_ids]},
                }
            },
            "requirements": [["has_ingredients"]],
            "rewards": {"recipes": [f"wildways:book_{name}"]},
        }
        (ADVANCEMENTS / f"book_{name}.json").write_text(
            json.dumps(advancement, indent=2) + "\n", encoding="utf-8"
        )
    print(f"Generated {len(RECIPES)} enchanted-book recipes")


if __name__ == "__main__":
    main()
