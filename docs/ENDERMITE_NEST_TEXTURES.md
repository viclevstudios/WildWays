# Endermite Nest textures

The nest model is 12 x 12 x 12 model units. Edit these PNGs in
`src/main/resources/assets/wildways/textures/block/`:

| File | Editable dimensions | Used on |
| --- | --- | --- |
| `endermite_nest_top.png` | 12 x 12 pixels | Top while closed |
| `endermite_nest_top_open.png` | 12 x 12 pixels | Top while open |
| `endermite_nest_side.png` | 12 x 12 pixels | All four sides and the bottom, open or closed |

Each file is a single static square PNG covering an entire model face. Draw on
the full 12 x 12 canvas without padding or an animation strip; no `.png.mcmeta`
is needed. The side texture is shared by all four sides and the bottom, so the
current model does not support a separate front or bottom design. Align edge
colors and patterns between the top and sides if they should join seamlessly.
The open top should use the same outer rim as the closed top, with the opening
drawn inside it; the model itself remains a cube.

`processResources` automatically enlarges these textures by exactly 4x with
nearest-neighbour sampling. The built
mod uses 48 x 48 images, preserving the authored pixels and allowing mipmap level
4. Loading raw 12 x 12 sprites would limit the entire block atlas to level 2.
The model's UV coordinates remain 0..16 to display each complete image.

After editing, run `.\gradlew.bat build` (or relaunch `runClient`). In game, give
yourself `/give @s wildways:endermite_box`, place it beside a normal block, and
compare the pixel density. Check all sides, the bottom, the closed and open top,
and verify that the sides stay static. With mipmaps set to 4, check the client log
for any nest-related mip-level reduction warnings.
