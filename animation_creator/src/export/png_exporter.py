"""
PNG Exporter - Export animation frames as PNG sequence
"""

from pathlib import Path
from typing import List
from PIL import Image
from core.frame_manager import FrameManager, Frame

class PNGExporter:
    """Exports animation frames as PNG sequence."""
    
    def __init__(self):
        self.default_size = (256, 256)
        
    def export_frames(self, frames: List[Frame], output_folder: str, 
                     prefix: str = "frame_", start_index: int = 1):
        """Export all frames as PNG files."""
        if not frames:
            raise ValueError("No frames to export")
            
        output_path = Path(output_folder)
        output_path.mkdir(parents=True, exist_ok=True)
        
        exported_files = []
        
        for i, frame in enumerate(frames):
            # Generate filename
            filename = f"{prefix}{i + start_index:03d}.png"
            file_path = output_path / filename
            
            # Render frame
            frame_image = frame.render()
            
            # Ensure image is 256x256
            if frame_image.size != self.default_size:
                frame_image = frame_image.resize(self.default_size, Image.Resampling.LANCZOS)
                
            # Save as PNG
            frame_image.save(file_path, "PNG")
            exported_files.append(str(file_path))
            
        return exported_files
        
    def export_single_frame(self, frame: Frame, output_path: str):
        """Export a single frame as PNG."""
        frame_image = frame.render()
        
        # Ensure image is 256x256
        if frame_image.size != self.default_size:
            frame_image = frame_image.resize(self.default_size, Image.Resampling.LANCZOS)
            
        # Save as PNG
        frame_image.save(output_path, "PNG")
        
    def export_contact_sheet(self, frames: List[Frame], output_path: str, 
                           columns: int = 4, spacing: int = 2):
        """Export all frames as a contact sheet (grid of thumbnails)."""
        if not frames:
            raise ValueError("No frames to export")
            
        # Calculate grid dimensions
        rows = (len(frames) + columns - 1) // columns
        thumb_size = 64  # Thumbnail size
        
        # Calculate contact sheet size
        sheet_width = columns * thumb_size + (columns - 1) * spacing
        sheet_height = rows * thumb_size + (rows - 1) * spacing
        
        # Create contact sheet
        contact_sheet = Image.new('RGBA', (sheet_width, sheet_height), (0, 0, 0, 0))
        
        for i, frame in enumerate(frames):
            # Calculate position
            col = i % columns
            row = i // columns
            x = col * (thumb_size + spacing)
            y = row * (thumb_size + spacing)
            
            # Render and resize frame
            frame_image = frame.render()
            thumbnail = frame_image.resize((thumb_size, thumb_size), Image.Resampling.LANCZOS)
            
            # Paste thumbnail
            contact_sheet.paste(thumbnail, (x, y), thumbnail if thumbnail.mode == 'RGBA' else None)
            
        # Save contact sheet
        contact_sheet.save(output_path, "PNG")
        
    def export_gif(self, frames: List[Frame], output_path: str, 
                   duration: int = 100, loop: int = 0):
        """Export frames as animated GIF."""
        if not frames:
            raise ValueError("No frames to export")
            
        # Render all frames
        gif_frames = []
        for frame in frames:
            frame_image = frame.render()
            
            # Ensure image is 256x256
            if frame_image.size != self.default_size:
                frame_image = frame_image.resize(self.default_size, Image.Resampling.LANCZOS)
                
            # Convert to RGB if needed (GIF doesn't support RGBA well)
            if frame_image.mode == 'RGBA':
                # Create white background
                bg = Image.new('RGB', frame_image.size, (255, 255, 255))
                bg.paste(frame_image, mask=frame_image.split()[-1])  # Use alpha as mask
                frame_image = bg
            elif frame_image.mode != 'RGB':
                frame_image = frame_image.convert('RGB')
                
            gif_frames.append(frame_image)
            
        # Save as GIF
        gif_frames[0].save(
            output_path,
            save_all=True,
            append_images=gif_frames[1:],
            duration=duration,
            loop=loop,
            format="GIF"
        )
        
    def export_with_metadata(self, frames: List[Frame], output_folder: str,
                           metadata: dict = None, prefix: str = "frame_"):
        """Export frames with metadata file."""
        # Export frames
        exported_files = self.export_frames(frames, output_folder, prefix)
        
        # Create metadata
        frame_metadata = {
            'animation_info': {
                'total_frames': len(frames),
                'frame_size': self.default_size,
                'exported_files': [Path(f).name for f in exported_files]
            },
            'export_settings': {
                'prefix': prefix,
                'format': 'PNG'
            }
        }
        
        # Add custom metadata
        if metadata:
            frame_metadata.update(metadata)
            
        # Save metadata as JSON
        import json
        metadata_path = Path(output_folder) / "animation_metadata.json"
        with open(metadata_path, 'w', encoding='utf-8') as f:
            json.dump(frame_metadata, f, indent=2)
            
        return exported_files, str(metadata_path)