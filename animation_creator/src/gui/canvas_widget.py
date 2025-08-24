"""
Canvas Widget - 256x256 animation canvas with transform controls and preview
"""

import tkinter as tk
from tkinter import ttk
from PIL import Image, ImageTk, ImageDraw
import math
from typing import Optional, Tuple, List

from core.frame_manager import FrameManager, AssetInfo
from core.animation_manager import AnimationManager

class TransformHandle:
    """Represents a transform handle for resizing assets."""
    
    def __init__(self, x: int, y: int, cursor: str, handle_type: str):
        self.x = x
        self.y = y
        self.cursor = cursor
        self.handle_type = handle_type  # 'corner', 'edge', or 'move'
        self.size = 8

class AnimationCanvas(tk.Frame):
    """256x256 animation canvas with zoom, transform controls, and preview."""
    
    def __init__(self, parent, animation_manager: AnimationManager, frame_manager: FrameManager):
        super().__init__(parent, bg='#2d2d2d')
        
        self.animation_manager = animation_manager
        self.frame_manager = frame_manager
        
        # Canvas properties
        self.canvas_size = 256
        self.zoom = 1.0
        self.zoom_center = (128, 128)
        
        # Selection and transform
        self.selected_asset_id = None
        self.transform_handles = []
        self.dragging = False
        self.drag_start = None
        self.drag_handle = None
        
        # Preview settings
        self.show_grid = True
        self.grid_size = 16
        self.show_onion_skin = False
        
        self.setup_ui()
        self.setup_bindings()
        
        # Register callbacks
        self.animation_manager.add_frame_changed_callback(self.on_animation_frame_changed)
        self.frame_manager.add_frame_changed_callback(self.on_frame_changed)
        
    def setup_ui(self):
        """Setup the canvas UI components."""
        # Canvas with scrollbars
        canvas_frame = tk.Frame(self, bg='#2d2d2d')
        canvas_frame.pack(fill=tk.BOTH, expand=True)
        
        # Scrollbars
        v_scrollbar = tk.Scrollbar(canvas_frame, orient=tk.VERTICAL)
        v_scrollbar.pack(side=tk.RIGHT, fill=tk.Y)
        
        h_scrollbar = tk.Scrollbar(canvas_frame, orient=tk.HORIZONTAL)
        h_scrollbar.pack(side=tk.BOTTOM, fill=tk.X)
        
        # Main canvas
        self.canvas = tk.Canvas(
            canvas_frame,
            bg='#1e1e1e',
            highlightthickness=0,
            yscrollcommand=v_scrollbar.set,
            xscrollcommand=h_scrollbar.set
        )
        self.canvas.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)
        
        # Configure scrollbars
        v_scrollbar.config(command=self.canvas.yview)
        h_scrollbar.config(command=self.canvas.xview)
        
        # Initial canvas setup
        self.update_canvas_size()
        
    def setup_bindings(self):
        """Setup event bindings for the canvas."""
        self.canvas.bind('<Button-1>', self.on_click)
        self.canvas.bind('<B1-Motion>', self.on_drag)
        self.canvas.bind('<ButtonRelease-1>', self.on_release)
        self.canvas.bind('<Double-Button-1>', self.on_double_click)
        self.canvas.bind('<Button-3>', self.on_right_click)
        self.canvas.bind('<Motion>', self.on_mouse_move)
        self.canvas.bind('<MouseWheel>', self.on_mouse_wheel)
        self.canvas.bind('<Configure>', self.on_canvas_configure)
        
        # Keyboard bindings
        self.canvas.bind('<Delete>', self.on_delete_key)
        self.canvas.focus_set()
        
    def update_canvas_size(self):
        """Update canvas size based on zoom level."""
        display_size = int(self.canvas_size * self.zoom)
        self.canvas.config(scrollregion=(0, 0, display_size, display_size))
        self.redraw()
        
    def set_zoom(self, zoom_level: float):
        """Set the zoom level."""
        self.zoom = max(0.1, min(10.0, zoom_level))
        self.update_canvas_size()
        
    def fit_to_window(self):
        """Fit canvas to window size."""
        canvas_width = self.canvas.winfo_width()
        canvas_height = self.canvas.winfo_height()
        
        if canvas_width > 1 and canvas_height > 1:
            zoom_x = canvas_width / self.canvas_size
            zoom_y = canvas_height / self.canvas_size
            self.set_zoom(min(zoom_x, zoom_y) * 0.9)  # 90% of fit for padding
            
    def canvas_to_image_coords(self, canvas_x: int, canvas_y: int) -> Tuple[int, int]:
        """Convert canvas coordinates to image coordinates."""
        # Account for scroll position
        canvas_x = self.canvas.canvasx(canvas_x)
        canvas_y = self.canvas.canvasy(canvas_y)
        
        # Convert to image coordinates
        image_x = int(canvas_x / self.zoom)
        image_y = int(canvas_y / self.zoom)
        
        return image_x, image_y
        
    def image_to_canvas_coords(self, image_x: int, image_y: int) -> Tuple[int, int]:
        """Convert image coordinates to canvas coordinates."""
        canvas_x = image_x * self.zoom
        canvas_y = image_y * self.zoom
        return int(canvas_x), int(canvas_y)
        
    def redraw(self):
        """Redraw the entire canvas."""
        self.canvas.delete("all")
        
        # Draw background checkerboard pattern
        self.draw_background()
        
        # Draw grid if enabled
        if self.show_grid:
            self.draw_grid()
            
        # Draw onion skin if enabled
        if self.show_onion_skin:
            self.draw_onion_skin()
            
        # Draw current frame
        self.draw_current_frame()
        
        # Draw selection and transform handles
        if self.selected_asset_id:
            self.draw_selection()
            
    def draw_background(self):
        """Draw checkerboard background to show transparency."""
        checker_size = max(8, int(8 * self.zoom))
        display_size = int(self.canvas_size * self.zoom)
        
        for y in range(0, display_size, checker_size * 2):
            for x in range(0, display_size, checker_size * 2):
                # Light gray squares
                self.canvas.create_rectangle(
                    x, y, x + checker_size, y + checker_size,
                    fill='#404040', outline=''
                )
                self.canvas.create_rectangle(
                    x + checker_size, y + checker_size, 
                    x + checker_size * 2, y + checker_size * 2,
                    fill='#404040', outline=''
                )
                
    def draw_grid(self):
        """Draw grid overlay."""
        grid_spacing = int(self.grid_size * self.zoom)
        display_size = int(self.canvas_size * self.zoom)
        
        # Vertical lines
        for x in range(0, display_size + 1, grid_spacing):
            self.canvas.create_line(x, 0, x, display_size, fill='#555555', width=1)
            
        # Horizontal lines
        for y in range(0, display_size + 1, grid_spacing):
            self.canvas.create_line(0, y, display_size, y, fill='#555555', width=1)
            
    def draw_onion_skin(self):
        """Draw previous/next frames with reduced opacity."""
        current_index = self.frame_manager.current_frame_index
        
        # Draw previous frame
        if current_index > 0:
            prev_frame = self.frame_manager.frames[current_index - 1]
            self.draw_frame_assets(prev_frame, opacity=0.3, color_tint=(0, 255, 0))
            
        # Draw next frame
        if current_index < len(self.frame_manager.frames) - 1:
            next_frame = self.frame_manager.frames[current_index + 1]
            self.draw_frame_assets(next_frame, opacity=0.3, color_tint=(255, 0, 0))
            
    def draw_current_frame(self):
        """Draw the current frame."""
        current_frame = self.frame_manager.get_current_frame()
        if current_frame:
            self.draw_frame_assets(current_frame)
            
    def draw_frame_assets(self, frame, opacity: float = 1.0, color_tint: Tuple[int, int, int] = None):
        """Draw all assets in a frame."""
        for asset in frame.assets:
            if not asset.visible:
                continue
                
            # Calculate display position and size
            x, y = self.image_to_canvas_coords(asset.x, asset.y)
            width = int(asset.width * self.zoom)
            height = int(asset.height * self.zoom)
            
            try:
                # Prepare image for display
                display_image = asset.image.copy()
                
                # Apply zoom
                if self.zoom != 1.0:
                    display_size = (width, height)
                    display_image = display_image.resize(display_size, Image.Resampling.NEAREST)
                    
                # Apply opacity for onion skinning
                if opacity < 1.0:
                    if display_image.mode != 'RGBA':
                        display_image = display_image.convert('RGBA')
                    alpha = display_image.split()[-1]
                    alpha = alpha.point(lambda p: int(p * opacity))
                    display_image.putalpha(alpha)
                    
                # Apply color tint for onion skinning
                if color_tint:
                    if display_image.mode != 'RGBA':
                        display_image = display_image.convert('RGBA')
                    # Simple tinting by blending with colored overlay
                    overlay = Image.new('RGBA', display_image.size, color_tint + (int(255 * opacity * 0.2),))
                    display_image = Image.alpha_composite(display_image, overlay)
                    
                # Convert to PhotoImage and display
                photo_image = ImageTk.PhotoImage(display_image)
                self.canvas.create_image(
                    x, y, anchor=tk.NW, image=photo_image,
                    tags=f"asset_{asset.id}"
                )
                
                # Keep reference to prevent garbage collection
                if not hasattr(self, '_image_refs'):
                    self._image_refs = []
                self._image_refs.append(photo_image)
                
            except Exception as e:
                print(f"Error drawing asset {asset.id}: {e}")
                # Draw placeholder rectangle
                self.canvas.create_rectangle(
                    x, y, x + width, y + height,
                    fill='red', outline='darkred', width=2,
                    tags=f"asset_{asset.id}"
                )
                
    def draw_selection(self):
        """Draw selection outline and transform handles."""
        current_frame = self.frame_manager.get_current_frame()
        if not current_frame:
            return
            
        selected_asset = current_frame.get_asset(self.selected_asset_id)
        if not selected_asset:
            return
            
        # Calculate display bounds
        x, y = self.image_to_canvas_coords(selected_asset.x, selected_asset.y)
        width = int(selected_asset.width * self.zoom)
        height = int(selected_asset.height * self.zoom)
        
        # Draw selection outline
        self.canvas.create_rectangle(
            x - 1, y - 1, x + width + 1, y + height + 1,
            outline='#007acc', width=2, tags="selection"
        )
        
        # Draw transform handles
        self.transform_handles.clear()
        handle_size = 8
        
        # Corner handles
        handles = [
            (x - handle_size//2, y - handle_size//2, "nw-resize", "corner"),  # Top-left
            (x + width - handle_size//2, y - handle_size//2, "ne-resize", "corner"),  # Top-right
            (x - handle_size//2, y + height - handle_size//2, "sw-resize", "corner"),  # Bottom-left
            (x + width - handle_size//2, y + height - handle_size//2, "se-resize", "corner"),  # Bottom-right
        ]
        
        # Edge handles
        handles.extend([
            (x + width//2 - handle_size//2, y - handle_size//2, "n-resize", "edge"),  # Top
            (x + width//2 - handle_size//2, y + height - handle_size//2, "s-resize", "edge"),  # Bottom
            (x - handle_size//2, y + height//2 - handle_size//2, "w-resize", "edge"),  # Left
            (x + width - handle_size//2, y + height//2 - handle_size//2, "e-resize", "edge"),  # Right
        ])
        
        for hx, hy, cursor, handle_type in handles:
            handle = TransformHandle(hx + handle_size//2, hy + handle_size//2, cursor, handle_type)
            self.transform_handles.append(handle)
            
            self.canvas.create_rectangle(
                hx, hy, hx + handle_size, hy + handle_size,
                fill='white', outline='#007acc', width=1, tags="handle"
            )
            
    def get_handle_at_position(self, x: int, y: int) -> Optional[TransformHandle]:
        """Get the transform handle at the given position."""
        for handle in self.transform_handles:
            if (abs(x - handle.x) <= handle.size//2 and 
                abs(y - handle.y) <= handle.size//2):
                return handle
        return None
        
    def get_asset_at_position(self, image_x: int, image_y: int) -> Optional[str]:
        """Get the asset ID at the given image coordinates."""
        current_frame = self.frame_manager.get_current_frame()
        if not current_frame:
            return None
            
        # Check assets in reverse order (top to bottom)
        for asset in reversed(current_frame.assets):
            if not asset.visible:
                continue
                
            if (asset.x <= image_x < asset.x + asset.width and
                asset.y <= image_y < asset.y + asset.height):
                return asset.id
                
        return None
        
    # Event handlers
    def on_click(self, event):
        """Handle mouse click."""
        canvas_x, canvas_y = event.x, event.y
        
        # Check for handle click first
        handle = self.get_handle_at_position(canvas_x, canvas_y)
        if handle:
            self.dragging = True
            self.drag_start = (canvas_x, canvas_y)
            self.drag_handle = handle
            self.canvas.config(cursor=handle.cursor)
            return
            
        # Convert to image coordinates
        image_x, image_y = self.canvas_to_image_coords(canvas_x, canvas_y)
        
        # Check for asset click
        clicked_asset_id = self.get_asset_at_position(image_x, image_y)
        
        if clicked_asset_id:
            # Select the asset
            self.selected_asset_id = clicked_asset_id
            self.dragging = True
            self.drag_start = (canvas_x, canvas_y)
            self.canvas.config(cursor="move")
        else:
            # Clear selection
            self.selected_asset_id = None
            
        self.redraw()
        
    def on_drag(self, event):
        """Handle mouse drag."""
        if not self.dragging or not self.drag_start:
            return
            
        canvas_x, canvas_y = event.x, event.y
        dx = canvas_x - self.drag_start[0]
        dy = canvas_y - self.drag_start[1]
        
        current_frame = self.frame_manager.get_current_frame()
        if not current_frame or not self.selected_asset_id:
            return
            
        selected_asset = current_frame.get_asset(self.selected_asset_id)
        if not selected_asset:
            return
            
        if self.drag_handle:
            # Handle resize
            self.handle_resize_drag(selected_asset, dx, dy)
        else:
            # Handle move
            dx_image = dx / self.zoom
            dy_image = dy / self.zoom
            
            new_x = int(selected_asset.x + dx_image)
            new_y = int(selected_asset.y + dy_image)
            
            # Constrain to canvas bounds
            new_x = max(0, min(self.canvas_size - selected_asset.width, new_x))
            new_y = max(0, min(self.canvas_size - selected_asset.height, new_y))
            
            selected_asset.update_transform(new_x, new_y)
            
            # Auto-sync if enabled
            self.frame_manager.sync_asset_transforms(self.selected_asset_id)
            
        self.drag_start = (canvas_x, canvas_y)
        self.redraw()
        
    def handle_resize_drag(self, asset: AssetInfo, dx: int, dy: int):
        """Handle resize dragging."""
        if not self.drag_handle:
            return
            
        dx_image = dx / self.zoom
        dy_image = dy / self.zoom
        
        handle_type = self.drag_handle.handle_type
        cursor = self.drag_handle.cursor
        
        new_x, new_y = asset.x, asset.y
        new_width, new_height = asset.width, asset.height
        
        # Handle different resize types
        if "w" in cursor:  # West (left)
            new_x += dx_image
            new_width -= dx_image
        if "e" in cursor:  # East (right)
            new_width += dx_image
        if "n" in cursor:  # North (top)
            new_y += dy_image
            new_height -= dy_image
        if "s" in cursor:  # South (bottom)
            new_height += dy_image
            
        # Ensure minimum size
        min_size = 4
        if new_width < min_size:
            if "w" in cursor:
                new_x = asset.x + asset.width - min_size
            new_width = min_size
        if new_height < min_size:
            if "n" in cursor:
                new_y = asset.y + asset.height - min_size
            new_height = min_size
            
        # Constrain to canvas bounds
        new_x = max(0, min(self.canvas_size - new_width, new_x))
        new_y = max(0, min(self.canvas_size - new_height, new_y))
        
        asset.update_transform(int(new_x), int(new_y), int(new_width), int(new_height))
        
        # Auto-sync if enabled
        self.frame_manager.sync_asset_transforms(self.selected_asset_id)
        
    def on_release(self, event):
        """Handle mouse release."""
        self.dragging = False
        self.drag_start = None
        self.drag_handle = None
        self.canvas.config(cursor="")
        
    def on_double_click(self, event):
        """Handle double click."""
        # Could be used for entering edit mode or opening properties
        pass
        
    def on_right_click(self, event):
        """Handle right click context menu."""
        # TODO: Implement context menu
        pass
        
    def on_mouse_move(self, event):
        """Handle mouse movement for cursor changes."""
        if self.dragging:
            return
            
        canvas_x, canvas_y = event.x, event.y
        handle = self.get_handle_at_position(canvas_x, canvas_y)
        
        if handle:
            self.canvas.config(cursor=handle.cursor)
        else:
            image_x, image_y = self.canvas_to_image_coords(canvas_x, canvas_y)
            if self.get_asset_at_position(image_x, image_y):
                self.canvas.config(cursor="move")
            else:
                self.canvas.config(cursor="")
                
    def on_mouse_wheel(self, event):
        """Handle mouse wheel for zooming."""
        # Zoom in/out
        if event.delta > 0:
            new_zoom = self.zoom * 1.2
        else:
            new_zoom = self.zoom / 1.2
            
        self.set_zoom(new_zoom)
        
    def on_canvas_configure(self, event):
        """Handle canvas resize."""
        # Clear image references to prevent memory leaks
        if hasattr(self, '_image_refs'):
            self._image_refs.clear()
            
    def on_delete_key(self, event):
        """Handle delete key press."""
        self.delete_selected()
        
    def on_animation_frame_changed(self, frame_index: int):
        """Handle animation frame change."""
        self.frame_manager.set_current_frame(frame_index)
        
    def on_frame_changed(self, frame_index: int):
        """Handle frame manager frame change."""
        self.selected_asset_id = None
        self.redraw()
        
    # Public methods
    def import_image(self, file_path: str):
        """Import an image into the current frame."""
        try:
            # Load image
            image = Image.open(file_path)
            
            # Convert to RGBA if not already
            if image.mode != 'RGBA':
                image = image.convert('RGBA')
                
            # Center the image on canvas
            x = (self.canvas_size - image.width) // 2
            y = (self.canvas_size - image.height) // 2
            
            # Add to current frame
            asset_id = self.frame_manager.add_asset_to_current_frame(image, x, y, file_path)
            
            if asset_id:
                self.selected_asset_id = asset_id
                self.redraw()
                
        except Exception as e:
            raise Exception(f"Failed to import image: {str(e)}")
            
    def delete_selected(self):
        """Delete the currently selected asset."""
        if self.selected_asset_id:
            current_frame = self.frame_manager.get_current_frame()
            if current_frame:
                current_frame.remove_asset(self.selected_asset_id)
                self.selected_asset_id = None
                self.redraw()
                
    def clear_selection(self):
        """Clear the current selection."""
        self.selected_asset_id = None
        self.redraw()
        
    def toggle_preview(self):
        """Toggle animation preview."""
        if self.animation_manager.is_animation_playing():
            self.animation_manager.stop()
        else:
            frame_count = self.frame_manager.get_frame_count()
            if frame_count > 0:
                self.animation_manager.play(frame_count)
                
    def toggle_grid(self):
        """Toggle grid display."""
        self.show_grid = not self.show_grid
        self.redraw()
        
    def toggle_onion_skin(self):
        """Toggle onion skin display."""
        self.show_onion_skin = not self.show_onion_skin
        self.redraw()