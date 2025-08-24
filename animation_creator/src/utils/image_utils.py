"""
Utility functions for the Animation Creator
"""

from PIL import Image, ImageDraw, ImageFont
import os
from pathlib import Path
from typing import Tuple, Optional

def create_sample_image(text: str, size: Tuple[int, int] = (128, 64), 
                       bg_color: Tuple[int, int, int, int] = (0, 0, 0, 0),
                       text_color: Tuple[int, int, int] = (255, 255, 255)) -> Image.Image:
    """Create a sample image with text for testing."""
    image = Image.new('RGBA', size, bg_color)
    draw = ImageDraw.Draw(image)
    
    # Try to use a better font if available
    try:
        # Try to find a font file
        font_size = min(size) // 4
        font = ImageFont.load_default()  # Fallback to default font
    except:
        font = None
        
    # Calculate text position (centered)
    if font:
        bbox = draw.textbbox((0, 0), text, font=font)
        text_width = bbox[2] - bbox[0]
        text_height = bbox[3] - bbox[1]
    else:
        text_width = len(text) * 8
        text_height = 12
        
    x = (size[0] - text_width) // 2
    y = (size[1] - text_height) // 2
    
    # Draw text
    draw.text((x, y), text, fill=text_color, font=font)
    
    return image

def create_sample_animation_frames() -> list:
    """Create sample animation frames for testing."""
    frames = []
    
    # Create 4 frames with different text
    texts = ["Welcome!", "Loading...", "Almost ready!", "Let's go!"]
    colors = [(255, 100, 100), (100, 255, 100), (100, 100, 255), (255, 255, 100)]
    
    for i, (text, color) in enumerate(zip(texts, colors)):
        image = create_sample_image(text, (200, 50), text_color=color)
        frames.append(image)
        
    return frames

def validate_image_size(image: Image.Image, target_size: Tuple[int, int] = (256, 256)) -> bool:
    """Validate if image size is appropriate for the canvas."""
    max_dimension = max(target_size)
    img_max = max(image.size)
    
    # Image should not be larger than canvas and not too small
    return 4 <= img_max <= max_dimension

def resize_image_proportional(image: Image.Image, max_size: Tuple[int, int] = (256, 256)) -> Image.Image:
    """Resize image proportionally to fit within max_size."""
    # Calculate scaling factor
    scale_x = max_size[0] / image.width
    scale_y = max_size[1] / image.height
    scale = min(scale_x, scale_y)
    
    # Calculate new size
    new_width = int(image.width * scale)
    new_height = int(image.height * scale)
    
    return image.resize((new_width, new_height), Image.Resampling.LANCZOS)

def extract_gif_frames(gif_path: str) -> list:
    """Extract frames from a GIF file."""
    frames = []
    
    try:
        with Image.open(gif_path) as gif:
            for frame_index in range(gif.n_frames):
                gif.seek(frame_index)
                # Convert to RGBA
                frame = gif.convert('RGBA')
                frames.append(frame.copy())
                
    except Exception as e:
        raise Exception(f"Failed to extract GIF frames: {str(e)}")
        
    return frames

def get_supported_formats() -> list:
    """Get list of supported image formats."""
    return [
        ("Image files", "*.png *.jpg *.jpeg *.gif *.bmp *.tiff"),
        ("PNG files", "*.png"),
        ("JPEG files", "*.jpg *.jpeg"),
        ("GIF files", "*.gif"),
        ("BMP files", "*.bmp"),
        ("TIFF files", "*.tiff"),
        ("All files", "*.*")
    ]

def ensure_directory(path: str) -> Path:
    """Ensure directory exists, create if it doesn't."""
    dir_path = Path(path)
    dir_path.mkdir(parents=True, exist_ok=True)
    return dir_path

def safe_filename(filename: str) -> str:
    """Create a safe filename by removing invalid characters."""
    import re
    # Replace invalid characters with underscores
    safe_name = re.sub(r'[<>:"/\\|?*]', '_', filename)
    # Remove multiple consecutive underscores
    safe_name = re.sub(r'_+', '_', safe_name)
    # Remove leading/trailing underscores
    safe_name = safe_name.strip('_')
    return safe_name

def get_unique_filename(base_path: Path, name: str, extension: str = "") -> str:
    """Get a unique filename by adding numbers if file exists."""
    counter = 1
    original_name = name
    
    while True:
        filename = f"{name}{extension}"
        if not (base_path / filename).exists():
            return filename
            
        # Add counter to name
        name = f"{original_name}_{counter}"
        counter += 1
        
        # Safety check to prevent infinite loop
        if counter > 1000:
            break
            
    return f"{original_name}_{counter}{extension}"