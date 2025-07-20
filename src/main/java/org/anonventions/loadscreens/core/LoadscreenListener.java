package org.anonventions.loadscreens.core;

import org.anonventions.loadscreens.util.LoadscreenManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

public class LoadscreenListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        var config = Loadscreens.getInstance().getConfig();
        if (!config.getBoolean("global.enabled")) return;

        if (config.getBoolean("loadscreen_types.join.show_on_join", true)) {
            // Let LoadscreenManager handle the timer_delay from config
            LoadscreenManager.showLoadscreen(event.getPlayer(), "join");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        var config = Loadscreens.getInstance().getConfig();
        if (!config.getBoolean("global.enabled")) return;

        // Check if leave loadscreen is enabled
        if (config.getBoolean("loadscreen_types.leave.enabled", false)) {
            // Show leave loadscreen to the disconnecting player
            // Note: We can't cancel PlayerQuitEvent, so we show it immediately
            LoadscreenManager.showLoadscreen(event.getPlayer(), "leave");

            if (config.getBoolean("global.debug", false)) {
                Loadscreens.getInstance().getLogger().info("Showing leave loadscreen to " + event.getPlayer().getName() + " during disconnect");
            }
        }

        // Handle normal disconnect cleanup
        handleNormalDisconnect(event.getPlayer());
    }

    private void handleNormalDisconnect(Player player) {
        var config = Loadscreens.getInstance().getConfig();

        // Clean up any active loadscreen (except the leave one we just started)
        // Note: Don't stop the leave loadscreen we just started

        // Show leave loadscreen to other players if enabled
        if (config.getBoolean("loadscreen_types.leave.show_to_others", false)) {
            double range = config.getDouble("loadscreen_types.leave.show_range", 50.0);
            Location playerLoc = player.getLocation();

            for (org.bukkit.entity.Player other : player.getWorld().getPlayers()) {
                if (!other.equals(player) &&
                        other.getLocation().distance(playerLoc) <= range) {
                    LoadscreenManager.showLoadscreen(other, "leave");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        var config = Loadscreens.getInstance().getConfig();
        if (!config.getBoolean("global.enabled")) return;

        if (config.getBoolean("loadscreen_types.world_change.show_on_world_change", false)) {
            // Small delay to let world change settle
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (event.getPlayer().isOnline()) {
                        LoadscreenManager.showLoadscreen(event.getPlayer(), "world_change");
                    }
                }
            }.runTaskLater(Loadscreens.getInstance(), 3L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        var config = Loadscreens.getInstance().getConfig();
        if (!config.getBoolean("global.enabled")) return;

        if (config.getBoolean("loadscreen_types.world_change.show_on_teleport", false)) {
            // Check minimum distance
            double minDistance = config.getDouble("loadscreen_types.world_change.min_teleport_distance", 100.0);

            if (event.getFrom().distance(event.getTo()) >= minDistance) {
                // Small delay to let teleport complete
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (event.getPlayer().isOnline()) {
                            LoadscreenManager.showLoadscreen(event.getPlayer(), "world_change");
                        }
                    }
                }.runTaskLater(Loadscreens.getInstance(), 2L);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        var config = Loadscreens.getInstance().getConfig();
        if (!config.getBoolean("global.enabled")) return;

        if (config.getBoolean("loadscreen_types.respawn.show_on_respawn", false)) {
            // Delay to let respawn settle
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (event.getPlayer().isOnline()) {
                        LoadscreenManager.showLoadscreen(event.getPlayer(), "respawn");
                    }
                }
            }.runTaskLater(Loadscreens.getInstance(), 10L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Stop any active loadscreen when player dies
        LoadscreenManager.stopLoadscreen(event.getEntity());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerKick(PlayerKickEvent event) {
        // Clean up loadscreen when player is kicked
        LoadscreenManager.stopLoadscreen(event.getPlayer());
    }
}