"""
Toolbar Widget - Main toolbar with file operations and tools
"""

import tkinter as tk
from tkinter import ttk

class ToolbarWidget(tk.Frame):
    """Main toolbar with file operations, tools, and quick actions."""
    
    def __init__(self, parent, main_window):
        super().__init__(parent, bg='#3d3d3d', height=40)
        self.pack_propagate(False)
        
        self.main_window = main_window
        self.setup_ui()
        
    def setup_ui(self):
        """Setup the toolbar UI."""
        # Button style
        button_style = {
            'bg': '#4d4d4d',
            'fg': 'white',
            'relief': tk.RAISED,
            'borderwidth': 1,
            'font': ('Arial', 9)
        }
        
        # File operations
        file_frame = tk.Frame(self, bg='#3d3d3d')
        file_frame.pack(side=tk.LEFT, fill=tk.Y, padx=5)
        
        tk.Button(file_frame, text="New", command=self.main_window.new_project, width=8, **button_style).pack(side=tk.LEFT, padx=1)
        tk.Button(file_frame, text="Open", command=self.main_window.open_project, width=8, **button_style).pack(side=tk.LEFT, padx=1)
        tk.Button(file_frame, text="Save", command=self.main_window.save_project, width=8, **button_style).pack(side=tk.LEFT, padx=1)
        
        # Separator
        tk.Frame(self, width=2, bg='#555555').pack(side=tk.LEFT, fill=tk.Y, padx=5)
        
        # Import operations
        import_frame = tk.Frame(self, bg='#3d3d3d')
        import_frame.pack(side=tk.LEFT, fill=tk.Y, padx=5)
        
        tk.Button(import_frame, text="Import Image", command=self.main_window.import_image, width=12, **button_style).pack(side=tk.LEFT, padx=1)
        tk.Button(import_frame, text="Import GIF", command=self.main_window.import_gif, width=12, **button_style).pack(side=tk.LEFT, padx=1)
        
        # Separator
        tk.Frame(self, width=2, bg='#555555').pack(side=tk.LEFT, fill=tk.Y, padx=5)
        
        # Export operations
        export_frame = tk.Frame(self, bg='#3d3d3d')
        export_frame.pack(side=tk.LEFT, fill=tk.Y, padx=5)
        
        tk.Button(export_frame, text="Export Frames", command=self.main_window.export_frames, width=12, **button_style).pack(side=tk.LEFT, padx=1)
        tk.Button(export_frame, text="Export YAML", command=self.main_window.export_yaml, width=12, **button_style).pack(side=tk.LEFT, padx=1)
        tk.Button(export_frame, text="Export Pack", command=self.main_window.export_resourcepack, width=12, **button_style).pack(side=tk.LEFT, padx=1)
        
        # Separator
        tk.Frame(self, width=2, bg='#555555').pack(side=tk.LEFT, fill=tk.Y, padx=5)
        
        # View tools
        view_frame = tk.Frame(self, bg='#3d3d3d')
        view_frame.pack(side=tk.LEFT, fill=tk.Y, padx=5)
        
        # Grid toggle
        self.grid_button = tk.Button(view_frame, text="Grid", command=self.toggle_grid, width=8, **button_style)
        self.grid_button.pack(side=tk.LEFT, padx=1)
        
        # Onion skin toggle
        self.onion_button = tk.Button(view_frame, text="Onion", command=self.toggle_onion_skin, width=8, **button_style)
        self.onion_button.pack(side=tk.LEFT, padx=1)
        
        # Right side - Auto-sync toggle
        right_frame = tk.Frame(self, bg='#3d3d3d')
        right_frame.pack(side=tk.RIGHT, fill=tk.Y, padx=5)
        
        # Auto-sync toggle with indicator
        self.sync_var = tk.BooleanVar(value=True)
        self.sync_check = tk.Checkbutton(
            right_frame,
            text="Auto-sync Transforms",
            variable=self.sync_var,
            command=self.toggle_auto_sync,
            bg='#3d3d3d',
            fg='white',
            selectcolor='#4d4d4d',
            activebackground='#3d3d3d',
            activeforeground='white'
        )
        self.sync_check.pack(side=tk.RIGHT, padx=5)
        
        # Update button states
        self.update_button_states()
        
    def update_button_states(self):
        """Update button states based on current settings."""
        # Update grid button
        if hasattr(self.main_window, 'canvas') and self.main_window.canvas.show_grid:
            self.grid_button.config(relief=tk.SUNKEN, bg='#007acc')
        else:
            self.grid_button.config(relief=tk.RAISED, bg='#4d4d4d')
            
        # Update onion skin button
        if hasattr(self.main_window, 'canvas') and self.main_window.canvas.show_onion_skin:
            self.onion_button.config(relief=tk.SUNKEN, bg='#007acc')
        else:
            self.onion_button.config(relief=tk.RAISED, bg='#4d4d4d')
            
    def toggle_grid(self):
        """Toggle grid display."""
        if hasattr(self.main_window, 'canvas'):
            self.main_window.canvas.toggle_grid()
            self.update_button_states()
            
    def toggle_onion_skin(self):
        """Toggle onion skin display."""
        if hasattr(self.main_window, 'canvas'):
            self.main_window.canvas.toggle_onion_skin()
            self.update_button_states()
            
    def toggle_auto_sync(self):
        """Toggle auto-sync setting."""
        if hasattr(self.main_window, 'frame_manager'):
            self.main_window.frame_manager.auto_sync_enabled = self.sync_var.get()