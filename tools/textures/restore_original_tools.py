"""Restore IC2R tool silhouettes and the detailed GraviSuite Vajra remaster.

Usage: python tools/textures/restore_original_tools.py [--ic2r ../IC2R] [--check]
Requires Pillow. IC2R grip recolors preserve alpha; Vajra uses the reviewed native 32px remaster.
"""
from pathlib import Path
import argparse
import colorsys
import hashlib
import json
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
TARGET = ROOT / "src/main/resources/assets/ic2r_addons/textures/item"
TOOLS = {
    "advanced_diamond_drill": "diamond_drill",
    "advanced_iridium_drill": "iridium_drill",
    "advanced_chainsaw": "chainsaw",
    "gravitool/wrench": "electric_wrench",
    "gravitool/hoe": "electric_hoe",
    "gravitool/treetap": "electric_treetap",
}


def blue_grip(source, drill):
    output = source.copy()
    changed = 0
    for y in range(source.height):
        for x in range(source.width):
            r, g, b, a = source.getpixel((x, y))
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            # Restrict the orange palette to the grip. Treetap wood and drill tips stay intact.
            grip = y >= 12 and x <= 20 if drill else y >= 16 and x >= 14
            if a and grip and .06 <= h <= .17 and s > .45 and v > .23:
                blue = tuple(round(c * 255) for c in colorsys.hsv_to_rgb(210 / 360, s, v))
                output.putpixel((x, y), (*blue, a))
                changed += 1
    assert changed > 0
    assert source.getchannel("A").tobytes() == output.getchannel("A").tobytes()
    return output, changed


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--ic2r", type=Path, default=ROOT.parent / "IC2R")
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    records = []
    for name, native in TOOLS.items():
        source_path = args.ic2r / f"src/main/resources/assets/ic2/textures/item/tool/electric/{native}.png"
        source = Image.open(source_path).convert("RGBA")
        assert source.size == (32, 32)
        output, changed = blue_grip(source, "drill" in native or native == "chainsaw")
        target = TARGET / f"{name}.png"
        if args.check:
            assert Image.open(target).convert("RGBA").tobytes() == output.tobytes(), target
        else:
            output.save(target)
        records.append({"item": name, "source": f"ic2:item/tool/electric/{native}",
                        "source_sha256": hashlib.sha256(source_path.read_bytes()).hexdigest(),
                        "recolored_pixels": changed, "alpha_unchanged": True})
    original = ROOT / "tools/textures/source/vajra-32.png"
    vajra = Image.open(original).convert("RGBA")
    assert vajra.size == (32, 32)
    target = TARGET / "vajra.png"
    if args.check:
        assert Image.open(target).convert("RGBA").tobytes() == vajra.tobytes(), target
    else:
        vajra.save(target)
    records.append({"item": "vajra", "source": "Original GraviSuite Vajra, detailed native 32x32 remaster",
                    "source_sha256": hashlib.sha256(original.read_bytes()).hexdigest(),
                    "scale": "native 32x32, not nearest-neighbor enlargement", "transparent_pixels":
                    vajra.getchannel("A").tobytes().count(0)})
    if not args.check:
        (ROOT / "docs/tool-texture-provenance.json").write_text(
            json.dumps(records, indent=2) + "\n", encoding="utf-8")
    print(f"Verified {len(records)} original tool textures (32x32 RGBA)")


if __name__ == "__main__":
    main()
