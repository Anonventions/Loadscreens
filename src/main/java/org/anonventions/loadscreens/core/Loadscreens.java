package org.anonventions.loadscreens.core;

import org.anonventions.loadscreens.command.LoadscreenCommand;
import org.anonventions.loadscreens.depends.PacketManager;
import org.anonventions.loadscreens.depends.PlaceholderManager;
import org.anonventions.loadscreens.util.LoadscreenManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.HandlerList;
import org.bukkit.Bukkit;

public class Loadscreens extends JavaPlugin {
    private static Loadscreens instance;
    private boolean packetEventsEnabled = false;
    private boolean placeholderAPIEnabled = false;
    private PlaceholderManager placeholderManager;
    private PacketManager packetManager;

    public static Loadscreens getInstance() {
        return instance;
    }

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
        getCommand("loadscreen").setExecutor(new LoadscreenCommand());
        getCommand("loadscreen").setTabCompleter(new LoadscreenCommand());

        // Metrics and info
        getLogger().info("======================================");
        getLogger().info("  Loadscreens v3.0 Ultimate Edition");
        getLogger().info("  Created by: Anonventions");
        getLogger().info("  Date: 2025-07-16 08:27:08 UTC");
        getLogger().info("======================================");
        getLogger().info("Features loaded:");
        getLogger().info("- PacketEvents: " + (packetEventsEnabled ? "✓" : "✗"));
        getLogger().info("- PlaceholderAPI: " + (placeholderAPIEnabled ? "✓" : "✗"));
        getLogger().info("- Custom Loadscreen Types: ✓");
        getLogger().info("- Timer Delays: ✓");
        getLogger().info("- Visual Effects: ✓");
        getLogger().info("======================================");
    }

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

    // Getters
    public boolean isPacketEventsEnabled() {
        return packetEventsEnabled;
    }

    public boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }

    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    public PacketManager getPacketManager() {
        return packetManager;
    }

    // API Methods for other plugins
    public void showCustomLoadscreen(org.bukkit.entity.Player player, String type) {
        LoadscreenManager.showLoadscreen(player, type);
    }

    public void showCustomLoadscreen(org.bukkit.entity.Player player, String type, int delay) {
        LoadscreenManager.showLoadscreen(player, type, delay);
    }
}