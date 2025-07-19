# Canvas Animation Creator for Loadscreens Plugin

A full-featured 256x256 canvas animation creator designed specifically for creating custom loading screen animations for the Loadscreens Minecraft plugin.

## Features

### Core Animation System
- **256x256 Canvas**: Optimized canvas size for Minecraft resource packs
- **Frame-by-Frame Animation**: Create smooth animations with precise frame control
- **Real-time Preview**: Live preview of your animation as you work
- **Zoom Controls**: Zoom in/out for detailed editing

### Import & Export
- **GIF/Image Import**: Import GIFs and images into individual frames
- **PNG Sequence Export**: Export animations as frame-by-frame PNG sequences
- **Multiple Format Support**: PNG, JPEG, GIF, and other common image formats

### Transform Controls
- **Position Control**: Move assets with pixel-perfect precision
- **Resize Control**: Scale assets while maintaining quality
- **Real-time Feedback**: See changes instantly as you transform assets
- **Auto-sync System**: Automatically sync transformations across frames for same-origin assets

### Plugin Integration
- **YAML Generation**: Auto-generate YAML configuration files for the Loadscreens plugin
- **Nexo ResourcePack**: Generate Nexo resourcepack glyph and pack configurations
- **Direct Compatibility**: All exports formatted for immediate use with the plugin

### User Interface
- **Dark Theme**: Modern dark-themed interface throughout
- **Intuitive Layout**: Streamlined workflow for efficient animation creation
- **Cross-platform**: Compatible with Windows, macOS, and Linux

## Requirements

- Python 3.8 or higher
- tkinter (usually included with Python)
- Required packages (see requirements.txt):
  - Pillow (image processing)
  - PyYAML (configuration files)
  - numpy (numerical operations)

## Installation

1. **Clone or download** this repository
2. **Install Python dependencies**:
   ```bash
   cd animation_creator
   pip install -r requirements.txt
   ```
3. **Run the application**:
   ```bash
   python main.py
   ```

## Quick Start Guide

### Creating Your First Animation

1. **Start the Application**
   - Run `python main.py`
   - The main window will open with a 256x256 canvas

2. **Add Frames**
   - Click "Add Frame" to create animation frames
   - Use the frame timeline to navigate between frames

3. **Import Assets**
   - Click "Import Image" or "Import GIF" to add visual elements
   - Drag and drop or use transform controls to position assets

4. **Preview Animation**
   - Click "Play Preview" to see your animation in action
   - Adjust frame timing with the frame interval control

5. **Export**
   - Use "Export PNG Sequence" for frame files
   - Use "Generate YAML" for plugin configuration
   - Use "Generate ResourcePack" for Nexo compatibility

### Transform Controls

- **Move**: Click and drag assets to reposition
- **Resize**: Use corner handles to resize assets
- **Auto-sync**: Enable to automatically apply transforms to same assets across all frames

### Advanced Features

- **Zoom**: Use mouse wheel or zoom controls for detailed editing
- **Grid**: Toggle grid overlay for precise alignment
- **Onion Skinning**: See previous/next frames as overlay for smooth animation
- **Batch Operations**: Apply transforms to multiple frames at once

## Plugin Integration

### YAML Configuration

The tool generates YAML files compatible with the Loadscreens plugin:

```yaml
loadscreen_types:
  custom_animation:
    enabled: true
    frames: ["frame_001.png", "frame_002.png", "frame_003.png"]
    frame_interval: 10
    duration: 100
    # Additional plugin-specific settings
```

### Nexo ResourcePack

Generates complete resourcepack structure:
```
resourcepack/
├── pack.mcmeta
├── assets/
│   └── nexo/
│       ├── font/
│       │   └── default.json
│       └── textures/
│           └── gui/
│               └── your_animation/
│                   ├── frame_001.png
│                   ├── frame_002.png
│                   └── frame_003.png
```

## File Structure

```
animation_creator/
├── main.py                 # Application entry point
├── requirements.txt        # Python dependencies
├── README.md              # This file
├── src/                   # Source code
│   ├── gui/              # User interface components
│   ├── core/             # Core animation system
│   ├── export/           # Export functionality
│   └── utils/            # Utility functions
├── examples/             # Example projects and templates
└── output/               # Default export directory
```

## Troubleshooting

### Common Issues

**Application won't start**
- Ensure Python 3.8+ is installed
- Install required dependencies: `pip install -r requirements.txt`
- Check console for error messages

**Import issues**
- Supported formats: PNG, JPEG, GIF, BMP, TIFF
- Maximum recommended size: 1024x1024 pixels
- Ensure files are not corrupted

**Export problems**
- Check write permissions in output directory
- Ensure sufficient disk space
- Verify all frames have valid content

**Performance issues**
- Reduce number of frames for complex animations
- Lower zoom level during editing
- Close preview during intensive operations

## Contributing

We welcome contributions! Please see the main repository's contributing guidelines.

## License

This project is part of the Loadscreens plugin and follows the same MIT License.

## Support

- [GitHub Issues](https://github.com/Anonventions/Loadscreens/issues)
- [Discord Server](https://discord.gg/SG8jvb9WU5)
- [Wiki Documentation](https://github.com/Anonventions/Loadscreens/wiki)

---

*Made with ❤️ by [Anonventions](https://github.com/Anonventions)*