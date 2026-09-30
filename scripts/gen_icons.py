"""Generates simple launcher icon PNGs for the app (mipmap-*/ic_launcher[.png|_round.png]).

Run once during project setup; not part of the app's build. Kept in scripts/ purely as a
record of how the icons were produced.
"""
import os
from PIL import Image, ImageDraw, ImageFont

BG_COLOR = (27, 43, 52, 255)       # matches @color/app_bar_bg
ACCENT_COLOR = (255, 255, 255, 255)

DENSITIES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

OUT_ROOT = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res")


def draw_base(size: int) -> Image.Image:
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle([0, 0, size, size], fill=BG_COLOR)

    # A simple pennant/flag glyph to suggest "schedule" without needing real team art.
    margin = size * 0.22
    pole_w = max(2, size * 0.06)
    draw.rectangle([margin, margin, margin + pole_w, size - margin], fill=ACCENT_COLOR)
    flag_points = [
        (margin + pole_w, margin),
        (size - margin, margin + (size - 2 * margin) * 0.28),
        (margin + pole_w, margin + (size - 2 * margin) * 0.56),
    ]
    draw.polygon(flag_points, fill=ACCENT_COLOR)
    return img


def make_round(img: Image.Image) -> Image.Image:
    size = img.size[0]
    mask = Image.new("L", (size, size), 0)
    mdraw = ImageDraw.Draw(mask)
    mdraw.ellipse([0, 0, size, size], fill=255)
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    out.paste(img, (0, 0), mask)
    return out


def main():
    for folder, size in DENSITIES.items():
        out_dir = os.path.join(OUT_ROOT, folder)
        os.makedirs(out_dir, exist_ok=True)
        base = draw_base(size)
        base.save(os.path.join(out_dir, "ic_launcher.png"))
        make_round(base).save(os.path.join(out_dir, "ic_launcher_round.png"))
        print(f"wrote {folder}/ic_launcher.png and ic_launcher_round.png ({size}x{size})")


if __name__ == "__main__":
    main()
