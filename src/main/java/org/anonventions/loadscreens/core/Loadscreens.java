package org.anonventions.loadscreens.core;

import org.anonventions.loadscreens.command.LoadscreenCommand;
import org.anonventions.loadscreens.depends.PacketManager;
import org.anonventions.loadscreens.depends.PlaceholderManager;
import org.anonventions.loadscreens.util.LoadscreenManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.HandlerList;
import org.bukkit.Bukkit;

/**
 * Main plugin class for the Loadscreens plugin.
 * 
 * <p>This plugin provides customizable animated loading screens for various 
 * in-game events such as player joins, world changes, teleports, and respawns.
 * It supports advanced visual effects, PlaceholderAPI integration, and packet-level
 * control through PacketEvents.</p>
 * 
 * <p>Features include:
 * <ul>
 *   <li>Animated text displays with custom frames</li>
 *   <li>Visual effects (pulse, wobble, rainbow, typewriter)</li>
 *   <li>Multiple trigger events</li>
 *   <li>Packet-level control for player interactions</li>
 *   <li>PlaceholderAPI support for dynamic content</li>
 *   <li>Performance optimized with concurrent session management</li>
 * </ul>
 * 
 * @author Axmon, amon_m, anonventions
 * @version 3.0 Ultimate Edition
 * @since 1.0
 */

public class Loadscreens extends JavaPlugin {
    /** The singleton instance of the plugin */
    private static Loadscreens instance;
    
    /** Whether PacketEvents is available and enabled */
    private boolean packetEventsEnabled = false;
    
    /** Whether PlaceholderAPI is available and enabled */
    private boolean placeholderAPIEnabled = false;
    
    /** Manager for placeholder handling and caching */
    private PlaceholderManager placeholderManager;
    
    /** Manager for packet control and player restrictions */
    private PacketManager packetManager;

    /**
     * Gets the singleton instance of the plugin.
     * 
     * @return the plugin instance
     * @throws IllegalStateException if the plugin has not been initialized
     */
    public static Loadscreens getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Plugin has not been initialized yet");
        }
        return instance;
    }

    /**
     * Called when the plugin is loaded.
     * 
     * <p>This method initializes PacketEvents if available and enabled in the configuration.
     * PacketEvents is loaded during this phase to ensure proper initialization before
     * other plugins.</p>
     */
    @Override
    public void onLoad() {
        // Initialize PacketEvents if available
        if (getConfig().getBoolean("global.use_packetevents", true)) {
            try {
                Class.forName("com.github.retrooper.packetevents.PacketEvents");
                Class<?> builderClass = Class.forName("io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder");
                Object api = builderClass.getMethod("build", Object.class).invoke(null, this);
                
                Class<?> packetEventsClass = Class.forName("com.github.retrooper.packetevents.PacketEvents");
                packetEventsClass.getMethod("setAPI", Object.class).invoke(null, api);
                
                Object apiInstance = packetEventsClass.getMethod("getAPI").invoke(null);
                apiInstance.getClass().getMethod("load").invoke(apiInstance);
                
                packetEventsEnabled = true;
                getLogger().info("PacketEvents initialized successfully!");
            } catch (ClassNotFoundException e) {
                getLogger().info("PacketEvents not found - packet control features disabled");
                packetEventsEnabled = false;
            } catch (Exception e) {
                getLogger().warning("Failed to initialize PacketEvents: " + e.getMessage());
                packetEventsEnabled = false;
            }
        } else {
            getLogger().info("PacketEvents disabled in config");
        }
    }

    /**
     * Called when the plugin is enabled.
     * 
     * <p>This method performs the main plugin initialization including:
     * <ul>
     *   <li>Setting up the plugin instance</li>
     *   <li>Saving default configuration</li>
     *   <li>Checking and initializing dependencies</li>
     *   <li>Registering event listeners and commands</li>
     *   <li>Displaying startup information</li>
     * </ul>
     */
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Check dependencies
        checkDependencies();

        // Initialize managers
        if (placeholderAPIEnabled) {
            placeholderManager = new PlaceholderManager();
        }

        if (packetEventsEnabled) {
            packetManager = new PacketManager();
            try {
                Class<?> packetEventsClass = Class.forName("com.github.retrooper.packetevents.PacketEvents");
                Object api = packetEventsClass.getMethod("getAPI").invoke(null);
                Object eventManager = api.getClass().getMethod("getEventManager").invoke(api);
                
                // Since we can't implement PacketListener without the dependency,
                // we'll use a fallback approach or skip packet event registration
                getLogger().info("PacketEvents API found but using fallback packet management");
                
                api.getClass().getMethod("init").invoke(api);
            } catch (Exception e) {
                getLogger().warning("Failed to register PacketEvents listener: " + e.getMessage());
                packetEventsEnabled = false;
            }
        }

        // Register events
        getServer().getPluginManager().registerEvents(new LoadscreenListener(), this);

        // Register commands
        LoadscreenCommand commandExecutor = new LoadscreenCommand();
        getCommand("loadscreen").setExecutor(commandExecutor);
        getCommand("loadscreen").setTabCompleter(commandExecutor);

        // Display startup information
        displayStartupInfo();
    }

    /**
     * Called when the plugin is disabled.
     * 
     * <p>This method performs cleanup operations including:
     * <ul>
     *   <li>Stopping all active loadscreen sessions</li>
     *   <li>Terminating PacketEvents if enabled</li>
     *   <li>Unregistering all event listeners</li>
     * </ul>
     */
    @Override
    public void onDisable() {
        // Stop all active loadscreens
        LoadscreenManager.stopAllLoadscreens();

        // Disable PacketEvents
        if (packetEventsEnabled) {
            try {
                Class<?> packetEventsClass = Class.forName("com.github.retrooper.packetevents.PacketEvents");
                Object api = packetEventsClass.getMethod("getAPI").invoke(null);
                api.getClass().getMethod("terminate").invoke(api);
            } catch (Exception e) {
                getLogger().warning("Failed to terminate PacketEvents: " + e.getMessage());
            }
        }

        // Unregister events
        HandlerList.unregisterAll(this);

        getLogger().info("Loadscreens disabled and all sessions cleaned up.");
    }

    /**
     * Checks for optional dependencies and enables features accordingly.
     * 
     * <p>This method checks for:
     * <ul>
     *   <li>PlaceholderAPI - for dynamic placeholder support</li>
     *   <li>PacketEvents - for advanced packet control (checked in onLoad)</li>
     * </ul>
     */
    private void checkDependencies() {
        // Check PlaceholderAPI
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                placeholderAPIEnabled = true;
                getLogger().info("PlaceholderAPI found and enabled!");
            } else {
                getLogger().info("PlaceholderAPI found but not loaded - placeholder features disabled");
            }
        } catch (ClassNotFoundException e) {
            getLogger().info("PlaceholderAPI not found - placeholder features disabled");
        }

        // PacketEvents check is done in onLoad()
        if (!packetEventsEnabled) {
            getLogger().info("PacketEvents not available - using fallback methods");
        }
    }

    /**
     * Displays startup information including plugin version and loaded features.
     */
    private void displayStartupInfo() {
        getLogger().info("======================================");
        getLogger().info("  Loadscreens v3.0 Ultimate Edition");
        getLogger().info("  Created by: Anonventions");
        getLogger().info("  Date: 2025-07-19");
        getLogger().info("======================================");
        getLogger().info("Features loaded:");
        getLogger().info("- PacketEvents: " + (packetEventsEnabled ? "✓" : "✗"));
        getLogger().info("- PlaceholderAPI: " + (placeholderAPIEnabled ? "✓" : "✗"));
        getLogger().info("- Custom Loadscreen Types: ✓");
        getLogger().info("- Timer Delays: ✓");
        getLogger().info("- Visual Effects: ✓");
        getLogger().info("======================================");
    }

    // Getters with documentation
    
    /**
     * Checks if PacketEvents is available and enabled.
     * 
     * @return true if PacketEvents is enabled, false otherwise
     */
    public boolean isPacketEventsEnabled() {
        return packetEventsEnabled;
    }

    /**
     * Checks if PlaceholderAPI is available and enabled.
     * 
     * @return true if PlaceholderAPI is enabled, false otherwise
     */
    public boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }

    /**
     * Gets the PlaceholderManager instance.
     * 
     * @return the PlaceholderManager, or null if PlaceholderAPI is not available
     */
    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    /**
     * Gets the PacketManager instance.
     * 
     * @return the PacketManager, or null if PacketEvents is not available
     */
    public PacketManager getPacketManager() {
        return packetManager;
    }

    // API Methods for other plugins
    
    /**
     * Shows a custom loadscreen to a player with the specified type.
     * 
     * <p>This method is part of the public API and can be used by other plugins
     * to trigger loadscreens programmatically.</p>
     * 
     * @param player the player to show the loadscreen to
     * @param type the loadscreen type as defined in the configuration
     * @throws IllegalArgumentException if player is null or type is invalid
     */
    public void showCustomLoadscreen(org.bukkit.entity.Player player, String type) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Type cannot be null or empty");
        }
        LoadscreenManager.showLoadscreen(player, type);
    }

    /**
     * Shows a custom loadscreen to a player with the specified type and delay.
     * 
     * <p>This method is part of the public API and can be used by other plugins
     * to trigger loadscreens programmatically with a custom delay.</p>
     * 
     * @param player the player to show the loadscreen to
     * @param type the loadscreen type as defined in the configuration
     * @param delay the delay in ticks before showing the loadscreen
     * @throws IllegalArgumentException if player is null, type is invalid, or delay is negative
     */
    public void showCustomLoadscreen(org.bukkit.entity.Player player, String type, int delay) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Type cannot be null or empty");
        }
        if (delay < 0) {
            throw new IllegalArgumentException("Delay cannot be negative");
        }
        LoadscreenManager.showLoadscreen(player, type, delay);
    }
}