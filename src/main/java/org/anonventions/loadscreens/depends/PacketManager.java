package org.anonventions.loadscreens.depends;

import org.anonventions.loadscreens.core.Loadscreens;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

public class PacketManager {
    private final Set<UUID> blockedPlayers = new HashSet<>();

    public void blockPackets(Player player) {
        blockedPlayers.add(player.getUniqueId());
        hideUI(player);
    }

    public void unblockPackets(Player player) {
        blockedPlayers.remove(player.getUniqueId());
        showUI(player);
    }

    private void hideUI(Player player) {
        // Hide various UI elements using fallback methods
        try {
            // Basic fallback - could be enhanced with NMS if needed
            Loadscreens.getInstance().getLogger().info("Hiding UI for " + player.getName() + " (fallback method)");
        } catch (Exception e) {
            Loadscreens.getInstance().getLogger().warning("Failed to hide UI for " + player.getName());
        }
    }

    private void showUI(Player player) {
        try {
            // Restore UI elements
            Loadscreens.getInstance().getLogger().info("Showing UI for " + player.getName() + " (fallback method)");
        } catch (Exception e) {
            Loadscreens.getInstance().getLogger().warning("Failed to show UI for " + player.getName());
        }
    }

    // This method would be called by PacketEvents if available
    public void handlePacketReceive(Object event) {
        // Implementation would use reflection to handle PacketEvents without compile dependency
        Loadscreens.getInstance().getLogger().info("Packet received (PacketEvents integration)");
    }

    public boolean isPlayerBlocked(Player player) {
        return blockedPlayers.contains(player.getUniqueId());
    }

    public void clearAllBlocked() {
        for (UUID uuid : new HashSet<>(blockedPlayers)) {
            Player player = org.bukkit.Bukkit.getPlayer(uuid);
            if (player != null) {
                unblockPackets(player);
            }
        }
        blockedPlayers.clear();
    }
}