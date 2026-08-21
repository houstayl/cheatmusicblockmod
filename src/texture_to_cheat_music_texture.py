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

def blockstates_json(name, blockstates_dir, i):
    blockstates_string = f"""
{{
    "variants": {{
        "type=0":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_a" }},
        "type=1":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_b" }},
        "type=2":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_c" }},
        "type=3":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_d" }},
        "type=4":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_e" }},
        "type=5":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_f" }},
        "type=6":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_g" }},
        "type=7":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_a_double_flat" }},
        "type=8":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_b_double_flat" }},
        "type=9":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_c_double_flat" }},
        "type=10":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_d_double_flat" }},
        "type=11":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_e_double_flat" }},
        "type=12":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_f_double_flat" }},
        "type=13":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_g_double_flat" }},
        "type=14":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_a_flat" }},
        "type=15":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_b_flat" }},
        "type=16":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_c_flat" }},
        "type=17":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_d_flat" }},
        "type=18":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_e_flat" }},
        "type=19":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_f_flat" }},
        "type=20":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_g_flat" }},
        "type=21":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_a_sharp" }},
        "type=22":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_b_sharp" }},
        "type=23":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_c_sharp" }},
        "type=24":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_d_sharp" }},
        "type=25":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_e_sharp" }},
        "type=26":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_f_sharp" }},
        "type=27":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_g_sharp" }},
        "type=28":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_a_double_sharp" }},
        "type=29":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_b_double_sharp" }},
        "type=30":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_c_double_sharp" }},
        "type=31":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_d_double_sharp" }},
        "type=32":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_e_double_sharp" }},
        "type=33":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_f_double_sharp" }},
        "type=34":  {{ "model": "cheatmusicblockmod:block/cheat_music_block_{name}_g_double_sharp" }}
  }}
}}
"""

    with open(os.path.join(blockstates_dir, f"cheat_music_block_{name}{i}.json"), "w", encoding="utf-8") as file:
        file.write(blockstates_string)




    
def block_json(name, letter_and_accidental, block_type, block_dir):
    block_string = f"""
{{
  "parent": "minecraft:block/{block_type}",
  "textures": {{
    "all": "cheatmusicblockmod:block/cheat_music_block_{name}_{letter_and_accidental}"
  }}
}}
"""
    with open(os.path.join(block_dir, f"cheat_music_block_{name}_{letter_and_accidental}.json"), "w", encoding="utf-8") as file:
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
    parser.add_argument("image_path", type=Path, help="Path to the primary .png image")
    parser.add_argument("block_type", type=str, help="Block type")
    



    args = parser.parse_args()

    file_dir = Path(__file__).resolve().parent
    colors_dir = file_dir / "colors"
    output_dir = file_dir / args.name
    textures_dir = output_dir / "textures.block"
    blockstates_dir = output_dir / "blockstates"
    block_dir = output_dir / "models.block"
    item_dir = output_dir / "models.item"



    # Validate inputs
    if not args.image_path.is_file() or args.image_path.suffix.lower() != ".png":
        print(f"Error: '{args.image_path}' must be a valid .png file", file=sys.stderr)
        sys.exit(1)

    textures_dir.mkdir(parents=True, exist_ok=True)
    colorize_image(args.image_path, colors_dir, textures_dir, args.name)

    blockstates_dir.mkdir(parents=True, exist_ok=True)
    block_dir.mkdir(parents=True, exist_ok=True)
    item_dir.mkdir(parents=True, exist_ok=True)
    types = ["a", "b", "c", "d", "e", "f", "g",
            "a_double_flat", "b_double_flat", "c_double_flat", "d_double_flat", "e_double_flat", "f_double_flat", "g_double_flat",
            "a_flat", "b_flat", "c_flat", "d_flat", "e_flat", "f_flat", "g_flat",
            "a_sharp", "b_sharp", "c_sharp", "d_sharp", "e_sharp", "f_sharp", "g_sharp",
            "a_double_sharp", "b_double_sharp", "c_double_sharp", "d_double_sharp", "e_double_sharp", "f_double_sharp", "g_double_sharp"]
    for i in range(7):
        blockstates_json(args.name, blockstates_dir, i)
    for t in types:
        block_json(args.name, t, args.block_type, block_dir)
    for i in range(7):
        letter = types[i]
        item_json(args.name, letter, item_dir, i)


if __name__ == "__main__":
    main()
