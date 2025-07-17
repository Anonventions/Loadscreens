package org.anonventions.loadscreens;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import me.clip.placeholderapi.PlaceholderAPI;
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
        // Initialize PacketEvents
        if (getConfig().getBoolean("global.use_packetevents", true)) {
            try {
                PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
                PacketEvents.getAPI().load();
                packetEventsEnabled = true;
                getLogger().info("PacketEvents initialized successfully!");
            } catch (Exception e) {
                getLogger().warning("Failed to initialize PacketEvents: " + e.getMessage());
                packetEventsEnabled = false;
            }
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
            PacketEvents.getAPI().getEventManager().registerListener(packetManager, PacketListenerPriority.HIGH);
            PacketEvents.getAPI().init();
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
            PacketEvents.getAPI().terminate();
        }

        // Unregister events
        HandlerList.unregisterAll(this);

        getLogger().info("Loadscreens disabled and all sessions cleaned up.");
    }

    private void checkDependencies() {
        // Check PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderAPIEnabled = true;
            getLogger().info("PlaceholderAPI found and enabled!");
        } else {
            getLogger().warning("PlaceholderAPI not found - placeholder features disabled");
        }

        // PacketEvents check is done in onLoad()
        if (!packetEventsEnabled) {
            getLogger().warning("PacketEvents not available - using fallback methods");
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