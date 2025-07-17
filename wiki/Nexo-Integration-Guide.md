# Nexo Integration Guide

![Loadscreens + Nexo](https://img.shields.io/badge/Loadscreens-Nexo%20Integration-success?style=for-the-badge)

This guide provides comprehensive instructions for integrating Loadscreens with the Nexo platform, enabling enhanced loading screen experiences with custom items, blocks, and advanced mechanics.

## Table of Contents
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Configuration](#configuration)
- [Advanced Features](#advanced-features)
- [Code Examples](#code-examples)
- [Troubleshooting](#troubleshooting)
- [Best Practices](#best-practices)

## Prerequisites

### Required Software
- **Minecraft Server**: 1.20+ (Paper/Spigot)
- **Java**: Version 17 or higher
- **Loadscreens Plugin**: v3.0+ Ultimate
- **Nexo Plugin**: Latest stable version
- **PlaceholderAPI**: Required for dynamic content integration

### Optional Dependencies
- **PacketEvents**: For advanced packet manipulation
- **ProtocolLib**: Alternative packet handling (if not using PacketEvents)
- **WorldGuard**: For region-based loading screens
- **Oraxen**: Alternative to Nexo (configuration similar)

### Permissions Setup
Ensure your server has the following permission nodes configured:
```yaml
permissions:
  - loadscreens.admin      # Full administrative access
  - loadscreens.use        # Basic user commands
  - loadscreens.bypass     # Bypass loadscreens entirely
  - nexo.admin            # Nexo administrative access
  - nexo.give             # Nexo item giving permissions
```

## Installation & Setup

### Step 1: Install Required Plugins

1. **Download and install plugins** in the following order:
   ```bash
   plugins/
   ├── PlaceholderAPI.jar
   ├── PacketEvents.jar (optional but recommended)
   ├── Nexo.jar
   └── Loadscreens.jar
   ```

2. **Start your server** to generate default configurations:
   ```bash
   # Server will generate default config files
   plugins/Loadscreens/config.yml
   plugins/Nexo/settings.yml
   ```

3. **Stop the server** and proceed to configuration.

### Step 2: Basic Configuration Verification

Verify that both plugins are properly loaded:
```bash
# Check server logs for successful loading
[INFO] [Loadscreens] Successfully enabled Loadscreens v3.0!
[INFO] [Nexo] Nexo has been enabled successfully!
[INFO] [PlaceholderAPI] Successfully registered placeholder expansion: nexo
```

## Configuration

### Core Loadscreens Configuration

Edit `plugins/Loadscreens/config.yml`:

```yaml
# =================================
# LOADSCREENS + NEXO INTEGRATION
# =================================

global:
  enabled: true
  debug: false
  max_concurrent_sessions: 100
  use_packetevents: true
  integration:
    nexo:
      enabled: true
      sync_with_nexo_events: true

# Nexo-specific loadscreen types
loadscreen_types:
  # Custom item crafting loadscreen
  nexo_crafting:
    enabled: true
    show_on_nexo_craft: true
    frames:
      - "&8[&7████████████&8] &eNexo Crafting..."
      - "&8[&e█&7███████████&8] &eNexo Crafting..."
      - "&8[&e██&7██████████&8] &eNexo Crafting..."
      - "&8[&e███&7█████████&8] &eNexo Crafting..."
      - "&8[&e████&7████████&8] &eNexo Crafting..."
      - "&8[&e█████&7███████&8] &eNexo Crafting..."
      - "&8[&e██████&7██████&8] &eNexo Crafting..."
      - "&8[&e███████&7█████&8] &eNexo Crafting..."
      - "&8[&e████████&7████&8] &eNexo Crafting..."
      - "&8[&e█████████&7███&8] &eNexo Crafting..."
      - "&8[&e██████████&7██&8] &eNexo Crafting..."
      - "&8[&e███████████&7█&8] &eNexo Crafting..."
      - "&8[&e████████████&8] &aComplete!"
    frame_interval: 3
    duration: 40
    fade_in_duration: 5
    fade_out_duration: 5
    pulse_effect: true
    pulse_intensity: 0.2
    
    # Nexo placeholders integration
    placeholders:
      - "%nexo_item_name%"
      - "%nexo_item_rarity%"
      - "%player_name%"
    
    # Position for optimal viewing
    x_offset: 0.0
    y_offset: 1.8
    z_offset: 0.0
    scale: 1.1
    
    # Sound effects
    sounds:
      frame_sound: "block.anvil.use"
      completion_sound: "entity.player.levelup"
      volume: 0.7
      pitch: 1.2

  # Custom furniture placement
  nexo_furniture:
    enabled: true
    show_on_nexo_place: true
    frames:
      - "&9▓&7▓▓▓▓▓▓▓▓ &bPlacing Nexo Furniture..."
      - "&9▓▓&7▓▓▓▓▓▓▓ &bPlacing Nexo Furniture..."
      - "&9▓▓▓&7▓▓▓▓▓▓ &bPlacing Nexo Furniture..."
      - "&9▓▓▓▓&7▓▓▓▓▓ &bPlacing Nexo Furniture..."
      - "&9▓▓▓▓▓&7▓▓▓▓ &bPlacing Nexo Furniture..."
      - "&9▓▓▓▓▓▓&7▓▓▓ &bPlacing Nexo Furniture..."
      - "&9▓▓▓▓▓▓▓&7▓▓ &bPlacing Nexo Furniture..."
      - "&9▓▓▓▓▓▓▓▓&7▓ &bPlacing Nexo Furniture..."
      - "&9▓▓▓▓▓▓▓▓▓ &aFurniture Placed!"
    frame_interval: 4
    duration: 35
    wobble_effect: true
    wobble_intensity: 0.15
    
  # Resource pack loading
  nexo_resourcepack:
    enabled: true
    show_on_join: true
    priority: high
    frames:
      - "&6⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛ &eLoading Nexo Resources..."
      - "&6⬜⬛⬛⬛⬛⬛⬛⬛⬛⬛ &eLoading Nexo Resources..."
      - "&6⬜⬜⬛⬛⬛⬛⬛⬛⬛⬛ &eLoading Nexo Resources..."
      - "&6⬜⬜⬜⬛⬛⬛⬛⬛⬛⬛ &eLoading Nexo Resources..."
      - "&6⬜⬜⬜⬜⬛⬛⬛⬛⬛⬛ &eLoading Nexo Resources..."
      - "&6⬜⬜⬜⬜⬜⬛⬛⬛⬛⬛ &eLoading Nexo Resources..."
      - "&6⬜⬜⬜⬜⬜⬜⬛⬛⬛⬛ &eLoading Nexo Resources..."
      - "&6⬜⬜⬜⬜⬜⬜⬜⬛⬛⬛ &eLoading Nexo Resources..."
      - "&6⬜⬜⬜⬜⬜⬜⬜⬜⬛⬛ &eLoading Nexo Resources..."
      - "&6⬜⬜⬜⬜⬜⬜⬜⬜⬜⬛ &eLoading Nexo Resources..."
      - "&6⬜⬜⬜⬜⬜⬜⬜⬜⬜⬜ &aResources Loaded!"
    frame_interval: 8
    duration: 100
    fade_in_duration: 15
    fade_out_duration: 10
    typewriter_effect: true
    typewriter_speed: 1

# Nexo event triggers
nexo_events:
  crafting_events:
    - "nexo:item_craft"
    - "nexo:recipe_complete"
  placement_events:
    - "nexo:furniture_place"
    - "nexo:block_place"
  interaction_events:
    - "nexo:item_interact"
    - "nexo:furniture_interact"

# Packet control for Nexo integration
packet_settings:
  block_movement_packets: true
  block_interaction_packets: true
  block_inventory_packets: false  # Allow inventory during Nexo operations
  block_chat_packets: false       # Allow chat during loading
  block_command_packets: false
  nexo_specific:
    block_nexo_packets: false     # Allow Nexo packets to flow
    preserve_custom_model_data: true

# Placeholder integration
placeholders:
  cache_duration: 50
  custom_placeholders:
    "%nexo_version%": "&bNexo %nexo_version%"
    "%nexo_items_count%": "&e%nexo_total_items%"
    "%server_name%": "&9%server_name%"
  nexo_placeholders:
    enabled: true
    refresh_rate: 20  # ticks
```

### Nexo Configuration for Loadscreens

Edit `plugins/Nexo/settings.yml` to add Loadscreens integration:

```yaml
# Nexo settings for Loadscreens integration
integration:
  loadscreens:
    enabled: true
    trigger_events:
      crafting: true
      placement: true
      interaction: false
    loading_delays:
      crafting: 2000    # 2 seconds
      placement: 1500   # 1.5 seconds
      
# Custom events that trigger loadscreens
events:
  custom_crafting:
    loadscreen_type: "nexo_crafting"
    minimum_duration: 30
  furniture_placement:
    loadscreen_type: "nexo_furniture"
    minimum_duration: 25
```

## Advanced Features

### Dynamic Content Integration

Create dynamic loadscreens that respond to Nexo item properties:

```yaml
# Advanced Nexo integration
loadscreen_types:
  nexo_dynamic:
    enabled: true
    frames:
      - "&7Processing &e%nexo_item_name%&7..."
      - "&7Rarity: &%nexo_rarity_color%%nexo_item_rarity%"
      - "&7Material: &f%nexo_base_material%"
      - "&7Custom Model: &b%nexo_custom_model_data%"
      - "&aProcessing complete!"
    conditional_frames:
      # Different frames based on item rarity
      legendary:
        frames:
          - "&6✦ &eLegendary Item Detected &6✦"
          - "&6&l%nexo_item_name%"
          - "&e⚡ Special effects loading... ⚡"
        effects:
          rainbow_text: true
          pulse_effect: true
          pulse_intensity: 0.4
      rare:
        frames:
          - "&9◆ &bRare Item Processing &9◆"
          - "&b%nexo_item_name%"
        effects:
          wobble_effect: true
      common:
        frames:
          - "&7▪ &fProcessing %nexo_item_name%"
```

### Multi-Stage Loading Screens

For complex Nexo operations:

```yaml
loadscreen_types:
  nexo_complex_craft:
    enabled: true
    stages:
      - stage_name: "preparation"
        frames:
          - "&7Preparing crafting station..."
          - "&7Gathering materials..."
        duration: 20
      - stage_name: "processing"
        frames:
          - "&eProcessing Nexo recipe..."
          - "&eCombining materials..."
        duration: 30
        effects:
          pulse_effect: true
      - stage_name: "completion"
        frames:
          - "&aItem crafted successfully!"
          - "&a%nexo_item_name% &ahas been created!"
        duration: 15
        effects:
          rainbow_text: true
```

### Region-Based Loadscreens

Integrate with WorldGuard for location-specific Nexo loadscreens:

```yaml
loadscreen_types:
  nexo_workshop:
    enabled: true
    region_requirements:
      - "nexo_workshop"
      - "crafting_area"
    frames:
      - "&6Workshop Loading..."
      - "&6Nexo crafting enabled in this area"
    show_on_region_enter: true
```

## Code Examples

### Custom Listener Integration

Create a custom listener to trigger Loadscreens from Nexo events:

```java
// Example Java code for custom integration
@EventHandler
public void onNexoCraft(NexoCraftEvent event) {
    Player player = event.getPlayer();
    String itemId = event.getItemId();
    
    // Trigger custom loadscreen based on item type
    LoadscreenManager manager = LoadscreensAPI.getManager();
    
    if (itemId.startsWith("legendary_")) {
        manager.showLoadscreen(player, "nexo_legendary_craft");
    } else if (itemId.startsWith("rare_")) {
        manager.showLoadscreen(player, "nexo_rare_craft");
    } else {
        manager.showLoadscreen(player, "nexo_common_craft");
    }
}

@EventHandler
public void onNexoFurniturePlace(NexoFurniturePlaceEvent event) {
    Player player = event.getPlayer();
    String furnitureId = event.getFurnitureId();
    
    // Custom duration based on furniture complexity
    int duration = calculateDuration(furnitureId);
    
    LoadscreenManager manager = LoadscreensAPI.getManager();
    manager.showLoadscreen(player, "nexo_furniture", duration);
}

private int calculateDuration(String furnitureId) {
    // Complex furniture = longer loading time
    if (furnitureId.contains("complex") || furnitureId.contains("animated")) {
        return 60; // 3 seconds
    }
    return 30; // 1.5 seconds
}
```

### PlaceholderAPI Extension

Custom placeholders for Nexo integration:

```java
// Custom placeholder extension
public class NexoLoadscreensExpansion extends PlaceholderExpansion {
    
    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (params.equals("nexo_current_item")) {
            return getCurrentNexoItem(player);
        }
        
        if (params.equals("nexo_crafting_progress")) {
            return getCraftingProgress(player);
        }
        
        if (params.startsWith("nexo_item_")) {
            String itemId = params.substring(10);
            return getNexoItemProperty(itemId);
        }
        
        return null;
    }
}
```

### Configuration Validation Script

Bash script to validate your Nexo + Loadscreens setup:

```bash
#!/bin/bash
# validate_nexo_loadscreens.sh

echo "🔍 Validating Nexo + Loadscreens Configuration..."

# Check if plugins are installed
if [ ! -f "plugins/Loadscreens.jar" ]; then
    echo "❌ Loadscreens plugin not found!"
    exit 1
fi

if [ ! -f "plugins/Nexo.jar" ]; then
    echo "❌ Nexo plugin not found!"
    exit 1
fi

# Check configuration files
if [ ! -f "plugins/Loadscreens/config.yml" ]; then
    echo "❌ Loadscreens config not found!"
    exit 1
fi

echo "✅ Plugin files validated"

# Check YAML syntax
python3 -c "
import yaml
import sys

try:
    with open('plugins/Loadscreens/config.yml', 'r') as f:
        yaml.safe_load(f)
    print('✅ Loadscreens config syntax is valid')
except yaml.YAMLError as e:
    print(f'❌ Loadscreens config syntax error: {e}')
    sys.exit(1)
"

echo "🎉 Configuration validation complete!"
```

## Troubleshooting

### Common Issues and Solutions

#### 1. Loadscreens Not Triggering with Nexo Events

**Problem**: Loadscreens don't appear when crafting Nexo items.

**Solutions**:
```yaml
# Check integration is enabled
global:
  integration:
    nexo:
      enabled: true
      sync_with_nexo_events: true

# Verify event listeners
nexo_events:
  crafting_events:
    - "nexo:item_craft"
    - "nexo:recipe_complete"  # Make sure this matches your Nexo version
```

**Debug steps**:
1. Enable debug mode: `debug: true`
2. Check server logs for event firing
3. Verify Nexo API version compatibility

#### 2. Placeholder Integration Issues

**Problem**: Nexo placeholders showing as raw text.

**Solutions**:
1. Install PlaceholderAPI
2. Register Nexo expansion: `/papi ecloud download Nexo`
3. Verify placeholder format:
   ```yaml
   placeholders:
     nexo_placeholders:
       enabled: true
       format: "%%nexo_item_%s%%"  # Correct format
   ```

#### 3. Performance Issues with Large Nexo Item Sets

**Problem**: Server lag when showing loadscreens with many Nexo items.

**Solutions**:
```yaml
# Optimize performance
global:
  max_concurrent_sessions: 50  # Reduce if needed
  
placeholders:
  cache_duration: 100  # Increase caching

# Reduce frame complexity
loadscreen_types:
  nexo_crafting:
    frame_interval: 5    # Increase interval
    duration: 30         # Reduce duration
    effects:
      pulse_effect: false  # Disable heavy effects
```

#### 4. Resource Pack Conflicts

**Problem**: Nexo resource pack interferes with loadscreen display.

**Solutions**:
1. Set proper loading order:
   ```yaml
   nexo_resourcepack:
     priority: high
     show_on_join: true
     delay_after_nexo: 20  # Wait for Nexo pack to load
   ```

2. Use packet preservation:
   ```yaml
   packet_settings:
     nexo_specific:
       preserve_custom_model_data: true
       block_nexo_packets: false
   ```

#### 5. Nexo API Version Compatibility

**Problem**: Plugin integration fails after Nexo update.

**Check compatibility matrix**:
```yaml
# Version compatibility
supported_versions:
  nexo: "1.19.0+"
  loadscreens: "3.0+"
  minecraft: "1.20+"
  
# Update hooks if needed
api_hooks:
  nexo_events: "com.nexo.api.events.*"
  nexo_items: "com.nexo.api.items.*"
```

### Debug Commands

Enable detailed logging:
```bash
# Enable debug mode
/loadscreen config set global.debug true

# Test specific integration
/loadscreen test nexo_crafting

# Monitor nexo events
/nexo debug events true

# Check placeholder registration
/papi list | grep nexo
```

### Log Analysis

Look for these patterns in your server logs:

```log
# Successful integration
[INFO] [Loadscreens] Nexo integration initialized successfully
[INFO] [Nexo] Loadscreens hook registered

# Event firing
[DEBUG] [Loadscreens] Nexo craft event fired for player: PlayerName
[DEBUG] [Loadscreens] Triggering loadscreen: nexo_crafting

# Errors to watch for
[ERROR] [Loadscreens] Failed to hook into Nexo API
[WARN] [Nexo] Loadscreens integration version mismatch
```

## Best Practices

### 1. Performance Optimization

**Resource Management**:
```yaml
# Optimize for server performance
global:
  max_concurrent_sessions: 75
  thread_pool_size: 4
  
# Cache frequently used data
placeholders:
  cache_duration: 200  # Increase for stable data
  preload_nexo_items: true
  
# Efficient frame timing
loadscreen_types:
  nexo_crafting:
    frame_interval: 4    # Balance smoothness vs. performance
    max_frames: 15       # Limit total frames
```

**Memory Usage**:
```yaml
# Memory optimization
global:
  cleanup_interval: 300  # Clean up old sessions
  session_timeout: 120   # Auto-timeout stuck sessions

# Efficient asset loading
assets:
  preload_common_frames: true
  compress_frame_data: true
  use_sprite_atlas: true
```

### 2. User Experience Guidelines

**Timing Recommendations**:
- **Crafting**: 2-4 seconds maximum
- **Furniture Placement**: 1.5-2.5 seconds
- **Resource Loading**: 5-8 seconds maximum

**Visual Design**:
```yaml
# Professional appearance
frames:
  - Use consistent color schemes
  - Include progress indicators
  - Provide clear status messages
  - Avoid overwhelming animations

# Example professional design
nexo_professional:
  frames:
    - "&8[&7▰▰▰▰▰▰▰▰▰▰&8] &fProcessing..."
    - "&8[&e▰&7▰▰▰▰▰▰▰▰▰&8] &fProcessing..."
    # Clean, professional progression
```

**Accessibility**:
```yaml
# Ensure accessibility
accessibility:
  color_blind_friendly: true
  high_contrast_mode: true
  disable_rapid_flashing: true
  text_alternatives: true
```

### 3. Configuration Management

**Version Control**:
```bash
# Keep configurations in version control
git add plugins/Loadscreens/config.yml
git add plugins/Nexo/settings.yml
git commit -m "Update Nexo loadscreen integration"
```

**Environment Separation**:
```yaml
# Use different configs for different environments
# config-dev.yml, config-staging.yml, config-prod.yml

development:
  debug: true
  frame_interval: 1  # Faster for testing

production:
  debug: false
  frame_interval: 4  # Optimized for performance
```

**Backup Strategy**:
```bash
# Regular configuration backups
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
mkdir -p backups/configs/$DATE
cp plugins/Loadscreens/config.yml backups/configs/$DATE/
cp plugins/Nexo/settings.yml backups/configs/$DATE/
```

### 4. Integration Patterns

**Event-Driven Architecture**:
```java
// Follow clean event handling patterns
@EventHandler(priority = EventPriority.MONITOR)
public void onNexoEvent(NexoItemEvent event) {
    // Only trigger if event wasn't cancelled
    if (event.isCancelled()) return;
    
    // Async processing to avoid blocking
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        processLoadscreen(event);
    });
}
```

**Graceful Degradation**:
```yaml
# Handle missing dependencies gracefully
fallback_behavior:
  nexo_unavailable:
    use_generic_loadscreen: true
    log_warning: true
  placeholderapi_unavailable:
    use_static_text: true
    disable_dynamic_content: true
```

### 5. Monitoring and Analytics

**Performance Metrics**:
```yaml
# Monitor integration health
monitoring:
  track_loadscreen_duration: true
  track_nexo_event_frequency: true
  alert_on_high_latency: true
  
thresholds:
  max_loadscreen_duration: 10000  # 10 seconds
  max_concurrent_sessions: 100
  min_fps_during_loadscreen: 20
```

**Health Checks**:
```bash
# Automated health check script
#!/bin/bash
# Check if integration is working

# Test Nexo event triggering
timeout 30s /loadscreen test nexo_crafting
if [ $? -eq 0 ]; then
    echo "✅ Nexo integration healthy"
else
    echo "❌ Nexo integration failed"
    # Send alert
fi
```

---

## Support and Resources

### Getting Help

- **GitHub Issues**: [Loadscreens Issues](https://github.com/Anonventions/Loadscreens/issues)
- **Discord Support**: Join the Anonventions Discord
- **Documentation**: [Main Wiki](README.md)

### Contributing

We welcome contributions to improve Nexo integration! Please:
1. Fork the repository
2. Create a feature branch
3. Submit a pull request with your improvements

### Version History

- **v1.0**: Initial Nexo integration guide
- **v1.1**: Added advanced configuration examples
- **v1.2**: Performance optimization guidelines
- **v1.3**: Troubleshooting section expansion

---

*This guide is maintained by the Anonventions team. Last updated: 2024*