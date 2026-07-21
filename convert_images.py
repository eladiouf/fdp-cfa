import os
from PIL import Image

def process_image(filepath):
    if os.path.exists(filepath):
        try:
            with Image.open(filepath) as img:
                # Convert to RGBA just in case
                img = img.convert("RGBA")
                # Resize to 16x16 using NEAREST interpolation to keep it pixelated
                img_small = img.resize((16, 16), resample=Image.Resampling.NEAREST)
                # Save it as proper PNG back to the same path
                img_small.save(filepath, format="PNG")
                print(f"Successfully processed {filepath}")
        except Exception as e:
            print(f"Error processing {filepath}: {e}")

images = [
    r"src\main\resources\assets\citeconomy\textures\item\bankbook.png",
    r"src\main\resources\assets\citeconomy\textures\block\banker_table.png",
    r"src\main\resources\assets\citeconomy\textures\block\personal_shop.png"
]

for img_path in images:
    process_image(img_path)
