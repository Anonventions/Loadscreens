"""
Main Window GUI for Canvas Animation Creator
Provides the primary user interface with dark theme and all main controls.
"""

import tkinter as tk
from tkinter import ttk, filedialog, messagebox
from PIL import Image, ImageTk
import os
from pathlib import Path

from core.animation_manager import AnimationManager
from core.frame_manager import FrameManager
from gui.canvas_widget import AnimationCanvas
from gui.timeline_widget import TimelineWidget
from gui.toolbar_widget import ToolbarWidget
from gui.properties_panel import PropertiesPanel
from export.yaml_exporter import YAMLExporter
from export.png_exporter import PNGExporter
from export.nexo_exporter import NexoExporter

class MainWindow:
    """Main application window with dark theme and full functionality."""
    
    def __init__(self, root):
        self.root = root
        self.setup_window()
        self.setup_theme()
        self.setup_managers()
        self.setup_ui()
        self.setup_bindings()
        
    def setup_window(self):
        """Configure the main window properties."""
        self.root.title("Canvas Animation Creator - Loadscreens Plugin")
        self.root.geometry("1200x800")
        self.root.minsize(1000, 600)
        
        # Center the window
        self.root.update_idletasks()
        width = self.root.winfo_width()
        height = self.root.winfo_height()
        x = (self.root.winfo_screenwidth() // 2) - (width // 2)
        y = (self.root.winfo_screenheight() // 2) - (height // 2)
        self.root.geometry(f"{width}x{height}+{x}+{y}")
        
    def setup_theme(self):
        """Setup dark theme for the application."""
        # Dark theme colors
        self.colors = {
            'bg': '#2d2d2d',
            'secondary_bg': '#3d3d3d',
            'accent': '#4d4d4d',
            'text': '#ffffff',
            'text_secondary': '#cccccc',
            'highlight': '#007acc',
            'success': '#28a745',
            'warning': '#ffc107',
            'danger': '#dc3545'
        }
        
        # Configure root window
        self.root.configure(bg=self.colors['bg'])
        
        # Configure ttk styles
        style = ttk.Style()
        style.theme_use('clam')
        
        # Configure styles for dark theme
        style.configure('Dark.TFrame', background=self.colors['bg'])
        style.configure('Secondary.TFrame', background=self.colors['secondary_bg'])
        style.configure('Dark.TLabel', background=self.colors['bg'], foreground=self.colors['text'])
        style.configure('Dark.TButton', background=self.colors['accent'], foreground=self.colors['text'])
        style.map('Dark.TButton', 
                 background=[('active', self.colors['highlight'])],
                 foreground=[('active', self.colors['text'])])
        
    def setup_managers(self):
        """Initialize the core animation and frame managers."""
        self.animation_manager = AnimationManager()
        self.frame_manager = FrameManager()
        
        # Create exporters
        self.yaml_exporter = YAMLExporter()
        self.png_exporter = PNGExporter()
        self.nexo_exporter = NexoExporter()
        
    def setup_ui(self):
        """Create and layout all UI components."""
        # Main container
        main_frame = ttk.Frame(self.root, style='Dark.TFrame')
        main_frame.pack(fill=tk.BOTH, expand=True, padx=5, pady=5)
        
        # Toolbar
        self.toolbar = ToolbarWidget(main_frame, self)
        self.toolbar.pack(fill=tk.X, pady=(0, 5))
        
        # Content area - split into left (canvas + timeline) and right (properties)
        content_frame = ttk.Frame(main_frame, style='Dark.TFrame')
        content_frame.pack(fill=tk.BOTH, expand=True)
        
        # Left panel - Canvas and timeline
        left_panel = ttk.Frame(content_frame, style='Dark.TFrame')
        left_panel.pack(side=tk.LEFT, fill=tk.BOTH, expand=True, padx=(0, 5))
        
        # Canvas area with zoom controls
        canvas_frame = ttk.Frame(left_panel, style='Secondary.TFrame')
        canvas_frame.pack(fill=tk.BOTH, expand=True, pady=(0, 5))
        
        # Zoom controls
        zoom_frame = ttk.Frame(canvas_frame, style='Secondary.TFrame')
        zoom_frame.pack(fill=tk.X, padx=5, pady=5)
        
        ttk.Label(zoom_frame, text="Zoom:", style='Dark.TLabel').pack(side=tk.LEFT)
        self.zoom_var = tk.StringVar(value="100%")
        zoom_combo = ttk.Combobox(zoom_frame, textvariable=self.zoom_var, 
                                 values=["25%", "50%", "75%", "100%", "150%", "200%", "300%", "400%"],
                                 state="readonly", width=8)
        zoom_combo.pack(side=tk.LEFT, padx=(5, 10))
        zoom_combo.bind('<<ComboboxSelected>>', self.on_zoom_changed)
        
        ttk.Button(zoom_frame, text="Fit", command=self.fit_to_window, style='Dark.TButton').pack(side=tk.LEFT, padx=2)
        ttk.Button(zoom_frame, text="Reset", command=self.reset_zoom, style='Dark.TButton').pack(side=tk.LEFT, padx=2)
        
        # Canvas widget
        self.canvas = AnimationCanvas(canvas_frame, self.animation_manager, self.frame_manager)
        self.canvas.pack(fill=tk.BOTH, expand=True, padx=5, pady=(0, 5))
        
        # Timeline widget
        self.timeline = TimelineWidget(left_panel, self.frame_manager, self)
        self.timeline.pack(fill=tk.X, pady=(0, 5))
        
        # Right panel - Properties and controls
        self.properties_panel = PropertiesPanel(content_frame, self)
        self.properties_panel.pack(side=tk.RIGHT, fill=tk.Y, padx=(5, 0))
        
        # Status bar
        self.status_bar = ttk.Label(main_frame, text="Ready", style='Dark.TLabel', relief=tk.SUNKEN)
        self.status_bar.pack(fill=tk.X, pady=(5, 0))
        
    def setup_bindings(self):
        """Setup keyboard shortcuts and event bindings."""
        # File operations
        self.root.bind('<Control-n>', lambda e: self.new_project())
        self.root.bind('<Control-o>', lambda e: self.open_project())
        self.root.bind('<Control-s>', lambda e: self.save_project())
        self.root.bind('<Control-e>', lambda e: self.export_frames())
        
        # Edit operations
        self.root.bind('<Control-z>', lambda e: self.undo())
        self.root.bind('<Control-y>', lambda e: self.redo())
        self.root.bind('<Delete>', lambda e: self.delete_selected())
        
        # Animation controls
        self.root.bind('<space>', lambda e: self.toggle_preview())
        self.root.bind('<Left>', lambda e: self.previous_frame())
        self.root.bind('<Right>', lambda e: self.next_frame())
        
        # Window close event
        self.root.protocol("WM_DELETE_WINDOW", self.on_closing)
        
    # File operations
    def new_project(self):
        """Create a new animation project."""
        if self.check_unsaved_changes():
            self.frame_manager.clear_frames()
            self.frame_manager.add_frame()  # Start with one frame
            self.canvas.clear_selection()
            self.update_status("New project created")
            
    def open_project(self):
        """Open an existing project."""
        file_path = filedialog.askopenfilename(
            title="Open Project",
            filetypes=[("JSON files", "*.json"), ("All files", "*.*")]
        )
        if file_path:
            try:
                # TODO: Implement project loading
                self.update_status(f"Opened project: {Path(file_path).name}")
            except Exception as e:
                messagebox.showerror("Error", f"Failed to open project:\n{str(e)}")
                
    def save_project(self):
        """Save the current project."""
        file_path = filedialog.asksaveasfilename(
            title="Save Project",
            defaultextension=".json",
            filetypes=[("JSON files", "*.json"), ("All files", "*.*")]
        )
        if file_path:
            try:
                # TODO: Implement project saving
                self.update_status(f"Saved project: {Path(file_path).name}")
            except Exception as e:
                messagebox.showerror("Error", f"Failed to save project:\n{str(e)}")
                
    def import_image(self):
        """Import an image into the current frame."""
        file_path = filedialog.askopenfilename(
            title="Import Image",
            filetypes=[
                ("Image files", "*.png *.jpg *.jpeg *.gif *.bmp *.tiff"),
                ("PNG files", "*.png"),
                ("JPEG files", "*.jpg *.jpeg"),
                ("GIF files", "*.gif"),
                ("All files", "*.*")
            ]
        )
        if file_path:
            try:
                self.canvas.import_image(file_path)
                self.update_status(f"Imported image: {Path(file_path).name}")
            except Exception as e:
                messagebox.showerror("Error", f"Failed to import image:\n{str(e)}")
                
    def import_gif(self):
        """Import a GIF and distribute frames."""
        file_path = filedialog.askopenfilename(
            title="Import GIF",
            filetypes=[("GIF files", "*.gif"), ("All files", "*.*")]
        )
        if file_path:
            try:
                # TODO: Implement GIF import with frame distribution
                self.update_status(f"Imported GIF: {Path(file_path).name}")
            except Exception as e:
                messagebox.showerror("Error", f"Failed to import GIF:\n{str(e)}")
                
    # Export operations
    def export_frames(self):
        """Export animation as PNG sequence."""
        if not self.frame_manager.frames:
            messagebox.showwarning("Warning", "No frames to export")
            return
            
        folder_path = filedialog.askdirectory(title="Select Export Folder")
        if folder_path:
            try:
                self.png_exporter.export_frames(self.frame_manager.frames, folder_path)
                self.update_status(f"Exported frames to: {folder_path}")
                messagebox.showinfo("Success", f"Exported {len(self.frame_manager.frames)} frames")
            except Exception as e:
                messagebox.showerror("Error", f"Failed to export frames:\n{str(e)}")
                
    def export_yaml(self):
        """Generate YAML configuration for Loadscreens plugin."""
        if not self.frame_manager.frames:
            messagebox.showwarning("Warning", "No frames to export")
            return
            
        file_path = filedialog.asksaveasfilename(
            title="Save YAML Configuration",
            defaultextension=".yml",
            filetypes=[("YAML files", "*.yml *.yaml"), ("All files", "*.*")]
        )
        if file_path:
            try:
                self.yaml_exporter.export_config(self.frame_manager, file_path)
                self.update_status(f"Exported YAML: {Path(file_path).name}")
                messagebox.showinfo("Success", "YAML configuration exported successfully")
            except Exception as e:
                messagebox.showerror("Error", f"Failed to export YAML:\n{str(e)}")
                
    def export_resourcepack(self):
        """Generate Nexo resourcepack."""
        if not self.frame_manager.frames:
            messagebox.showwarning("Warning", "No frames to export")
            return
            
        folder_path = filedialog.askdirectory(title="Select ResourcePack Output Folder")
        if folder_path:
            try:
                self.nexo_exporter.export_resourcepack(self.frame_manager.frames, folder_path)
                self.update_status(f"Exported resourcepack to: {folder_path}")
                messagebox.showinfo("Success", "Nexo resourcepack exported successfully")
            except Exception as e:
                messagebox.showerror("Error", f"Failed to export resourcepack:\n{str(e)}")
                
    # Edit operations
    def undo(self):
        """Undo last action."""
        # TODO: Implement undo functionality
        self.update_status("Undo")
        
    def redo(self):
        """Redo last undone action."""
        # TODO: Implement redo functionality
        self.update_status("Redo")
        
    def delete_selected(self):
        """Delete selected elements."""
        self.canvas.delete_selected()
        self.update_status("Deleted selected elements")
        
    # Animation controls
    def toggle_preview(self):
        """Toggle animation preview."""
        self.canvas.toggle_preview()
        
    def previous_frame(self):
        """Go to previous frame."""
        self.timeline.previous_frame()
        
    def next_frame(self):
        """Go to next frame."""
        self.timeline.next_frame()
        
    # Zoom controls
    def on_zoom_changed(self, event=None):
        """Handle zoom level change."""
        zoom_text = self.zoom_var.get()
        zoom_value = int(zoom_text.replace('%', '')) / 100.0
        self.canvas.set_zoom(zoom_value)
        
    def fit_to_window(self):
        """Fit canvas to window."""
        self.canvas.fit_to_window()
        
    def reset_zoom(self):
        """Reset zoom to 100%."""
        self.zoom_var.set("100%")
        self.canvas.set_zoom(1.0)
        
    # Utility methods
    def update_status(self, message):
        """Update status bar message."""
        self.status_bar.config(text=message)
        self.root.after(3000, lambda: self.status_bar.config(text="Ready"))
        
    def check_unsaved_changes(self):
        """Check for unsaved changes and prompt user."""
        # TODO: Implement unsaved changes detection
        return True
        
    def on_closing(self):
        """Handle application closing."""
        if self.check_unsaved_changes():
            self.root.destroy()