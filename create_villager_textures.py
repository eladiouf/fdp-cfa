import os
from PIL import Image

dirs = [
    r"src\main\resources\assets\citeconomy\textures\entity\villager\profession",
    r"src\main\resources\assets\citeconomy\textures\entity\zombie_villager\profession"
]

for d in dirs:
    os.makedirs(d, exist_ok=True)

# Create a 64x64 transparent image
img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))

filenames = ["banker.png", "merchant.png"]

for d in dirs:
    for f in filenames:
        path = os.path.join(d, f)
        img.save(path, "PNG")
        print(f"Created transparent villager profession texture at {path}")
