#!/usr/bin/env python3
"""
Test script for Canvas Animation Creator core functionality
This tests the core components without requiring a GUI.
"""

import sys
import os
from pathlib import Path

# Add src to path
sys.path.append(str(Path(__file__).parent / "src"))

from core.frame_manager import FrameManager, Frame, AssetInfo
from core.animation_manager import AnimationManager
from export.yaml_exporter import YAMLExporter
from export.png_exporter import PNGExporter
from export.nexo_exporter import NexoExporter
from utils.image_utils import create_sample_image, create_sample_animation_frames

def test_frame_manager():
    """Test frame manager functionality."""
    print("Testing Frame Manager...")
    
    frame_manager = FrameManager()
    
    # Test adding frames
    frame_manager.add_frame()
    frame_manager.add_frame()
    assert frame_manager.get_frame_count() == 2, "Frame count should be 2"
    
    # Test adding assets
    sample_image = create_sample_image("Test", (100, 50))
    asset_id = frame_manager.add_asset_to_current_frame(sample_image, 10, 20)
    assert asset_id is not None, "Asset should be added"
    
    # Test frame rendering
    current_frame = frame_manager.get_current_frame()
    rendered = current_frame.render()
    assert rendered.size == (256, 256), "Rendered frame should be 256x256"
    
    print("✓ Frame Manager tests passed")

def test_animation_manager():
    """Test animation manager functionality."""
    print("Testing Animation Manager...")
    
    animation_manager = AnimationManager()
    
    # Test frame interval setting
    animation_manager.set_frame_interval(200)
    assert animation_manager.frame_interval == 200, "Frame interval should be set"
    
    # Test minimum frame interval
    animation_manager.set_frame_interval(5)
    assert animation_manager.frame_interval == 16, "Frame interval should be minimum 16ms"
    
    print("✓ Animation Manager tests passed")

def test_exporters():
    """Test export functionality."""
    print("Testing Exporters...")
    
    # Create test frames
    frame_manager = FrameManager()
    
    # Add frames with sample content
    for i in range(3):
        frame_manager.add_frame()
        frame_manager.set_current_frame(i)
        
        # Add a sample image to each frame
        sample_image = create_sample_image(f"Frame {i+1}", (80, 40))
        frame_manager.add_asset_to_current_frame(sample_image, 50 + i*10, 100)
    
    # Test output directory
    output_dir = Path(__file__).parent / "output" / "test"
    output_dir.mkdir(parents=True, exist_ok=True)
    
    # Test PNG exporter
    png_exporter = PNGExporter()
    try:
        exported_files = png_exporter.export_frames(frame_manager.frames, str(output_dir))
        assert len(exported_files) == 3, "Should export 3 frames"
        print("✓ PNG Exporter test passed")
    except Exception as e:
        print(f"✗ PNG Exporter test failed: {e}")
    
    # Test YAML exporter
    yaml_exporter = YAMLExporter()
    try:
        yaml_path = output_dir / "test_config.yml"
        yaml_exporter.export_config(frame_manager, str(yaml_path), "test_animation")
        assert yaml_path.exists(), "YAML file should be created"
        print("✓ YAML Exporter test passed")
    except Exception as e:
        print(f"✗ YAML Exporter test failed: {e}")
    
    # Test Nexo exporter
    nexo_exporter = NexoExporter()
    try:
        nexo_dir = output_dir / "nexo_pack"
        nexo_exporter.export_resourcepack(frame_manager.frames, str(nexo_dir), "test_animation")
        assert nexo_dir.exists(), "Nexo resourcepack should be created"
        print("✓ Nexo Exporter test passed")
    except Exception as e:
        print(f"✗ Nexo Exporter test failed: {e}")

def test_image_utilities():
    """Test image utility functions."""
    print("Testing Image Utilities...")
    
    from utils.image_utils import (
        create_sample_image, 
        validate_image_size, 
        resize_image_proportional,
        safe_filename
    )
    
    # Test sample image creation
    sample = create_sample_image("Test")
    assert sample.size == (128, 64), "Sample image should have default size"
    
    # Test image validation
    assert validate_image_size(sample), "Sample image should be valid"
    
    # Test proportional resize
    large_image = create_sample_image("Large", (500, 300))
    resized = resize_image_proportional(large_image, (256, 256))
    assert max(resized.size) <= 256, "Resized image should fit in 256x256"
    
    # Test safe filename
    unsafe_name = "test<>file:name"
    safe_name = safe_filename(unsafe_name)
    assert "<" not in safe_name, "Unsafe characters should be removed"
    
    print("✓ Image Utilities tests passed")

def main():
    """Run all tests."""
    print("Running Canvas Animation Creator Tests...")
    print("=" * 50)
    
    try:
        test_frame_manager()
        test_animation_manager()
        test_image_utilities()
        test_exporters()
        
        print("=" * 50)
        print("✓ All tests passed!")
        print("\nTest output created in:", Path(__file__).parent / "output" / "test")
        
    except Exception as e:
        print(f"✗ Test failed: {e}")
        import traceback
        traceback.print_exc()
        return 1
        
    return 0

if __name__ == "__main__":
    sys.exit(main())