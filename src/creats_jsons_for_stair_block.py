import argparse
from pathlib import Path
import sys
from PIL import Image, ImageOps, ImageChops
import cv2
import numpy as np
import json
import os



def apply_texture(gray_path: str, color_path: str):
    gray_orig = Image.open(gray_path)
    color_img = Image.open(color_path).convert("RGBA")

    if gray_orig.size != color_img.size:
        gray_orig = gray_orig.resize(color_img.size, Image.Resampling.LANCZOS)

    # Extract alpha mask from gray image
    if "A" in gray_orig.getbands():
        gray_alpha = gray_orig.getchannel("A")
    else:
        gray_alpha = Image.new("L", gray_orig.size, 255)

    # Explicitly convert to 'L' mode, then expand to RGB for channel multiplication
    gray_l_rgb = gray_orig.convert("L").convert("RGB")

    color_rgb = color_img.convert("RGB")
    color_alpha = color_img.getchannel("A")

    # Multiply color RGB by grayscale texture RGB
    textured_rgb = ImageChops.multiply(color_rgb, gray_l_rgb)

    # Combine alpha channels
    final_alpha = ImageChops.multiply(color_alpha, gray_alpha)

    # Merge into RGBA
    result = Image.merge("RGBA", (*textured_rgb.split(), final_alpha))
    return result


def colorize_image(image_path: Path, colors_dir: Path, output_dir: Path, name):
    # Load image and convert to grayscale ('L' mode)
    #base_img = Image.open(image_path)
    #gray_img = base_img.convert("L")

    # Extract alpha channel if the original image has transparency
    #alpha = base_img.split()[-1] if "A" in base_img.getbands() else None

    # Locate all PNG color swatches in the directory
    color_files = sorted(list(colors_dir.glob("*.png")))
    if not color_files:
        print(f"Error: No .png files found in directory '{colors_dir}'", file=sys.stderr)
        sys.exit(1)


    print(f"Processing {len(color_files)} colors...")
    for color_file in color_files:
        #tinted_img = apply_luminance_texture(str(color_file), str(image_path), alpha=.7)
        tinted_img = apply_texture(str(image_path), str(color_file))

        # Save result
        output_filename = f"cheat_music_block_{name}_{color_file.stem}.png"
        output_path = output_dir / output_filename
        tinted_img.save(output_path)
        #cv2.imwrite(str(output_path), tinted_img)
        print(f"Saved: {output_path}")

def blockstates_json(base_block_name, blockstates_dir, i, types, stair_block_name):
    faces = ["east", "south", "west", "north"]
    halfs = ["bottom", "top"]
    shapes = ["straight", "outer_left", "outer_right", "inner_left", "inner_right"]

    blockstates_string = f"""
            {{
                "variants": {{
                """
    for k in range(len(faces)):
        for half in halfs:
            for shape in shapes:
                for j in range(len(types)):
                    face = faces[k]
                    base_angle = k * 90
                    shape_offset = 0
                    if shape == "inner_left" or shape == "outer_left":
                        if half == "bottom":
                            shape_offset = 270
                        if half == "top":
                            shape_offset = 90

                    y_value = (k * 90 + shape_offset) % 360
                    x = 0
                    if half == "top":
                        x = 180
                    stair_type = ""
                    if shape == "outer_left" or shape =="outer_right":
                        stair_type = "_outer"
                    if shape == "inner_left" or shape =="inner_right":
                        stair_type = "_inner"
                    #facing=east,half=top,shape=inner_left
                    #facing=east,half=top,shape=inner_right
                    if x != 0 and y_value != 0:
                        blockstates_string = blockstates_string + f'"facing={face},half={half},shape={shape},type={j}":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{stair_block_name}_{types[j]}{stair_type}", "x": 180, "y": {y_value}, "uvlock": true }},\n'
                    elif y_value != 0:
                        blockstates_string = blockstates_string + f'"facing={face},half={half},shape={shape},type={j}":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{stair_block_name}_{types[j]}{stair_type}", "y": {y_value}, "uvlock": true }},\n'
                    elif x != 0:
                        blockstates_string = blockstates_string + f'"facing={face},half={half},shape={shape},type={j}":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{stair_block_name}_{types[j]}{stair_type}", "x": 180, "uvlock": true }},\n'
                    else:
                        blockstates_string = blockstates_string + f'"facing={face},half={half},shape={shape},type={j}":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{stair_block_name}_{types[j]}{stair_type}", "uvlock": true }},\n'

                    #if half == "bottom":
                    #    blockstates_string = blockstates_string + f'"facing={face},half={half},shape={shape},type={j}":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{base_block_name}_{types[j]}", "y": {y_value}, "uvlock": true }},\n'
                    #else:
                    #    blockstates_string = blockstates_string + f'"facing={face},half={half},shape={shape},type={j}":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{base_block_name}_{types[j]}", "x": 180, "y": {y_value}, "uvlock": true }},\n'
    blockstates_string = blockstates_string[:-2] + "\n"#delete ,

    blockstates_string = blockstates_string +   f"""}}
                                              }}"""



    with open(os.path.join(blockstates_dir, f"cheat_music_block_{stair_block_name}{i}.json"), "w", encoding="utf-8") as file:
        file.write(blockstates_string)




    
def block_json(base_block_name, letter_and_accidental, block_dir, stair_block_name):
    block_string = f"""
{{
  "parent": "minecraft:block/stairs",
  "textures": {{
    "bottom": "cheatmusicblockmod:block/cheat_music_block_{base_block_name}_{letter_and_accidental}",
    "top": "cheatmusicblockmod:block/cheat_music_block_{base_block_name}_{letter_and_accidental}",
    "side": "cheatmusicblockmod:block/cheat_music_block_{base_block_name}_{letter_and_accidental}"
  }}
}}
"""
    with open(os.path.join(block_dir, f"cheat_music_block_{stair_block_name}_{letter_and_accidental}.json"), "w", encoding="utf-8") as file:
        file.write(block_string)

def block_json_inner(base_block_name, letter_and_accidental, block_dir, stair_block_name):
    block_string = f"""
{{
  "parent": "minecraft:block/inner_stairs",
  "textures": {{
    "bottom": "cheatmusicblockmod:block/cheat_music_block_{base_block_name}_{letter_and_accidental}",
    "top": "cheatmusicblockmod:block/cheat_music_block_{base_block_name}_{letter_and_accidental}",
    "side": "cheatmusicblockmod:block/cheat_music_block_{base_block_name}_{letter_and_accidental}"
  }}
}}
"""
    with open(os.path.join(block_dir, f"cheat_music_block_{stair_block_name}_{letter_and_accidental}_inner.json"), "w", encoding="utf-8") as file:
        file.write(block_string)

def block_json_outer(base_block_name, letter_and_accidental, block_dir, stair_block_name):
    block_string = f"""
{{
  "parent": "minecraft:block/outer_stairs",
  "textures": {{
    "bottom": "cheatmusicblockmod:block/cheat_music_block_{base_block_name}_{letter_and_accidental}",
    "top": "cheatmusicblockmod:block/cheat_music_block_{base_block_name}_{letter_and_accidental}",
    "side": "cheatmusicblockmod:block/cheat_music_block_{base_block_name}_{letter_and_accidental}"
  }}
}}
"""
    with open(os.path.join(block_dir, f"cheat_music_block_{stair_block_name}_{letter_and_accidental}_outer.json"), "w", encoding="utf-8") as file:
        file.write(block_string)





    
def item_json(name, letter, item_dir, i):
    item_string = f"""
{{
  "parent": "cheatmusicblockmod:block/cheat_music_block_{name}_{letter}"
}}
"""
    with open(os.path.join(item_dir, f"cheat_music_block_{name}{i}.json"), "w", encoding="utf-8") as file:
        file.write(item_string)



def main():
    parser = argparse.ArgumentParser(
        description="Convert a PNG to grayscale and apply color tints from a directory of solid-color PNGs."
    )
    parser.add_argument("name", type=str, help="The target name")
    parser.add_argument("base_block_name", type=Path, help="name of the base block for textures")

    #arguments are name of block, path to block texture



    args = parser.parse_args()

    file_dir = Path(__file__).resolve().parent
    colors_dir = file_dir / "colors"
    output_dir = file_dir / args.name
    textures_dir = output_dir / "textures.block"
    blockstates_dir = output_dir / "blockstates"
    block_dir = output_dir / "models.block"
    item_dir = output_dir / "models.item"


    #not making textures for stair blocks, using base block textures
    #textures_dir.mkdir(parents=True, exist_ok=True)
    #colorize_image(args.image_path, colors_dir, textures_dir, args.name)

    blockstates_dir.mkdir(parents=True, exist_ok=True)
    block_dir.mkdir(parents=True, exist_ok=True)
    item_dir.mkdir(parents=True, exist_ok=True)
    types = ["a", "b", "c", "d", "e", "f", "g",
            "a_double_flat", "b_double_flat", "c_double_flat", "d_double_flat", "e_double_flat", "f_double_flat", "g_double_flat",
            "a_flat", "b_flat", "c_flat", "d_flat", "e_flat", "f_flat", "g_flat",
            "a_sharp", "b_sharp", "c_sharp", "d_sharp", "e_sharp", "f_sharp", "g_sharp",
            "a_double_sharp", "b_double_sharp", "c_double_sharp", "d_double_sharp", "e_double_sharp", "f_double_sharp", "g_double_sharp"]
    for i in range(7):
        blockstates_json(args.base_block_name, blockstates_dir, i, types, args.name)
    for t in types:
        block_json(args.base_block_name, t, block_dir, args.name)
        block_json_inner(args.base_block_name, t, block_dir, args.name)
        block_json_outer(args.base_block_name, t, block_dir, args.name)
    for i in range(7):
        letter = types[i]
        item_json(args.name, letter, item_dir, i)


if __name__ == "__main__":
    main()
