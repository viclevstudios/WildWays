"""Generate Wildways' small, palette-limited Volcanite texture study.

Run with a Python installation containing Pillow after Loom has downloaded the
Minecraft 26.2 client JAR. Vanilla diamond sprites provide familiar silhouettes;
their pixels are recolored and given a few hand-placed ember accents.
"""

import json
from io import BytesIO
from pathlib import Path
from zipfile import ZipFile

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/wildways/textures"
CLIENT_JAR = Path.home() / ".gradle/caches/fabric-loom/26.2/minecraft-client.jar"

PALETTE = {
    ".": (0, 0, 0, 0),
    "0": "#1d1e23", "1": "#292b31", "2": "#35383e",
    "3": "#44474c", "4": "#55585d", "5": "#6b6e70",
    "6": "#858680", "7": "#aaa397", "8": "#c7b49b",
    "a": "#894425", "b": "#b9662f", "c": "#da9250",
    "d": "#ecc078",
    "e": "#4e171d", "f": "#711f25", "g": "#992b32",
    "h": "#c23d41",
}

RAW = [
    "................", "................", "........06......", "......635020....",
    ".....246100e0...", "...00520100g0...", "..031gh12400....", "..400ffe00411...",
    "..2000ehe20140..", "...0334ehe1000..", "...5300h00e10...", "...0212013f0....",
    "....030030......", ".....00.........", "................", "................",
]

INGOT = [
    "................", "................", "................", ".....78887......",
    "...778887877....", "..77666666677...", ".7666666666667..", ".65555555555568.",
    ".5444444444445c.", "..43333333334b..", "..33bbbbb333b...", "...2bbbbb22a....",
    "....2222222.....", "................", "................", "................",
]


def make_sprite(rows):
    assert len(rows) == 16 and all(len(row) == 16 for row in rows)
    image = Image.new("RGBA", (16, 16))
    for y, row in enumerate(rows):
        for x, mark in enumerate(row):
            color = PALETTE[mark]
            if isinstance(color, str):
                color = tuple(bytes.fromhex(color[1:])) + (255,)
            image.putpixel((x, y), color)
    return image


def ore_side():
    image = Image.new("RGBA", (16, 16))
    columns = [2, 3, 4, 4, 3, 2, 1, 2, 3, 4, 5, 4, 3, 2, 2, 3]
    for y in range(16):
        for x in range(16):
            shade = columns[x] + ((x * 3 + y * 7) % 5 == 0) - ((x + y * 3) % 7 == 0)
            if y in (5, 11) and x % 5 != 0:
                shade -= 1
            image.putpixel((x, y), rgb(PALETTE[str(max(0, min(6, shade)))]))
    for x, y, mark in [
        (3, 2, "a"), (4, 2, "b"), (4, 3, "c"), (3, 4, "b"),
        (10, 5, "a"), (11, 5, "b"), (11, 6, "d"), (12, 6, "b"),
        (7, 10, "a"), (8, 10, "c"), (8, 11, "b"), (9, 11, "a"),
        (2, 13, "a"), (3, 13, "b"), (13, 12, "b"), (13, 13, "c"),
    ]:
        image.putpixel((x, y), rgb(PALETTE[mark]))
    return image


def ore_top():
    image = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            ring = min(x, y, 15 - x, 15 - y)
            shade = (2, 4, 3, 5, 3, 2, 4, 1)[ring]
            shade += (x * 5 + y * 3) % 9 == 0
            image.putpixel((x, y), rgb(PALETTE[str(shade)]))
    for x, y, mark in [
        (4, 5, "a"), (5, 5, "b"), (5, 6, "c"), (6, 6, "a"),
        (10, 9, "a"), (11, 9, "c"), (10, 10, "b"), (11, 10, "d"),
        (8, 3, "b"), (9, 3, "a"),
    ]:
        image.putpixel((x, y), rgb(PALETTE[mark]))
    return image


def rgb(value):
    return tuple(bytes.fromhex(value[1:])) + (255,)


METAL = {
    (8, 37, 32): "0", (14, 63, 54): "2", (21, 99, 85): "3",
    (30, 138, 119): "4", (39, 178, 154): "5", (43, 199, 172): "6",
    (51, 235, 203): "7", (164, 253, 240): "8",
    (26, 170, 167): "4", (32, 197, 181): "5", (74, 237, 217): "6",
    (161, 251, 232): "7", (255, 255, 255): "8",
}
WOOD = {
    (40, 30, 11): "0", (73, 54, 21): "1", (104, 78, 30): "2",
    (137, 103, 39): "a",
}


def recolor(source, name, worn=False):
    source = source.convert("RGBA")
    output = Image.new("RGBA", source.size)
    for y in range(source.height):
        for x in range(source.width):
            r, g, b, a = source.getpixel((x, y))
            if not a:
                continue
            mark = METAL.get((r, g, b), WOOD.get((r, g, b)))
            if mark is None:
                # Worn diamond armor has a slightly different cyan palette.
                light = round((r + g + b) / 3)
                mark = ("0", "2", "3", "4", "5", "6", "7", "8")[min(7, light // 28)]
            output.putpixel((x, y), rgb(PALETTE[mark])[:3] + (a,))
    if worn:
        return output

    accents = {
        "volcanite_sword": [(12, 2), (11, 3), (10, 4), (9, 5), (8, 6)],
        "volcanite_pickaxe": [(7, 2), (8, 2), (11, 3), (12, 3)],
        "volcanite_axe": [(10, 2), (11, 3), (12, 5)],
        "volcanite_shovel": [(12, 3), (11, 4)],
        "volcanite_hoe": [(8, 2), (10, 3)],
        "volcanite_helmet": [(6, 4), (9, 4), (5, 10), (10, 10)],
        "volcanite_chestplate": [(3, 5), (12, 5), (7, 8), (8, 8)],
        "volcanite_leggings": [(5, 4), (10, 4), (4, 12), (11, 12)],
        "volcanite_boots": [(4, 11), (11, 11), (3, 12), (12, 12)],
    }[name]
    for x, y in accents:
        if output.getpixel((x, y))[3]:
            output.putpixel((x, y), rgb(PALETTE["c" if (x + y) % 3 == 0 else "b"]))
    return output


def save(image, relative):
    path = ASSETS / relative
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path)
    print(path.relative_to(ROOT))


def main():
    save(ore_side(), "block/volcanite_ore_side.png")
    save(ore_top(), "block/volcanite_ore_top.png")
    save(make_sprite(RAW), "item/raw_volcanite.png")
    # Keep the forged bar darker than the armor's polished highlights.
    ingot_rows = [row.translate(str.maketrans("876543", "654321")) for row in INGOT]
    save(make_sprite(ingot_rows), "item/volcanite.png")
    with ZipFile(CLIENT_JAR) as jar:
        for name in [
            "sword", "pickaxe", "axe", "shovel", "hoe",
            "helmet", "chestplate", "leggings", "boots",
        ]:
            vanilla = Image.open(BytesIO(jar.read(f"assets/minecraft/textures/item/diamond_{name}.png")))
            save(recolor(vanilla, f"volcanite_{name}"), f"item/volcanite_{name}.png")
        for layer in ("humanoid", "humanoid_baby", "humanoid_leggings"):
            vanilla = Image.open(BytesIO(jar.read(f"assets/minecraft/textures/entity/equipment/{layer}/diamond.png")))
            save(recolor(vanilla, layer, worn=True), f"entity/equipment/{layer}/volcanite.png")

    for name in [
        "raw_volcanite", "volcanite", "volcanite_sword", "volcanite_pickaxe",
        "volcanite_axe", "volcanite_shovel", "volcanite_hoe",
        "volcanite_helmet", "volcanite_chestplate", "volcanite_leggings", "volcanite_boots",
    ]:
        model_path = ROOT / f"src/main/resources/assets/wildways/models/item/{name}.json"
        model = json.loads(model_path.read_text(encoding="utf-8"))
        model["textures"]["layer0"] = f"wildways:item/{name}"
        model_path.write_text(json.dumps(model, indent=2) + "\n", encoding="utf-8")

    names = [
        ("Ore side", "block/volcanite_ore_side.png"),
        ("Ore top", "block/volcanite_ore_top.png"),
        ("Raw", "item/raw_volcanite.png"),
        ("Ingot", "item/volcanite.png"),
        ("Sword", "item/volcanite_sword.png"),
        ("Pickaxe", "item/volcanite_pickaxe.png"),
        ("Axe", "item/volcanite_axe.png"),
        ("Shovel", "item/volcanite_shovel.png"),
        ("Hoe", "item/volcanite_hoe.png"),
        ("Helmet", "item/volcanite_helmet.png"),
        ("Chestplate", "item/volcanite_chestplate.png"),
        ("Leggings", "item/volcanite_leggings.png"),
        ("Boots", "item/volcanite_boots.png"),
    ]
    preview = Image.new("RGB", (5 * 152, 3 * 170), "#202126")
    draw = ImageDraw.Draw(preview)
    for index, (label, relative) in enumerate(names):
        x, y = (index % 5) * 152, (index // 5) * 170
        sprite = Image.open(ASSETS / relative).convert("RGBA")
        preview.paste(sprite.resize((128, 128), Image.Resampling.NEAREST), (x + 12, y + 8),
                      sprite.resize((128, 128), Image.Resampling.NEAREST))
        draw.text((x + 12, y + 142), label, fill="#e8ded0")
    destination = ROOT / "docs/volcanite-texture-preview.png"
    preview.save(destination)
    print(destination.relative_to(ROOT))

    raw_preview = Image.new("RGBA", (16, 16), "#202126")
    raw_preview.alpha_composite(Image.open(ASSETS / "item/raw_volcanite.png").convert("RGBA"))
    raw_destination = ROOT / "docs/raw-volcanite-preview.png"
    raw_preview.resize((256, 256), Image.Resampling.NEAREST).save(raw_destination)
    print(raw_destination.relative_to(ROOT))


if __name__ == "__main__":
    main()
