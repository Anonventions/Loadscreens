"""
Timeline Widget - Frame management and navigation interface
"""

import tkinter as tk
from tkinter import ttk
from typing import Optional, Callable

from core.frame_manager import FrameManager

class FrameThumbnail(tk.Frame):
    """Individual frame thumbnail in the timeline."""
    
    def __init__(self, parent, frame_index: int, is_current: bool = False):
        super().__init__(parent, relief=tk.RAISED, borderwidth=1)
        
        self.frame_index = frame_index
        self.is_current = is_current
        self.on_select_callback = None
        
        self.setup_ui()
        self.update_style()
        
    def setup_ui(self):
        """Setup the thumbnail UI."""
        self.config(width=80, height=60)
        
        # Frame number label
        self.label = tk.Label(
            self, 
            text=f"{self.frame_index + 1}",
            font=("Arial", 10),
            bg='#3d3d3d',
            fg='white'
        )
        self.label.pack(expand=True)
        
        # Bind click events
        self.bind('<Button-1>', self.on_click)
        self.label.bind('<Button-1>', self.on_click)
        
    def update_style(self):
        """Update visual style based on selection state."""
        if self.is_current:
            self.config(bg='#007acc', relief=tk.SUNKEN, borderwidth=2)
            self.label.config(bg='#007acc', fg='white', font=("Arial", 10, "bold"))
        else:
            self.config(bg='#3d3d3d', relief=tk.RAISED, borderwidth=1)
            self.label.config(bg='#3d3d3d', fg='white', font=("Arial", 10))
            
    def set_current(self, is_current: bool):
        """Set whether this frame is the current frame."""
        self.is_current = is_current
        self.update_style()
        
    def set_select_callback(self, callback: Callable[[int], None]):
        """Set callback for when frame is selected."""
        self.on_select_callback = callback
        
    def on_click(self, event):
        """Handle click on frame thumbnail."""
        if self.on_select_callback:
            self.on_select_callback(self.frame_index)

class TimelineWidget(tk.Frame):
    """Timeline widget for frame navigation and management."""
    
    def __init__(self, parent, frame_manager: FrameManager, main_window):
        super().__init__(parent, bg='#2d2d2d', height=120)
        
        self.frame_manager = frame_manager
        self.main_window = main_window
        self.thumbnails = []
        
        self.setup_ui()
        self.setup_bindings()
        
        # Register callbacks
        self.frame_manager.add_frame_changed_callback(self.on_frame_changed)
        
        # Initialize with current frames
        self.refresh_timeline()
        
    def setup_ui(self):
        """Setup the timeline UI."""
        # Control buttons
        control_frame = tk.Frame(self, bg='#2d2d2d', height=40)
        control_frame.pack(fill=tk.X, padx=5, pady=5)
        control_frame.pack_propagate(False)
        
        # Frame control buttons
        button_style = {'bg': '#4d4d4d', 'fg': 'white', 'relief': tk.RAISED, 'borderwidth': 1}
        
        tk.Button(control_frame, text="◀◀", command=self.first_frame, width=4, **button_style).pack(side=tk.LEFT, padx=2)
        tk.Button(control_frame, text="◀", command=self.previous_frame, width=4, **button_style).pack(side=tk.LEFT, padx=2)
        tk.Button(control_frame, text="▶", command=self.next_frame, width=4, **button_style).pack(side=tk.LEFT, padx=2)
        tk.Button(control_frame, text="▶▶", command=self.last_frame, width=4, **button_style).pack(side=tk.LEFT, padx=2)
        
        # Separator
        tk.Frame(control_frame, width=2, bg='#555555').pack(side=tk.LEFT, fill=tk.Y, padx=10)
        
        # Play controls
        self.play_button = tk.Button(control_frame, text="▶", command=self.toggle_preview, width=4, **button_style)
        self.play_button.pack(side=tk.LEFT, padx=2)
        
        tk.Button(control_frame, text="⏹", command=self.stop_preview, width=4, **button_style).pack(side=tk.LEFT, padx=2)
        
        # Separator
        tk.Frame(control_frame, width=2, bg='#555555').pack(side=tk.LEFT, fill=tk.Y, padx=10)
        
        # Frame management buttons
        tk.Button(control_frame, text="+ Frame", command=self.add_frame, **button_style).pack(side=tk.LEFT, padx=2)
        tk.Button(control_frame, text="Duplicate", command=self.duplicate_current_frame, **button_style).pack(side=tk.LEFT, padx=2)
        tk.Button(control_frame, text="Delete", command=self.delete_current_frame, **button_style).pack(side=tk.LEFT, padx=2)
        
        # Frame timing controls
        timing_frame = tk.Frame(control_frame, bg='#2d2d2d')
        timing_frame.pack(side=tk.RIGHT)
        
        tk.Label(timing_frame, text="Interval:", bg='#2d2d2d', fg='white').pack(side=tk.LEFT)
        self.interval_var = tk.StringVar(value="100")
        interval_spinbox = tk.Spinbox(
            timing_frame, 
            from_=16, to=2000, increment=10,
            textvariable=self.interval_var,
            width=6, bg='#4d4d4d', fg='white',
            command=self.on_interval_changed
        )
        interval_spinbox.pack(side=tk.LEFT, padx=5)
        interval_spinbox.bind('<Return>', self.on_interval_changed)
        
        tk.Label(timing_frame, text="ms", bg='#2d2d2d', fg='white').pack(side=tk.LEFT)
        
        # Timeline frame with scrollbar
        timeline_frame = tk.Frame(self, bg='#2d2d2d')
        timeline_frame.pack(fill=tk.BOTH, expand=True, padx=5, pady=(0, 5))
        
        # Horizontal scrollbar
        self.h_scrollbar = tk.Scrollbar(timeline_frame, orient=tk.HORIZONTAL)
        self.h_scrollbar.pack(side=tk.BOTTOM, fill=tk.X)
        
        # Canvas for thumbnails
        self.timeline_canvas = tk.Canvas(
            timeline_frame,
            bg='#3d3d3d',
            height=75,
            highlightthickness=0,
            xscrollcommand=self.h_scrollbar.set
        )
        self.timeline_canvas.pack(side=tk.TOP, fill=tk.BOTH, expand=True)
        
        # Configure scrollbar
        self.h_scrollbar.config(command=self.timeline_canvas.xview)
        
        # Frame to hold thumbnails
        self.thumbnails_frame = tk.Frame(self.timeline_canvas, bg='#3d3d3d')
        self.timeline_canvas.create_window((0, 0), window=self.thumbnails_frame, anchor=tk.NW)
        
    def setup_bindings(self):
        """Setup event bindings."""
        self.thumbnails_frame.bind('<Configure>', self.on_thumbnails_configure)
        
    def on_thumbnails_configure(self, event):
        """Handle thumbnails frame configuration change."""
        self.timeline_canvas.configure(scrollregion=self.timeline_canvas.bbox("all"))
        
    def refresh_timeline(self):
        """Refresh the timeline display."""
        # Clear existing thumbnails
        for thumbnail in self.thumbnails:
            thumbnail.destroy()
        self.thumbnails.clear()
        
        # Create thumbnails for each frame
        for i in range(self.frame_manager.get_frame_count()):
            is_current = (i == self.frame_manager.current_frame_index)
            thumbnail = FrameThumbnail(self.thumbnails_frame, i, is_current)
            thumbnail.set_select_callback(self.select_frame)
            thumbnail.pack(side=tk.LEFT, padx=2, pady=2)
            self.thumbnails.append(thumbnail)
            
        # Update scroll region
        self.thumbnails_frame.update_idletasks()
        self.timeline_canvas.configure(scrollregion=self.timeline_canvas.bbox("all"))
        
        # Scroll to current frame if needed
        self.scroll_to_current_frame()
        
    def scroll_to_current_frame(self):
        """Scroll timeline to show current frame."""
        if not self.thumbnails:
            return
            
        current_index = self.frame_manager.current_frame_index
        if 0 <= current_index < len(self.thumbnails):
            # Calculate position
            thumbnail_width = 84  # 80 + 4 padding
            total_width = len(self.thumbnails) * thumbnail_width
            current_pos = current_index * thumbnail_width
            
            if total_width > 0:
                # Scroll to position
                left = current_pos / total_width
                right = (current_pos + thumbnail_width) / total_width
                
                # Check if current frame is visible
                view_left, view_right = self.timeline_canvas.xview()
                
                if left < view_left or right > view_right:
                    # Scroll to center the current frame
                    center = (left + right) / 2
                    view_width = view_right - view_left
                    new_left = max(0, min(1 - view_width, center - view_width / 2))
                    self.timeline_canvas.xview_moveto(new_left)
                    
    def on_frame_changed(self, frame_index: int):
        """Handle frame change from frame manager."""
        # Update thumbnail styles
        for i, thumbnail in enumerate(self.thumbnails):
            thumbnail.set_current(i == frame_index)
            
        self.scroll_to_current_frame()
        
        # Update play button
        if hasattr(self.main_window, 'animation_manager'):
            if self.main_window.animation_manager.is_animation_playing():
                self.play_button.config(text="⏸")
            else:
                self.play_button.config(text="▶")
                
    def on_interval_changed(self, event=None):
        """Handle frame interval change."""
        try:
            interval = int(self.interval_var.get())
            if hasattr(self.main_window, 'animation_manager'):
                self.main_window.animation_manager.set_frame_interval(interval)
        except ValueError:
            # Reset to previous value
            self.interval_var.set("100")
            
    # Frame navigation methods
    def select_frame(self, frame_index: int):
        """Select a specific frame."""
        self.frame_manager.set_current_frame(frame_index)
        
    def first_frame(self):
        """Go to first frame."""
        if self.frame_manager.get_frame_count() > 0:
            self.frame_manager.set_current_frame(0)
            
    def previous_frame(self):
        """Go to previous frame."""
        current = self.frame_manager.current_frame_index
        if current > 0:
            self.frame_manager.set_current_frame(current - 1)
            
    def next_frame(self):
        """Go to next frame."""
        current = self.frame_manager.current_frame_index
        max_frame = self.frame_manager.get_frame_count() - 1
        if current < max_frame:
            self.frame_manager.set_current_frame(current + 1)
            
    def last_frame(self):
        """Go to last frame."""
        last_index = self.frame_manager.get_frame_count() - 1
        if last_index >= 0:
            self.frame_manager.set_current_frame(last_index)
            
    # Animation controls
    def toggle_preview(self):
        """Toggle animation preview."""
        if hasattr(self.main_window, 'animation_manager'):
            if self.main_window.animation_manager.is_animation_playing():
                self.main_window.animation_manager.pause()
                self.play_button.config(text="▶")
            else:
                frame_count = self.frame_manager.get_frame_count()
                if frame_count > 1:
                    current_frame = self.frame_manager.current_frame_index
                    self.main_window.animation_manager.play(frame_count, current_frame)
                    self.play_button.config(text="⏸")
                    
    def stop_preview(self):
        """Stop animation preview."""
        if hasattr(self.main_window, 'animation_manager'):
            self.main_window.animation_manager.stop()
            self.play_button.config(text="▶")
            
    # Frame management methods
    def add_frame(self):
        """Add a new frame after the current frame."""
        current_index = self.frame_manager.current_frame_index
        new_index = self.frame_manager.add_frame(current_index + 1)
        self.frame_manager.set_current_frame(new_index)
        self.refresh_timeline()
        
    def duplicate_current_frame(self):
        """Duplicate the current frame."""
        current_index = self.frame_manager.current_frame_index
        if current_index >= 0:
            new_index = self.frame_manager.duplicate_frame(current_index)
            if new_index >= 0:
                self.frame_manager.set_current_frame(new_index)
                self.refresh_timeline()
                
    def delete_current_frame(self):
        """Delete the current frame."""
        if self.frame_manager.get_frame_count() > 1:  # Keep at least one frame
            current_index = self.frame_manager.current_frame_index
            if self.frame_manager.remove_frame(current_index):
                self.refresh_timeline()
        else:
            # Can't delete the last frame
            pass