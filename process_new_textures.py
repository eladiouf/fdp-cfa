import os
from PIL import Image

def make_transparent_and_resize(input_path, output_path, target_size=(32, 32)):
    if not os.path.exists(input_path):
        print(f"Error: {input_path} does not exist.")
        return
    
    try:
        with Image.open(input_path) as img:
            img = img.convert("RGBA")
            datas = img.getdata()
            
            # Simple thresholding: turn white-ish pixels transparent
            new_data = []
            for item in datas:
                # If R, G, B are all > 230, make it transparent
                if item[0] > 230 and item[1] > 230 and item[2] > 230:
                    new_data.append((0, 0, 0, 0)) # transparent
                else:
                    new_data.append(item)
            
            img.putdata(new_data)
            
            # Autocrop transparent borders
            bbox = img.getbbox()
            if bbox:
                img = img.crop(bbox)
            
            # Resize to target size (32x32) using NEAREST to maintain pixel art style
            img_resized = img.resize(target_size, resample=Image.Resampling.NEAREST)
            
            # Create parent folder if not exists
            os.makedirs(os.path.dirname(output_path), exist_ok=True)
            
            # Save
            img_resized.save(output_path, "PNG")
            print(f"Processed {input_path} -> saved to {output_path}")
            
    except Exception as e:
        print(f"Error processing {input_path}: {e}")

# Paths to the newly generated images in the artifact directory
artifact_dir = r"C:\Users\El Hadji\.gemini\antigravity-ide\brain\55ab424b-78ab-4abb-b16e-2ec0cca4a2c7"

new_textures = {
    os.path.join(artifact_dir, "bankbook_texture_1784521030278.png"): r"src\main\resources\assets\citeconomy\textures\item\bankbook.png",
    os.path.join(artifact_dir, "banker_table_texture_1784521047397.png"): r"src\main\resources\assets\citeconomy\textures\block\banker_table.png",
    os.path.join(artifact_dir, "personal_shop_texture_1784521065288.png"): r"src\main\resources\assets\citeconomy\textures\block\personal_shop.png"
}

for src, dest in new_textures.items():
    make_transparent_and_resize(src, dest)
