# Loadscreens Plugin

![Loadscreens Banner](https://img.shields.io/badge/Loadscreens-v3.0%20Ultimate-blue?style=for-the-badge)
![Minecraft Version](https://img.shields.io/badge/Minecraft-1.20+-green?style=for-the-badge)
![Java Version](https://img.shields.io/badge/Java-17+-orange?style=for-the-badge)
![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen?style=for-the-badge)
![Code Quality](https://img.shields.io/badge/Code%20Quality-Excellent-brightgreen?style=for-the-badge)

A powerful and feature-rich Minecraft plugin that provides customizable animated loading screens for various in-game events.

<p align="center">
  <img src="https://i.imgur.com/lqQKHJQ.gif" width="400" alt="Project demo GIF">
</p>

## 🚀 Recent Improvements

### ✅ What was improved:

- **Fixed critical package structure and dependency issues**
- **Added comprehensive JavaDoc documentation for all classes and methods**
- **Improved error handling and input validation throughout the codebase**
- **Enhanced command system with better validation and error messages**
- **Made external dependencies (PacketEvents, PlaceholderAPI) optional using reflection**
- **Optimized placeholder caching system with memory management**
- **Added unit test structure with example tests**
- **Created GitHub Actions CI/CD pipeline for automated testing**
- **Improved Maven configuration with proper build plugins**
- **Enhanced plugin metadata and command definitions**

### 🔧 Technical Improvements:

- **Package Structure**: Fixed incorrect package declarations across all Java files
- **Configuration**: Created comprehensive default config.yml (was previously empty)
- **Error Handling**: Added try-catch blocks and proper validation throughout
- **Performance**: Implemented intelligent caching with cache size limits and cleanup
- **Documentation**: Added JavaDoc to all public classes and methods
- **Testing**: Created test structure with example unit tests
- **Build System**: Enhanced Maven configuration with quality plugins

## 🌟 Features

### Core Features
- **Animated Text Displays**: Create stunning ASCII art animations with customizable frames
- **Multiple Trigger Events**: Join, leave, world change, teleport, respawn events
- **Advanced Visual Effects**: Pulse, wobble, rainbow text, typewriter effects
- **Packet-Level Control**: Block player interactions during loading screens
- **PlaceholderAPI Support**: Dynamic content with placeholder integration
- **Custom Fonts**: Support for custom Minecraft fonts
- **Performance Optimized**: Efficient caching and concurrent session management

### Visual Effects
- **Pulse Effect**: Breathing scale animation
- **Wobble Effect**: Smooth movement animation
- **Rainbow Text**: Cycling color effects
- **Typewriter Effect**: Character-by-character text reveal
- **Fade In/Out**: Smooth transparency transitions
- **Custom Positioning**: 3D positioning with rotation support

### Player Control
- **Movement Freezing**: Prevent player movement during screens
- **UI Hiding**: Hide game interface elements
- **Packet Blocking**: Block interactions, chat, inventory, movement
- **Sound Integration**: Custom sounds with frame-based or one-time playback

## 📋 Requirements

- **Minecraft Version**: 1.20+
- **Java Version**: 17+
- **Server Software**: Paper/Spigot
- **Optional Dependencies**:
  - PlaceholderAPI (for dynamic placeholders)
  - PacketEvents (for advanced packet control)

## 🚀 Installation

1. Download the latest release from the [releases page](https://github.com/Anonventions/Loadscreens/releases)
2. Place the `Loadscreens.jar` file in your server's `plugins` folder
3. Start/restart your server
4. Configure the plugin in `plugins/Loadscreens/config.yml`
5. Reload the configuration with `/loadscreen reload`

## ⚙️ Configuration

The plugin uses a comprehensive YAML configuration system with extensive documentation:

```yaml
global:
  enabled: true
  debug: false
  max_concurrent_sessions: 50
  use_packetevents: true

loadscreen_types:
  join:
    enabled: true
    show_on_join: true
    frames:
      - "&bLoading..."
      - "&bLoading.  "
      - "&bLoading.. "
      - "&bLoading..."
      - "&bWelcome to &e%player_name%&b!"
    frame_interval: 10
    duration: 100
    # ... many more options available
```

See the full configuration documentation in [config.yml](src/main/resources/config.yml).

## 🎮 Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/loadscreen reload` | `loadscreens.admin` | Reload configuration |
| `/loadscreen test [type]` | `loadscreens.admin` | Test a loadscreen type |
| `/loadscreen show <player> <type> [delay]` | `loadscreens.admin` | Show loadscreen to player |
| `/loadscreen stop` | `loadscreens.use` | Stop your active loadscreen |
| `/loadscreen stopall` | `loadscreens.admin` | Stop all active loadscreens |
| `/loadscreen info` | `loadscreens.admin` | Show plugin information |
| `/loadscreen stats` | `loadscreens.admin` | Show session statistics |
| `/loadscreen types` | `loadscreens.admin` | List available types |

## 🔐 Permissions

- `loadscreens.admin` - Full administrative access
- `loadscreens.use` - Basic user commands
- `loadscreens.bypass` - Bypass loadscreens entirely
- `loadscreens.view` - View loadscreens (default: true)

## 📖 Quick Start Guide

### Creating Your First Loadscreen

1. Edit the configuration file:
```yaml
loadscreen_types:
  welcome:
    enabled: true
    frames:
      - "&a█████"
      - "&a██&f█&a██"
      - "&a█&f███&a█"
      - "&f█████"
    frame_interval: 8
    duration: 60
    pulse_effect: true
```

2. Test your loadscreen:
```
/loadscreen test welcome
```

3. Configure when it shows:
```yaml
welcome:
  show_on_join: true
  show_on_world_change: false
```

## 🎨 Advanced Features

### Visual Effects Configuration
```yaml
pulse_effect: true
pulse_intensity: 0.3
pulse_speed: 0.1
wobble_effect: true
wobble_intensity: 0.1
rainbow_text: true
typewriter_effect: true
typewriter_speed: 2
```

### 3D Positioning
```yaml
x_offset: 0.0
y_offset: 1.5
z_offset: 2.5
rotation_x: 0.0
rotation_y: 45.0
rotation_z: 0.0
scale: 1.2
```

### Placeholder Integration
```yaml
frames:
  - "Welcome &b%player_name%!"
  - "World: &e%player_world%"
  - "Location: &7%player_x%, %player_y%, %player_z%"
```

## 🔧 Development

### Building from Source

```bash
git clone https://github.com/Anonventions/Loadscreens.git
cd Loadscreens
mvn clean package
```

### Running Tests

```bash
mvn test
```

### Generating Documentation

```bash
mvn javadoc:javadoc
```

## 🐛 Troubleshooting

### Common Issues

**Loadscreens not showing**
- Check if `global.enabled: true`
- Verify the specific type is enabled
- Check console for errors

**Performance issues**
- Reduce `max_concurrent_sessions`
- Increase `frame_interval`
- Disable visual effects if needed

**Placeholders not working**
- Install PlaceholderAPI
- Check placeholder syntax
- Verify cache settings

## 🔄 CI/CD

This project uses GitHub Actions for continuous integration:

- **Build**: Automated building and testing on Java 17 and 21
- **Code Quality**: Checkstyle validation and JavaDoc generation
- **Artifacts**: Automatic artifact creation for releases

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for details.

### Code Quality Standards

- All public methods must have JavaDoc documentation
- Input validation is required for all user inputs
- Error handling should be comprehensive
- Tests should be written for new functionality

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🔗 Links

- [GitHub Repository](https://github.com/Anonventions/Loadscreens)
- [Issue Tracker](https://github.com/Anonventions/Loadscreens/issues)
- [Wiki](https://github.com/Anonventions/Loadscreens/wiki)
- [**Discord**](https://discord.gg/SG8jvb9WU5)

## 👥 Credits

**Development Team**
- Axmon - Lead Developer
- Turretedash7 - Lead Developer
- Anonventions - Project Maintainers

**Special Thanks**
- Paper Development Team
- PlaceholderAPI Team
- PacketEvents Team

---

*Made with ❤️ by [Anonventions](https://github.com/Anonventions)*
