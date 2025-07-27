#!/usr/bin/env python3
"""
Complete Workflow Demo for Canvas Animation Creator
This script demonstrates the full animation creation workflow.
"""

import sys
import os
from pathlib import Path

# Add src to path
sys.path.append(str(Path(__file__).parent / "src"))

from core.frame_manager import FrameManager
from core.animation_manager import AnimationManager
from export.yaml_exporter import YAMLExporter
from export.png_exporter import PNGExporter
from export.nexo_exporter import NexoExporter
from utils.image_utils import create_sample_image

def create_demo_animation():
    """Create a complete demo animation with all features."""
    print("Creating Demo Animation...")
    print("=" * 50)
    
    # Create output directory
    output_dir = Path(__file__).parent / "output" / "demo"
    output_dir.mkdir(parents=True, exist_ok=True)
    
    # Initialize managers
    frame_manager = FrameManager()
    animation_manager = AnimationManager()
    
    # Create 5 frames with different content
    animation_frames = [
        ("🌟 Welcome!", (255, 200, 100)),
        ("🔄 Loading...", (100, 200, 255)),
        ("⚡ Processing...", (255, 100, 200)),
        ("✅ Ready!", (100, 255, 150)),
        ("🚀 Let's Go!", (255, 150, 100))
    ]
    
    print(f"Creating {len(animation_frames)} frames...")
    
    for i, (text, color) in enumerate(animation_frames):
        # Add frame
        frame_manager.add_frame()
        frame_manager.set_current_frame(i)
        
        # Create main title
        title_image = create_sample_image(text, (180, 60), text_color=color)
        title_id = frame_manager.add_asset_to_current_frame(title_image, 38, 80)
        
        # Create subtitle
        subtitle_image = create_sample_image(f"Frame {i+1}", (120, 30), text_color=(200, 200, 200))
        subtitle_id = frame_manager.add_asset_to_current_frame(subtitle_image, 68, 150)
        
        # Create decorative element
        if i % 2 == 0:  # Even frames get a border
            border_image = create_sample_image("", (200, 80), (color[0]//4, color[1]//4, color[2]//4, 100))
            border_id = frame_manager.add_asset_to_current_frame(border_image, 28, 75)
        
        print(f"  ✓ Frame {i+1}: {text}")
    
    print(f"\nAnimation created with {frame_manager.get_frame_count()} frames")
    
    # Export PNG sequence
    print("\nExporting PNG sequence...")
    png_exporter = PNGExporter()
    png_files = png_exporter.export_frames(frame_manager.frames, str(output_dir))
    print(f"  ✓ Exported {len(png_files)} PNG files")
    
    # Export as contact sheet
    contact_sheet_path = output_dir / "contact_sheet.png"
    png_exporter.export_contact_sheet(frame_manager.frames, str(contact_sheet_path))
    print(f"  ✓ Created contact sheet: {contact_sheet_path.name}")
    
    # Export as GIF
    gif_path = output_dir / "animation.gif"
    png_exporter.export_gif(frame_manager.frames, str(gif_path), duration=200)
    print(f"  ✓ Created animated GIF: {gif_path.name}")
    
    # Export YAML configuration
    print("\nExporting YAML configuration...")
    yaml_exporter = YAMLExporter()
    yaml_path = output_dir / "demo_animation.yml"
    yaml_exporter.export_config(frame_manager, str(yaml_path), "demo_animation")
    print(f"  ✓ Created YAML config: {yaml_path.name}")
    
    # Export advanced YAML with custom settings
    advanced_yaml_path = output_dir / "demo_animation_advanced.yml"
    advanced_settings = {
        'frame_interval': 8,
        'duration': 60,
        'show_on_join': True,
        'show_on_world_change': True,
        'effects': {
            'pulse_effect': True,
            'pulse_intensity': 0.4,
            'rainbow_text': True
        }
    }
    yaml_exporter.export_advanced_config(frame_manager, str(advanced_yaml_path), 
                                        "demo_animation_advanced", advanced_settings)
    print(f"  ✓ Created advanced YAML: {advanced_yaml_path.name}")
    
    # Export Nexo ResourcePack
    print("\nExporting Nexo ResourcePack...")
    nexo_exporter = NexoExporter()
    nexo_pack_dir = output_dir / "Demo_Animation_Pack"
    nexo_exporter.export_resourcepack(frame_manager.frames, str(nexo_pack_dir), 
                                     "demo_animation", "Demo Animation Pack")
    print(f"  ✓ Created Nexo ResourcePack: {nexo_pack_dir.name}")
    
    # Create installation guide
    guide_path = nexo_exporter.create_installation_guide(str(nexo_pack_dir), "demo_animation")
    print(f"  ✓ Created installation guide: {Path(guide_path).name}")
    
    # Export as Minecraft .mcmeta animation
    mcmeta_path = output_dir / "demo_animation"
    texture_path, mcmeta_file = nexo_exporter.export_mcmeta_animation(
        frame_manager.frames, str(mcmeta_path), frame_time=4
    )
    print(f"  ✓ Created .mcmeta animation: {Path(texture_path).name}")
    
    # Create metadata
    metadata = {
        'demo_info': {
            'title': 'Demo Animation',
            'description': 'Demonstration of Canvas Animation Creator features',
            'author': 'Anonventions',
            'frames': len(frame_manager.frames),
            'features_demonstrated': [
                'Multi-frame animation',
                'Text rendering',
                'Color variations',
                'Asset layering',
                'PNG sequence export',
                'GIF export',
                'YAML configuration',
                'Nexo ResourcePack',
                'Contact sheet',
                '.mcmeta animation'
            ]
        }
    }
    
    png_files, metadata_file = png_exporter.export_with_metadata(
        frame_manager.frames, str(output_dir), metadata, "demo_"
    )
    print(f"  ✓ Created metadata file: {Path(metadata_file).name}")
    
    print("\n" + "=" * 50)
    print("Demo Animation Complete!")
    print(f"\nOutput created in: {output_dir}")
    print("\nGenerated files:")
    print("  📁 PNG Frames - Individual frame files")
    print("  🎬 Animated GIF - Preview animation")
    print("  🖼️  Contact Sheet - All frames in one image")
    print("  ⚙️  YAML Configs - Plugin configurations")
    print("  📦 Nexo Pack - Complete ResourcePack")
    print("  🎮 .mcmeta Animation - Minecraft animated texture")
    print("  📋 Metadata - Technical information")
    print("  📖 Installation Guide - Setup instructions")
    
    print(f"\nTo use with Loadscreens plugin:")
    print(f"  1. Copy PNG files to your server's resourcepack")
    print(f"  2. Add YAML configuration to plugin config")
    print(f"  3. Use command: /loadscreen show <player> demo_animation")
    
    print(f"\nTo use with Nexo:")
    print(f"  1. Copy {nexo_pack_dir.name} to your server")
    print(f"  2. Follow the installation guide")
    print(f"  3. Reload Nexo configuration")
    
    return output_dir

def main():
    """Run the complete workflow demo."""
    try:
        output_dir = create_demo_animation()
        
        # List all created files
        print(f"\n📄 Files created:")
        for file_path in sorted(output_dir.rglob("*")):
            if file_path.is_file():
                rel_path = file_path.relative_to(output_dir)
                size = file_path.stat().st_size
                if size > 1024:
                    size_str = f"{size // 1024}KB"
                else:
                    size_str = f"{size}B"
                print(f"   {rel_path} ({size_str})")
        
        return 0
        
    except Exception as e:
        print(f"❌ Demo failed: {e}")
        import traceback
        traceback.print_exc()
        return 1

if __name__ == "__main__":
    sys.exit(main())