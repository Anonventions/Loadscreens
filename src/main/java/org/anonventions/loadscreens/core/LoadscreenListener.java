package org.anonventions.loadscreens;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.Location;

public class LoadscreenListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        var config = Loadscreens.getInstance().getConfig();
        if (!config.getBoolean("global.enabled")) return;

        if (config.getBoolean("loadscreen_types.join.show_on_join", true)) {
            LoadscreenManager.showLoadscreen(event.getPlayer(), "join");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        var config = Loadscreens.getInstance().getConfig();
        if (!config.getBoolean("global.enabled")) return;

        // Clean up any active loadscreen
        LoadscreenManager.stopLoadscreen(event.getPlayer());

        // Show leave loadscreen to other players if enabled
        if (config.getBoolean("loadscreen_types.leave.enabled", false) &&
                config.getBoolean("loadscreen_types.leave.show_to_others", false)) {

            double range = config.getDouble("loadscreen_types.leave.show_range", 50.0);
            Location playerLoc = event.getPlayer().getLocation();

            for (org.bukkit.entity.Player other : event.getPlayer().getWorld().getPlayers()) {
                if (!other.equals(event.getPlayer()) &&
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
            LoadscreenManager.showLoadscreen(event.getPlayer(), "world_change");
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
                org.bukkit.Bukkit.getScheduler().runTaskLater(
                        Loadscreens.getInstance(),
                        () -> LoadscreenManager.showLoadscreen(event.getPlayer(), "world_change"),
                        2L
                );
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        var config = Loadscreens.getInstance().getConfig();
        if (!config.getBoolean("global.enabled")) return;

        if (config.getBoolean("loadscreen_types.respawn.show_on_respawn", false)) {
            LoadscreenManager.showLoadscreen(event.getPlayer(), "respawn");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Stop any active loadscreen when player dies
        LoadscreenManager.stopLoadscreen(event.getEntity());
    }
}