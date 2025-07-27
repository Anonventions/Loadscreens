"""
YAML Exporter - Generate YAML configuration files for Loadscreens plugin
"""

import yaml
from pathlib import Path
from typing import List, Dict, Any
from core.frame_manager import FrameManager

class YAMLExporter:
    """Exports animation data as YAML configuration for Loadscreens plugin."""
    
    def __init__(self):
        self.default_config = {
            'enabled': True,
            'frame_interval': 10,
            'duration': 100,
            'fade_in_duration': 5,
            'fade_out_duration': 5,
            'show_on_join': True,
            'show_on_world_change': False,
            'show_on_teleport': False,
            'show_on_respawn': False,
            'freeze_player': True,
            'hide_ui': True,
            'block_movement_packets': True,
            'block_interaction_packets': True,
            'position': {
                'x_offset': 0.0,
                'y_offset': 1.5,
                'z_offset': 0.0
            },
            'effects': {
                'pulse_effect': False,
                'pulse_intensity': 0.3,
                'pulse_speed': 0.1,
                'wobble_effect': False,
                'wobble_intensity': 0.1,
                'rainbow_text': False,
                'typewriter_effect': False,
                'typewriter_speed': 2
            }
        }
        
    def export_config(self, frame_manager: FrameManager, output_path: str, animation_name: str = "custom_animation"):
        """Export animation as YAML configuration."""
        if not frame_manager.frames:
            raise ValueError("No frames to export")
            
        # Generate frame file names
        frame_files = []
        for i in range(len(frame_manager.frames)):
            frame_files.append(f"frame_{i+1:03d}.png")
            
        # Create configuration
        config = {
            'loadscreen_types': {
                animation_name: {
                    **self.default_config,
                    'frames': frame_files
                }
            }
        }
        
        # Update timing if available
        # Note: This would need to be connected to the UI settings
        config['loadscreen_types'][animation_name]['frame_interval'] = 10  # Default value
        
        # Write YAML file
        output_path = Path(output_path)
        with open(output_path, 'w', encoding='utf-8') as f:
            yaml.dump(config, f, default_flow_style=False, indent=2, sort_keys=False)
            
    def export_advanced_config(self, frame_manager: FrameManager, output_path: str, 
                             animation_name: str = "custom_animation", settings: Dict[str, Any] = None):
        """Export animation with advanced settings."""
        if not frame_manager.frames:
            raise ValueError("No frames to export")
            
        # Generate frame file names
        frame_files = []
        for i in range(len(frame_manager.frames)):
            frame_files.append(f"frame_{i+1:03d}.png")
            
        # Merge settings with defaults
        config_settings = self.default_config.copy()
        if settings:
            config_settings.update(settings)
            
        # Create configuration
        config = {
            'global': {
                'enabled': True,
                'debug': False,
                'max_concurrent_sessions': 50,
                'use_packetevents': True
            },
            'loadscreen_types': {
                animation_name: {
                    **config_settings,
                    'frames': frame_files
                }
            },
            'packet_settings': {
                'block_movement_packets': True,
                'block_interaction_packets': True,
                'block_inventory_packets': True,
                'block_chat_packets': True,
                'block_command_packets': False
            },
            'placeholders': {
                'cache_duration': 100,
                'custom_placeholders': {
                    '%server_name%': '&bYour Server',
                    '%custom_animation%': f'&a{animation_name}'
                }
            }
        }
        
        # Write YAML file
        output_path = Path(output_path)
        with open(output_path, 'w', encoding='utf-8') as f:
            yaml.dump(config, f, default_flow_style=False, indent=2, sort_keys=False)
            
    def generate_example_config(self, output_path: str):
        """Generate an example configuration file."""
        example_config = {
            'global': {
                'enabled': True,
                'debug': False,
                'max_concurrent_sessions': 50,
                'use_packetevents': True
            },
            'loadscreen_types': {
                'join_animation': {
                    'enabled': True,
                    'frames': [
                        'welcome_1.png',
                        'welcome_2.png',
                        'welcome_3.png',
                        'welcome_4.png'
                    ],
                    'frame_interval': 8,
                    'duration': 60,
                    'fade_in_duration': 10,
                    'fade_out_duration': 10,
                    'show_on_join': True,
                    'show_on_world_change': False,
                    'freeze_player': True,
                    'hide_ui': True,
                    'position': {
                        'x_offset': 0.0,
                        'y_offset': 1.5,
                        'z_offset': 0.0,
                        'rotation_y': 0.0
                    },
                    'effects': {
                        'pulse_effect': True,
                        'pulse_intensity': 0.3,
                        'pulse_speed': 0.1
                    }
                },
                'teleport_animation': {
                    'enabled': True,
                    'frames': [
                        'teleport_1.png',
                        'teleport_2.png',
                        'teleport_3.png'
                    ],
                    'frame_interval': 12,
                    'duration': 40,
                    'show_on_teleport': True,
                    'wobble_effect': True,
                    'wobble_intensity': 0.2
                }
            },
            'packet_settings': {
                'block_movement_packets': True,
                'block_interaction_packets': True,
                'block_inventory_packets': True,
                'block_chat_packets': True,
                'block_command_packets': False
            },
            'placeholders': {
                'cache_duration': 100,
                'custom_placeholders': {
                    '%server_name%': '&bMyServer',
                    '%world_display%': '&e%player_world%'
                }
            }
        }
        
        output_path = Path(output_path)
        with open(output_path, 'w', encoding='utf-8') as f:
            yaml.dump(example_config, f, default_flow_style=False, indent=2, sort_keys=False)