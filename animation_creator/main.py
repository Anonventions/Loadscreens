#!/usr/bin/env python3
"""
Canvas Animation Creator for Loadscreens Plugin
Created by: Anonventions
Version: 1.0.0

A full-featured 256x256 canvas animation creator with frame-by-frame animation support,
GIF/image import, transform controls, auto-sync system, and export capabilities for
the Loadscreens Minecraft plugin.
"""

import tkinter as tk
from tkinter import ttk, filedialog, messagebox
import os
import sys
from pathlib import Path

# Add the src directory to the path for imports
sys.path.append(str(Path(__file__).parent / "src"))

from gui.main_window import MainWindow

def main():
    """Main entry point for the Canvas Animation Creator."""
    try:
        # Create the main application window
        root = tk.Tk()
        app = MainWindow(root)
        
        # Start the application
        root.mainloop()
        
    except Exception as e:
        messagebox.showerror("Application Error", f"Failed to start application:\n{str(e)}")
        sys.exit(1)

if __name__ == "__main__":
    main()