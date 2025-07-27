# Canvas Animation Creator - User Manual

## Table of Contents
1. [Getting Started](#getting-started)
2. [User Interface Overview](#user-interface-overview)
3. [Creating Your First Animation](#creating-your-first-animation)
4. [Working with Frames](#working-with-frames)
5. [Managing Assets](#managing-assets)
6. [Transform Controls](#transform-controls)
7. [Animation Preview](#animation-preview)
8. [Export Options](#export-options)
9. [Advanced Features](#advanced-features)
10. [Troubleshooting](#troubleshooting)

## Getting Started

### Requirements
- Python 3.8 or higher
- tkinter (usually included with Python)
- Required packages: Pillow, PyYAML, numpy

### Installation
1. Navigate to the `animation_creator` directory
2. Install dependencies: `pip install -r requirements.txt`
3. Run the application: `python main.py`

### First Launch
When you first launch the application, you'll see:
- A 256x256 canvas in the center
- Timeline at the bottom with one frame
- Toolbar at the top with all main tools
- Properties panel on the right

## User Interface Overview

### Main Toolbar
The toolbar contains all primary functions:

**File Operations:**
- **New** - Create a new animation project
- **Open** - Open an existing project file
- **Save** - Save current project

**Import Operations:**
- **Import Image** - Add image to current frame
- **Import GIF** - Import GIF and distribute frames

**Export Operations:**
- **Export Frames** - Export as PNG sequence
- **Export YAML** - Generate plugin configuration
- **Export Pack** - Create Nexo resourcepack

**View Tools:**
- **Grid** - Toggle alignment grid
- **Onion** - Toggle onion skinning (show previous/next frames)

**Settings:**
- **Auto-sync Transforms** - Automatically sync asset positions across frames

### Canvas Area
The main canvas displays your 256x256 animation frame:

**Zoom Controls:**
- Dropdown: Select zoom level (25% to 400%)
- **Fit** - Fit canvas to window
- **Reset** - Reset to 100% zoom

**Mouse Controls:**
- **Click** - Select assets
- **Drag** - Move selected assets
- **Double-click** - (Future: Edit asset properties)
- **Right-click** - (Future: Context menu)
- **Mouse wheel** - Zoom in/out

### Timeline
Bottom panel for frame management:

**Navigation Controls:**
- **◀◀** - First frame
- **◀** - Previous frame  
- **▶** - Next frame
- **▶▶** - Last frame

**Playback Controls:**
- **▶/⏸** - Play/pause animation
- **⏹** - Stop animation

**Frame Management:**
- **+ Frame** - Add new frame
- **Duplicate** - Copy current frame
- **Delete** - Remove current frame

**Timing:**
- **Interval** - Frame duration in milliseconds

**Frame Thumbnails:**
- Click any thumbnail to navigate to that frame
- Current frame highlighted in blue

### Properties Panel
Right panel with three tabs:

**Asset Tab:**
- Asset information (ID, source file)
- Transform properties (X, Y, Width, Height)
- Appearance settings (Opacity, Visibility)

**Project Tab:**
- Canvas settings
- Export configuration (animation name, frame prefix)

**Animation Tab:**
- Timing settings (frame interval, loop)
- Current frame information
- Plugin settings (duration, trigger events)

## Creating Your First Animation

### Step 1: Plan Your Animation
Before starting, consider:
- What type of animation do you want?
- How many frames will you need?
- What assets (images) do you have?

### Step 2: Import Your First Asset
1. Click **Import Image** in the toolbar
2. Select an image file (PNG, JPEG, GIF, etc.)
3. The image will appear centered on the canvas
4. It will be automatically selected (highlighted in blue)

### Step 3: Position Your Asset
1. Click and drag the asset to move it
2. Use corner handles to resize
3. Check the Properties panel for exact positioning

### Step 4: Add More Frames
1. Click **+ Frame** to add a new frame
2. Import assets for the new frame
3. Repeat until you have all frames

### Step 5: Preview Your Animation
1. Click the **▶** (play) button in the timeline
2. Adjust frame interval if needed
3. Use **⏹** (stop) to stop playback

### Step 6: Export Your Animation
1. Click **Export Frames** to save PNG files
2. Click **Export YAML** to generate plugin config
3. Use **Export Pack** for complete Nexo resourcepack

## Working with Frames

### Adding Frames
- **+ Frame** - Adds a new empty frame after current frame
- **Duplicate** - Creates a copy of the current frame

### Navigating Frames
- Click frame thumbnails in timeline
- Use arrow buttons (◀◀ ◀ ▶ ▶▶)
- Keyboard: Left/Right arrow keys

### Deleting Frames
- Select frame and click **Delete**
- Note: Cannot delete the last remaining frame

### Frame Order
- Frames play in left-to-right order in timeline
- Currently no way to reorder (future feature)

## Managing Assets

### Importing Assets
**Supported Formats:**
- PNG (recommended for transparency)
- JPEG/JPG
- GIF (imports first frame, or use Import GIF for all frames)
- BMP, TIFF

**Import Methods:**
- **Import Image** - Single image to current frame
- **Import GIF** - All frames distributed to timeline

### Selecting Assets
- Click on asset in canvas to select
- Selected asset shows blue outline and transform handles
- Only one asset can be selected at a time

### Asset Properties
Selected asset properties shown in Properties panel:
- **ID** - Unique identifier
- **Source** - Original file path
- **Position** - X, Y coordinates
- **Size** - Width, Height in pixels
- **Opacity** - 0 (transparent) to 255 (opaque)
- **Visible** - Show/hide asset

### Deleting Assets
- Select asset and press **Delete** key
- Or clear selection by clicking empty canvas area

## Transform Controls

### Moving Assets
1. Click asset to select
2. Drag to new position
3. Canvas boundaries constrain movement

### Resizing Assets
1. Select asset to show transform handles
2. Drag corner handles to resize proportionally
3. Drag edge handles to resize in one dimension
4. Minimum size: 4x4 pixels

### Handle Types
- **Corner handles** (squares) - Proportional resize
- **Edge handles** (rectangles) - Single-dimension resize
- **Move cursor** - Appears when hovering over asset

### Auto-sync Feature
When **Auto-sync Transforms** is enabled:
- Moving/resizing an asset automatically applies to same asset in other frames
- Based on source file path matching
- Helps maintain consistent positioning across animation

### Precise Positioning
Use Properties panel for exact values:
- Enter specific X, Y coordinates
- Set exact width, height
- Values update in real-time

## Animation Preview

### Playback Controls
- **▶** - Start/resume animation from current frame
- **⏸** - Pause animation (retains position)
- **⏹** - Stop animation (returns to current frame)

### Timing Control
- **Frame Interval** - Duration each frame displays (milliseconds)
- Minimum: 16ms (60 FPS)
- Default: 100ms (10 FPS)
- Range: 16-2000ms

### Loop Settings
- Animation automatically loops by default
- Configure in Properties > Animation tab

### Preview Tips
- Use preview to check animation smoothness
- Adjust frame interval for desired speed
- Stop preview before making edits for better performance

## Export Options

### PNG Sequence Export
**What it creates:**
- Individual PNG files for each frame
- Files named with pattern: `frame_001.png`, `frame_002.png`, etc.
- 256x256 pixel resolution

**Steps:**
1. Click **Export Frames**
2. Choose output folder
3. Files are created with frame prefix

**Uses:**
- Direct use in Loadscreens plugin
- Import into other animation software
- Manual frame editing

### YAML Configuration Export
**What it creates:**
- YAML file compatible with Loadscreens plugin
- Contains all animation settings
- References frame files by name

**Configuration includes:**
- Frame list and timing
- Display settings (duration, fade effects)
- Trigger events (join, teleport, etc.)
- Player control settings
- Visual effects

**Steps:**
1. Click **Export YAML**
2. Choose save location
3. Configure settings in Properties > Animation tab

### Nexo ResourcePack Export
**What it creates:**
- Complete Minecraft resourcepack structure
- Font configuration for frame glyphs
- Nexo-specific item and glyph configs
- Installation guide

**Package contents:**
- `pack.mcmeta` - ResourcePack metadata
- `assets/nexo/textures/gui/[animation]/` - Frame images
- `assets/nexo/font/[animation].json` - Font definition
- `nexo/items.yml` - Nexo item configuration
- `nexo/glyphs.yml` - Nexo glyph configuration

**Steps:**
1. Click **Export Pack**
2. Choose output folder
3. Follow generated installation guide

## Advanced Features

### Grid Overlay
- Toggle with **Grid** button
- 16-pixel grid spacing
- Helps align assets precisely
- Visual aid only (not exported)

### Onion Skinning
- Toggle with **Onion** button
- Shows previous frame in green tint
- Shows next frame in red tint
- 30% opacity overlay
- Helps create smooth animations

### Zoom Functionality
- **Zoom dropdown** - Predefined levels
- **Mouse wheel** - Zoom in/out at cursor
- **Fit button** - Fit canvas to window
- **Reset button** - Return to 100%
- Range: 25% to 400%

### Keyboard Shortcuts
- **Ctrl+N** - New project
- **Ctrl+O** - Open project
- **Ctrl+S** - Save project
- **Ctrl+E** - Export frames
- **Ctrl+Z** - Undo (future feature)
- **Ctrl+Y** - Redo (future feature)
- **Delete** - Delete selected asset
- **Space** - Toggle preview
- **Left/Right arrows** - Navigate frames

### Project Files
- JSON format containing all animation data
- Includes frame information and asset properties
- Does not include actual image data
- Save with **Ctrl+S** or **Save** button

## Troubleshooting

### Application Won't Start
**Problem:** Error when running `python main.py`

**Solutions:**
- Ensure Python 3.8+ is installed
- Install dependencies: `pip install -r requirements.txt`
- On Linux: Install tkinter with `apt install python3-tk`
- Check console for specific error messages

### Images Won't Import
**Problem:** "Failed to import image" error

**Solutions:**
- Check file format is supported
- Ensure file is not corrupted
- Try a different image
- Check file permissions

### Poor Performance
**Problem:** Slow response or choppy animation

**Solutions:**
- Close animation preview when editing
- Reduce zoom level
- Use smaller images (resize before import)
- Reduce number of frames
- Close other applications

### Export Problems
**Problem:** Export fails or creates empty files

**Solutions:**
- Check output folder permissions
- Ensure sufficient disk space
- Verify frames have content
- Try different output location

### Canvas Issues
**Problem:** Can't select assets or strange display

**Solutions:**
- Try different zoom level
- Reset zoom to 100%
- Close and reopen application
- Check if assets are visible (Properties panel)

### ResourcePack Not Working
**Problem:** Generated resourcepack doesn't work in Minecraft

**Solutions:**
- Check Minecraft version compatibility (1.20+)
- Verify file structure matches expected format
- Test individual PNG files
- Check server resourcepack settings

### Plugin Configuration Issues
**Problem:** YAML config causes plugin errors

**Solutions:**
- Validate YAML syntax
- Check frame file paths
- Ensure frame files exist
- Match animation name in config and commands

## Getting Help

If you need additional help:

1. **Check the Examples** - Look at `examples/` folder for sample projects
2. **Test Core Functionality** - Run `python test_core.py` to verify installation
3. **GitHub Issues** - Report bugs at https://github.com/Anonventions/Loadscreens/issues
4. **Discord Support** - Join https://discord.gg/SG8jvb9WU5
5. **Documentation** - Read the main README.md for additional information

## Tips for Best Results

1. **Start Simple** - Begin with 2-3 frames to learn the workflow
2. **Consistent Size** - Use assets of similar sizes for best results
3. **Test Early** - Preview animation frequently during creation
4. **Save Often** - Save your project regularly to avoid data loss
5. **Plan Ahead** - Sketch your animation concept before starting
6. **Use Templates** - Start with example projects and modify them
7. **Optimize Images** - Use appropriate file sizes and formats
8. **Test in Game** - Verify exported animations work in Minecraft

---

*This manual covers all current features. Some advanced features mentioned are planned for future updates.*