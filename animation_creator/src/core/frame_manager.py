"""
Frame Manager - Handles individual frames and their assets
"""

from typing import List, Dict, Any, Optional, Tuple
from PIL import Image, ImageDraw
import copy
import uuid

class AssetInfo:
    """Information about an asset in a frame."""
    
    def __init__(self, image: Image.Image, x: int = 0, y: int = 0, width: int = None, height: int = None, source_path: str = None):
        self.id = str(uuid.uuid4())
        self.image = image.copy()
        self.original_image = image.copy()  # Keep original for quality preservation
        self.x = x
        self.y = y
        self.width = width or image.width
        self.height = height or image.height
        self.source_path = source_path
        self.rotation = 0
        self.opacity = 255
        self.visible = True
        
    def copy(self):
        """Create a copy of this asset."""
        new_asset = AssetInfo(self.original_image, self.x, self.y, self.width, self.height, self.source_path)
        new_asset.rotation = self.rotation
        new_asset.opacity = self.opacity
        new_asset.visible = self.visible
        return new_asset
        
    def update_transform(self, x: int = None, y: int = None, width: int = None, height: int = None):
        """Update asset transform properties."""
        if x is not None:
            self.x = x
        if y is not None:
            self.y = y
        if width is not None:
            self.width = max(1, width)
        if height is not None:
            self.height = max(1, height)
            
        # Resize image if dimensions changed
        if width is not None or height is not None:
            self.image = self.original_image.resize((self.width, self.height), Image.Resampling.LANCZOS)

class Frame:
    """Represents a single animation frame with its assets."""
    
    def __init__(self, width: int = 256, height: int = 256):
        self.width = width
        self.height = height
        self.assets: List[AssetInfo] = []
        self.background_color = (0, 0, 0, 0)  # Transparent by default
        
    def add_asset(self, asset: AssetInfo) -> str:
        """Add an asset to this frame."""
        self.assets.append(asset)
        return asset.id
        
    def remove_asset(self, asset_id: str) -> bool:
        """Remove an asset by ID."""
        for i, asset in enumerate(self.assets):
            if asset.id == asset_id:
                del self.assets[i]
                return True
        return False
        
    def get_asset(self, asset_id: str) -> Optional[AssetInfo]:
        """Get an asset by ID."""
        for asset in self.assets:
            if asset.id == asset_id:
                return asset
        return None
        
    def copy(self):
        """Create a copy of this frame."""
        new_frame = Frame(self.width, self.height)
        new_frame.background_color = self.background_color
        for asset in self.assets:
            new_frame.add_asset(asset.copy())
        return new_frame
        
    def render(self) -> Image.Image:
        """Render the frame to a PIL Image."""
        # Create base image
        if len(self.background_color) == 4:
            image = Image.new('RGBA', (self.width, self.height), self.background_color)
        else:
            image = Image.new('RGB', (self.width, self.height), self.background_color)
            
        # Render each asset
        for asset in self.assets:
            if not asset.visible:
                continue
                
            # Apply opacity
            asset_image = asset.image.copy()
            if asset.opacity < 255:
                # Create alpha mask
                alpha = asset_image.convert('RGBA')
                alpha.putalpha(asset.opacity)
                asset_image = alpha
                
            # Paste asset onto frame
            try:
                if asset_image.mode == 'RGBA':
                    image.paste(asset_image, (asset.x, asset.y), asset_image)
                else:
                    image.paste(asset_image, (asset.x, asset.y))
            except Exception as e:
                print(f"Error rendering asset {asset.id}: {e}")
                
        return image

class FrameManager:
    """Manages all frames in the animation."""
    
    def __init__(self):
        self.frames: List[Frame] = []
        self.current_frame_index = 0
        self.auto_sync_enabled = True
        self.frame_changed_callbacks = []
        
    def add_frame_changed_callback(self, callback):
        """Add callback for when current frame changes."""
        self.frame_changed_callbacks.append(callback)
        
    def add_frame(self, index: int = None) -> int:
        """Add a new frame at the specified index (or at the end)."""
        new_frame = Frame()
        
        if index is None:
            self.frames.append(new_frame)
            return len(self.frames) - 1
        else:
            self.frames.insert(index, new_frame)
            return index
            
    def remove_frame(self, index: int) -> bool:
        """Remove a frame by index."""
        if 0 <= index < len(self.frames):
            del self.frames[index]
            
            # Adjust current frame index
            if self.current_frame_index >= len(self.frames) and self.frames:
                self.current_frame_index = len(self.frames) - 1
            elif not self.frames:
                self.current_frame_index = 0
                
            self._notify_frame_changed()
            return True
        return False
        
    def duplicate_frame(self, index: int) -> int:
        """Duplicate a frame."""
        if 0 <= index < len(self.frames):
            new_frame = self.frames[index].copy()
            self.frames.insert(index + 1, new_frame)
            return index + 1
        return -1
        
    def move_frame(self, from_index: int, to_index: int) -> bool:
        """Move a frame from one position to another."""
        if 0 <= from_index < len(self.frames) and 0 <= to_index < len(self.frames):
            frame = self.frames.pop(from_index)
            self.frames.insert(to_index, frame)
            
            # Adjust current frame index
            if self.current_frame_index == from_index:
                self.current_frame_index = to_index
            elif from_index < self.current_frame_index <= to_index:
                self.current_frame_index -= 1
            elif to_index <= self.current_frame_index < from_index:
                self.current_frame_index += 1
                
            self._notify_frame_changed()
            return True
        return False
        
    def get_current_frame(self) -> Optional[Frame]:
        """Get the current frame."""
        if 0 <= self.current_frame_index < len(self.frames):
            return self.frames[self.current_frame_index]
        return None
        
    def set_current_frame(self, index: int):
        """Set the current frame index."""
        if 0 <= index < len(self.frames):
            self.current_frame_index = index
            self._notify_frame_changed()
            
    def get_frame_count(self) -> int:
        """Get total number of frames."""
        return len(self.frames)
        
    def clear_frames(self):
        """Remove all frames."""
        self.frames.clear()
        self.current_frame_index = 0
        self._notify_frame_changed()
        
    def add_asset_to_current_frame(self, image: Image.Image, x: int = 0, y: int = 0, source_path: str = None) -> Optional[str]:
        """Add an asset to the current frame."""
        current_frame = self.get_current_frame()
        if current_frame:
            asset = AssetInfo(image, x, y, source_path=source_path)
            return current_frame.add_asset(asset)
        return None
        
    def sync_asset_transforms(self, asset_id: str, exclude_frame_index: int = None):
        """Sync asset transforms across frames with the same source path."""
        if not self.auto_sync_enabled:
            return
            
        # Find the reference asset
        reference_asset = None
        reference_frame_index = exclude_frame_index if exclude_frame_index is not None else self.current_frame_index
        
        if 0 <= reference_frame_index < len(self.frames):
            reference_asset = self.frames[reference_frame_index].get_asset(asset_id)
            
        if not reference_asset or not reference_asset.source_path:
            return
            
        # Apply transforms to assets with the same source path
        for i, frame in enumerate(self.frames):
            if i == reference_frame_index:
                continue
                
            for asset in frame.assets:
                if (asset.source_path == reference_asset.source_path and 
                    asset.id != asset_id):
                    asset.update_transform(
                        reference_asset.x,
                        reference_asset.y,
                        reference_asset.width,
                        reference_asset.height
                    )
                    asset.rotation = reference_asset.rotation
                    asset.opacity = reference_asset.opacity
                    
    def _notify_frame_changed(self):
        """Notify callbacks of frame change."""
        for callback in self.frame_changed_callbacks:
            try:
                callback(self.current_frame_index)
            except Exception as e:
                print(f"Error in frame changed callback: {e}")