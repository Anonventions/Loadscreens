"""
Properties Panel - Asset properties and project settings
"""

import tkinter as tk
from tkinter import ttk

class PropertiesPanel(tk.Frame):
    """Properties panel for asset and project settings."""
    
    def __init__(self, parent, main_window):
        super().__init__(parent, bg='#2d2d2d', width=250)
        self.pack_propagate(False)
        
        self.main_window = main_window
        self.current_asset_id = None
        
        self.setup_ui()
        self.update_properties()
        
    def setup_ui(self):
        """Setup the properties panel UI."""
        # Title
        title_label = tk.Label(
            self, text="Properties", font=("Arial", 12, "bold"),
            bg='#2d2d2d', fg='white'
        )
        title_label.pack(fill=tk.X, padx=10, pady=(10, 5))
        
        # Notebook for tabs
        self.notebook = ttk.Notebook(self)
        self.notebook.pack(fill=tk.BOTH, expand=True, padx=10, pady=5)
        
        # Asset properties tab
        self.asset_frame = tk.Frame(self.notebook, bg='#3d3d3d')
        self.notebook.add(self.asset_frame, text="Asset")
        self.setup_asset_properties()
        
        # Project settings tab
        self.project_frame = tk.Frame(self.notebook, bg='#3d3d3d')
        self.notebook.add(self.project_frame, text="Project")
        self.setup_project_settings()
        
        # Animation settings tab
        self.animation_frame = tk.Frame(self.notebook, bg='#3d3d3d')
        self.notebook.add(self.animation_frame, text="Animation")
        self.setup_animation_settings()
        
    def setup_asset_properties(self):
        """Setup asset properties controls."""
        # Asset info
        info_frame = tk.LabelFrame(
            self.asset_frame, text="Asset Info", 
            bg='#3d3d3d', fg='white', font=("Arial", 10, "bold")
        )
        info_frame.pack(fill=tk.X, padx=5, pady=5)
        
        # Asset ID
        tk.Label(info_frame, text="ID:", bg='#3d3d3d', fg='white').grid(row=0, column=0, sticky=tk.W, padx=5, pady=2)
        self.asset_id_label = tk.Label(info_frame, text="None", bg='#3d3d3d', fg='#cccccc', font=("Arial", 8))
        self.asset_id_label.grid(row=0, column=1, sticky=tk.W, padx=5, pady=2)
        
        # Source path
        tk.Label(info_frame, text="Source:", bg='#3d3d3d', fg='white').grid(row=1, column=0, sticky=tk.W, padx=5, pady=2)
        self.source_label = tk.Label(info_frame, text="None", bg='#3d3d3d', fg='#cccccc', font=("Arial", 8))
        self.source_label.grid(row=1, column=1, sticky=tk.W, padx=5, pady=2)
        
        # Transform properties
        transform_frame = tk.LabelFrame(
            self.asset_frame, text="Transform", 
            bg='#3d3d3d', fg='white', font=("Arial", 10, "bold")
        )
        transform_frame.pack(fill=tk.X, padx=5, pady=5)
        
        # Position
        tk.Label(transform_frame, text="X:", bg='#3d3d3d', fg='white').grid(row=0, column=0, sticky=tk.W, padx=5, pady=2)
        self.x_var = tk.StringVar(value="0")
        x_entry = tk.Entry(transform_frame, textvariable=self.x_var, width=8, bg='#4d4d4d', fg='white')
        x_entry.grid(row=0, column=1, padx=5, pady=2)
        x_entry.bind('<Return>', self.on_transform_changed)
        
        tk.Label(transform_frame, text="Y:", bg='#3d3d3d', fg='white').grid(row=0, column=2, sticky=tk.W, padx=5, pady=2)
        self.y_var = tk.StringVar(value="0")
        y_entry = tk.Entry(transform_frame, textvariable=self.y_var, width=8, bg='#4d4d4d', fg='white')
        y_entry.grid(row=0, column=3, padx=5, pady=2)
        y_entry.bind('<Return>', self.on_transform_changed)
        
        # Size
        tk.Label(transform_frame, text="W:", bg='#3d3d3d', fg='white').grid(row=1, column=0, sticky=tk.W, padx=5, pady=2)
        self.width_var = tk.StringVar(value="0")
        width_entry = tk.Entry(transform_frame, textvariable=self.width_var, width=8, bg='#4d4d4d', fg='white')
        width_entry.grid(row=1, column=1, padx=5, pady=2)
        width_entry.bind('<Return>', self.on_transform_changed)
        
        tk.Label(transform_frame, text="H:", bg='#3d3d3d', fg='white').grid(row=1, column=2, sticky=tk.W, padx=5, pady=2)
        self.height_var = tk.StringVar(value="0")
        height_entry = tk.Entry(transform_frame, textvariable=self.height_var, width=8, bg='#4d4d4d', fg='white')
        height_entry.grid(row=1, column=3, padx=5, pady=2)
        height_entry.bind('<Return>', self.on_transform_changed)
        
        # Appearance properties
        appearance_frame = tk.LabelFrame(
            self.asset_frame, text="Appearance", 
            bg='#3d3d3d', fg='white', font=("Arial", 10, "bold")
        )
        appearance_frame.pack(fill=tk.X, padx=5, pady=5)
        
        # Opacity
        tk.Label(appearance_frame, text="Opacity:", bg='#3d3d3d', fg='white').grid(row=0, column=0, sticky=tk.W, padx=5, pady=2)
        self.opacity_var = tk.IntVar(value=255)
        opacity_scale = tk.Scale(
            appearance_frame, from_=0, to=255, orient=tk.HORIZONTAL,
            variable=self.opacity_var, bg='#3d3d3d', fg='white',
            highlightthickness=0, command=self.on_opacity_changed
        )
        opacity_scale.grid(row=0, column=1, columnspan=3, sticky=tk.EW, padx=5, pady=2)
        
        # Visible checkbox
        self.visible_var = tk.BooleanVar(value=True)
        visible_check = tk.Checkbutton(
            appearance_frame, text="Visible", variable=self.visible_var,
            command=self.on_visibility_changed, bg='#3d3d3d', fg='white',
            selectcolor='#4d4d4d', activebackground='#3d3d3d', activeforeground='white'
        )
        visible_check.grid(row=1, column=0, columnspan=4, sticky=tk.W, padx=5, pady=2)
        
        # Configure grid weights
        transform_frame.columnconfigure(1, weight=1)
        transform_frame.columnconfigure(3, weight=1)
        appearance_frame.columnconfigure(1, weight=1)
        
    def setup_project_settings(self):
        """Setup project settings controls."""
        # Canvas settings
        canvas_frame = tk.LabelFrame(
            self.project_frame, text="Canvas Settings", 
            bg='#3d3d3d', fg='white', font=("Arial", 10, "bold")
        )
        canvas_frame.pack(fill=tk.X, padx=5, pady=5)
        
        # Canvas size (fixed at 256x256)
        tk.Label(canvas_frame, text="Size:", bg='#3d3d3d', fg='white').grid(row=0, column=0, sticky=tk.W, padx=5, pady=2)
        tk.Label(canvas_frame, text="256 x 256", bg='#3d3d3d', fg='#cccccc').grid(row=0, column=1, sticky=tk.W, padx=5, pady=2)
        
        # Background color
        tk.Label(canvas_frame, text="Background:", bg='#3d3d3d', fg='white').grid(row=1, column=0, sticky=tk.W, padx=5, pady=2)
        self.bg_color_var = tk.StringVar(value="Transparent")
        bg_combo = ttk.Combobox(
            canvas_frame, textvariable=self.bg_color_var,
            values=["Transparent", "Black", "White", "Custom"],
            state="readonly", width=12
        )
        bg_combo.grid(row=1, column=1, padx=5, pady=2)
        
        # Export settings
        export_frame = tk.LabelFrame(
            self.project_frame, text="Export Settings", 
            bg='#3d3d3d', fg='white', font=("Arial", 10, "bold")
        )
        export_frame.pack(fill=tk.X, padx=5, pady=5)
        
        # Animation name
        tk.Label(export_frame, text="Name:", bg='#3d3d3d', fg='white').grid(row=0, column=0, sticky=tk.W, padx=5, pady=2)
        self.animation_name_var = tk.StringVar(value="custom_animation")
        name_entry = tk.Entry(export_frame, textvariable=self.animation_name_var, bg='#4d4d4d', fg='white')
        name_entry.grid(row=0, column=1, sticky=tk.EW, padx=5, pady=2)
        
        # Frame prefix
        tk.Label(export_frame, text="Frame Prefix:", bg='#3d3d3d', fg='white').grid(row=1, column=0, sticky=tk.W, padx=5, pady=2)
        self.frame_prefix_var = tk.StringVar(value="frame_")
        prefix_entry = tk.Entry(export_frame, textvariable=self.frame_prefix_var, bg='#4d4d4d', fg='white')
        prefix_entry.grid(row=1, column=1, sticky=tk.EW, padx=5, pady=2)
        
        export_frame.columnconfigure(1, weight=1)
        
    def setup_animation_settings(self):
        """Setup animation settings controls."""
        # Timing settings
        timing_frame = tk.LabelFrame(
            self.animation_frame, text="Timing", 
            bg='#3d3d3d', fg='white', font=("Arial", 10, "bold")
        )
        timing_frame.pack(fill=tk.X, padx=5, pady=5)
        
        # Frame rate / interval
        tk.Label(timing_frame, text="Frame Interval:", bg='#3d3d3d', fg='white').grid(row=0, column=0, sticky=tk.W, padx=5, pady=2)
        self.frame_interval_var = tk.StringVar(value="100")
        interval_spinbox = tk.Spinbox(
            timing_frame, from_=16, to=2000, increment=10,
            textvariable=self.frame_interval_var, width=8,
            bg='#4d4d4d', fg='white'
        )
        interval_spinbox.grid(row=0, column=1, padx=5, pady=2)
        tk.Label(timing_frame, text="ms", bg='#3d3d3d', fg='white').grid(row=0, column=2, sticky=tk.W, padx=2, pady=2)
        
        # Loop setting
        self.loop_var = tk.BooleanVar(value=True)
        loop_check = tk.Checkbutton(
            timing_frame, text="Loop Animation", variable=self.loop_var,
            bg='#3d3d3d', fg='white', selectcolor='#4d4d4d',
            activebackground='#3d3d3d', activeforeground='white'
        )
        loop_check.grid(row=1, column=0, columnspan=3, sticky=tk.W, padx=5, pady=2)
        
        # Playback settings
        playback_frame = tk.LabelFrame(
            self.animation_frame, text="Playback", 
            bg='#3d3d3d', fg='white', font=("Arial", 10, "bold")
        )
        playback_frame.pack(fill=tk.X, padx=5, pady=5)
        
        # Current frame info
        tk.Label(playback_frame, text="Current Frame:", bg='#3d3d3d', fg='white').grid(row=0, column=0, sticky=tk.W, padx=5, pady=2)
        self.current_frame_label = tk.Label(playback_frame, text="1 / 1", bg='#3d3d3d', fg='#cccccc')
        self.current_frame_label.grid(row=0, column=1, sticky=tk.W, padx=5, pady=2)
        
        # Total frames
        tk.Label(playback_frame, text="Total Frames:", bg='#3d3d3d', fg='white').grid(row=1, column=0, sticky=tk.W, padx=5, pady=2)
        self.total_frames_label = tk.Label(playback_frame, text="1", bg='#3d3d3d', fg='#cccccc')
        self.total_frames_label.grid(row=1, column=1, sticky=tk.W, padx=5, pady=2)
        
        # Plugin settings
        plugin_frame = tk.LabelFrame(
            self.animation_frame, text="Plugin Settings", 
            bg='#3d3d3d', fg='white', font=("Arial", 10, "bold")
        )
        plugin_frame.pack(fill=tk.X, padx=5, pady=5)
        
        # Duration
        tk.Label(plugin_frame, text="Duration:", bg='#3d3d3d', fg='white').grid(row=0, column=0, sticky=tk.W, padx=5, pady=2)
        self.duration_var = tk.StringVar(value="100")
        duration_spinbox = tk.Spinbox(
            plugin_frame, from_=10, to=10000, increment=10,
            textvariable=self.duration_var, width=8,
            bg='#4d4d4d', fg='white'
        )
        duration_spinbox.grid(row=0, column=1, padx=5, pady=2)
        tk.Label(plugin_frame, text="ticks", bg='#3d3d3d', fg='white').grid(row=0, column=2, sticky=tk.W, padx=2, pady=2)
        
        # Trigger events
        tk.Label(plugin_frame, text="Show on:", bg='#3d3d3d', fg='white').grid(row=1, column=0, sticky=tk.W, padx=5, pady=2)
        
        # Checkboxes for trigger events
        self.show_on_join_var = tk.BooleanVar(value=True)
        join_check = tk.Checkbutton(
            plugin_frame, text="Join", variable=self.show_on_join_var,
            bg='#3d3d3d', fg='white', selectcolor='#4d4d4d',
            activebackground='#3d3d3d', activeforeground='white'
        )
        join_check.grid(row=2, column=0, sticky=tk.W, padx=5, pady=1)
        
        self.show_on_world_change_var = tk.BooleanVar(value=False)
        world_check = tk.Checkbutton(
            plugin_frame, text="World Change", variable=self.show_on_world_change_var,
            bg='#3d3d3d', fg='white', selectcolor='#4d4d4d',
            activebackground='#3d3d3d', activeforeground='white'
        )
        world_check.grid(row=2, column=1, sticky=tk.W, padx=5, pady=1)
        
        self.show_on_teleport_var = tk.BooleanVar(value=False)
        teleport_check = tk.Checkbutton(
            plugin_frame, text="Teleport", variable=self.show_on_teleport_var,
            bg='#3d3d3d', fg='white', selectcolor='#4d4d4d',
            activebackground='#3d3d3d', activeforeground='white'
        )
        teleport_check.grid(row=3, column=0, sticky=tk.W, padx=5, pady=1)
        
    def update_properties(self):
        """Update properties display based on current selection."""
        # Update asset properties if an asset is selected
        if hasattr(self.main_window, 'canvas') and self.main_window.canvas.selected_asset_id:
            self.update_asset_properties(self.main_window.canvas.selected_asset_id)
        else:
            self.clear_asset_properties()
            
        # Update animation info
        self.update_animation_info()
        
    def update_asset_properties(self, asset_id: str):
        """Update asset properties for the given asset."""
        self.current_asset_id = asset_id
        
        current_frame = self.main_window.frame_manager.get_current_frame()
        if not current_frame:
            return
            
        asset = current_frame.get_asset(asset_id)
        if not asset:
            return
            
        # Update asset info
        self.asset_id_label.config(text=asset_id[:8] + "...")
        self.source_label.config(text=asset.source_path or "Unknown")
        
        # Update transform values
        self.x_var.set(str(asset.x))
        self.y_var.set(str(asset.y))
        self.width_var.set(str(asset.width))
        self.height_var.set(str(asset.height))
        
        # Update appearance values
        self.opacity_var.set(asset.opacity)
        self.visible_var.set(asset.visible)
        
    def clear_asset_properties(self):
        """Clear asset properties display."""
        self.current_asset_id = None
        
        self.asset_id_label.config(text="None")
        self.source_label.config(text="None")
        
        self.x_var.set("0")
        self.y_var.set("0")
        self.width_var.set("0")
        self.height_var.set("0")
        
        self.opacity_var.set(255)
        self.visible_var.set(True)
        
    def update_animation_info(self):
        """Update animation information display."""
        if hasattr(self.main_window, 'frame_manager'):
            current_frame = self.main_window.frame_manager.current_frame_index + 1
            total_frames = self.main_window.frame_manager.get_frame_count()
            
            self.current_frame_label.config(text=f"{current_frame} / {total_frames}")
            self.total_frames_label.config(text=str(total_frames))
            
    # Event handlers
    def on_transform_changed(self, event=None):
        """Handle transform property changes."""
        if not self.current_asset_id:
            return
            
        current_frame = self.main_window.frame_manager.get_current_frame()
        if not current_frame:
            return
            
        asset = current_frame.get_asset(self.current_asset_id)
        if not asset:
            return
            
        try:
            new_x = int(self.x_var.get())
            new_y = int(self.y_var.get())
            new_width = int(self.width_var.get())
            new_height = int(self.height_var.get())
            
            asset.update_transform(new_x, new_y, new_width, new_height)
            
            # Auto-sync if enabled
            self.main_window.frame_manager.sync_asset_transforms(self.current_asset_id)
            
            # Redraw canvas
            if hasattr(self.main_window, 'canvas'):
                self.main_window.canvas.redraw()
                
        except ValueError:
            # Invalid input, revert to current values
            self.update_asset_properties(self.current_asset_id)
            
    def on_opacity_changed(self, value):
        """Handle opacity changes."""
        if not self.current_asset_id:
            return
            
        current_frame = self.main_window.frame_manager.get_current_frame()
        if not current_frame:
            return
            
        asset = current_frame.get_asset(self.current_asset_id)
        if asset:
            asset.opacity = int(value)
            
            # Redraw canvas
            if hasattr(self.main_window, 'canvas'):
                self.main_window.canvas.redraw()
                
    def on_visibility_changed(self):
        """Handle visibility changes."""
        if not self.current_asset_id:
            return
            
        current_frame = self.main_window.frame_manager.get_current_frame()
        if not current_frame:
            return
            
        asset = current_frame.get_asset(self.current_asset_id)
        if asset:
            asset.visible = self.visible_var.get()
            
            # Redraw canvas
            if hasattr(self.main_window, 'canvas'):
                self.main_window.canvas.redraw()