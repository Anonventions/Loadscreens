package org.anonventions.loadscreens;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.*;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

public class PacketManager implements PacketListener {
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
        // Hide various UI elements using packets
        try {
            // Hide hotbar, health, food, etc.
            // Implementation depends on your specific needs
            player.setHealthScale(0.1); // Temporary workaround
        } catch (Exception e) {
            Loadscreens.getInstance().getLogger().warning("Failed to hide UI for " + player.getName());
        }
    }

    private void showUI(Player player) {
        try {
            // Restore UI elements
            player.setHealthScale(20.0);
        } catch (Exception e) {
            Loadscreens.getInstance().getLogger().warning("Failed to show UI for " + player.getName());
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        if (!blockedPlayers.contains(player.getUniqueId())) return;

        var config = Loadscreens.getInstance().getConfig();

        // Block movement packets
        if (config.getBoolean("packet_settings.block_movement_packets", true)) {
            if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION ||
                    event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION ||
                    event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION) {
                event.setCancelled(true);
                return;
            }
        }

        // Block interaction packets
        if (config.getBoolean("packet_settings.block_interaction_packets", true)) {
            if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY ||
                    event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT ||
                    event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
                event.setCancelled(true);
                return;
            }
        }

        // Block inventory packets
        if (config.getBoolean("packet_settings.block_inventory_packets", true)) {
            if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW ||
                    event.getPacketType() == PacketType.Play.Client.CREATIVE_INVENTORY_ACTION) {
                event.setCancelled(true);
                return;
            }
        }

        // Block chat packets
        if (config.getBoolean("packet_settings.block_chat_packets", true)) {
            if (event.getPacketType() == PacketType.Play.Client.CHAT_MESSAGE) {
                event.setCancelled(true);
                return;
            }
        }

        // Block command packets (optional)
        if (config.getBoolean("packet_settings.block_command_packets", false)) {
            if (event.getPacketType() == PacketType.Play.Client.CHAT_COMMAND) {
                // Allow admin commands
                if (player.hasPermission("loadscreens.admin")) {
                    WrapperPlayClientChatCommand wrapper = new WrapperPlayClientChatCommand(event);
                    String command = wrapper.getCommand().toLowerCase();
                    if (command.startsWith("loadscreen")) {
                        return; // Don't block loadscreen commands
                    }
                }
                event.setCancelled(true);
            }
        }
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