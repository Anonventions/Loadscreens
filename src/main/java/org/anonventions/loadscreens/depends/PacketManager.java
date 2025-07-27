package org.anonventions.loadscreens.depends;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.*;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import org.anonventions.loadscreens.core.Loadscreens;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

public class PacketManager implements PacketListener {
    private final Set<UUID> blockedPlayers = new HashSet<>();
    private final Map<UUID, PlayerState> originalStates = new HashMap<>();
    private final Map<UUID, TextDisplay> cameraEntities = new HashMap<>(); // Track camera entities
    private final Map<UUID, BukkitRunnable> rotationLockTasks = new HashMap<>(); // Keep this for cleanup
    private final Set<UUID> playersBeingRestored = new HashSet<>(); // Track players being restored
    
    // Connection health monitoring to prevent timeouts
    private final Map<UUID, Long> lastKeepAlive = new HashMap<>();
    private final Map<UUID, Integer> packetBlockCount = new HashMap<>();
    private final long MAX_PACKET_BLOCKS_PER_SECOND = 20; // Configurable limit
    private final long KEEPALIVE_TIMEOUT_MS = 30000; // 30 seconds

    public void blockPackets(Player player) {
        UUID uuid = player.getUniqueId();
        blockedPlayers.add(uuid);

        // Store original state BEFORE making any changes
        PlayerState originalState = new PlayerState(player);
        originalStates.put(uuid, originalState);

        // Initialize connection health monitoring
        lastKeepAlive.put(uuid, System.currentTimeMillis());
        packetBlockCount.put(uuid, 0);

        // Apply optimized lockdown (less aggressive to prevent timeouts)
        makePlayerInvisible(player);
        hideSelectiveUI(player); // Less aggressive UI hiding
        freezePlayerOptimized(player); // Optimized freezing
        startOptimizedRotationLock(player); // Less aggressive rotation lock

        if (Loadscreens.getInstance().getConfig().getBoolean("global.debug", false)) {
            Loadscreens.getInstance().getLogger().info("Applied OPTIMIZED lockdown to " + player.getName() + " (timeout prevention enabled)");
        }
    }

    public void unblockPackets(Player player) {
        UUID uuid = player.getUniqueId();
        blockedPlayers.remove(uuid);

        // Stop rotation lock task
        BukkitRunnable rotationTask = rotationLockTasks.remove(uuid);
        if (rotationTask != null) {
            rotationTask.cancel();
        }

        // Clean up connection monitoring
        lastKeepAlive.remove(uuid);
        packetBlockCount.remove(uuid);

        // Restore original state
        PlayerState originalState = originalStates.remove(uuid);
        if (originalState != null) {
            restorePlayerState(player, originalState);
        }

        // Remove camera entity if it exists
        TextDisplay cameraEntity = cameraEntities.remove(uuid);
        if (cameraEntity != null) {
            cameraEntity.remove();
        }

        if (Loadscreens.getInstance().getConfig().getBoolean("global.debug", false)) {
            Loadscreens.getInstance().getLogger().info("Restored " + player.getName() + " from OPTIMIZED lockdown");
        }
    }

    private void startOptimizedRotationLock(Player player) {
        // TIMEOUT FIX: Use much less aggressive rotation locking
        Location targetLocation = player.getLocation().clone();

        // Check if player has an active loadscreen session to get the exact target rotation
        if (org.anonventions.loadscreens.util.LoadscreenManager.hasActiveLoadscreen(player)) {
            var config = Loadscreens.getInstance().getConfig();
            float targetYaw = (float) config.getDouble("loadscreen_types.join.look_yaw", 0.0);
            float targetPitch = (float) config.getDouble("loadscreen_types.join.look_pitch", 0.0);

            // Set the target rotation and position
            targetLocation.setYaw(targetYaw);
            targetLocation.setPitch(targetPitch);

            // TIMEOUT FIX: Only set position once, don't use camera entity (less aggressive)
            player.teleport(targetLocation);

            if (Loadscreens.getInstance().getConfig().getBoolean("global.debug", false)) {
                Loadscreens.getInstance().getLogger().info("Set OPTIMIZED rotation lock for " + player.getName() +
                    " to yaw: " + targetYaw + ", pitch: " + targetPitch + " (no camera entity to prevent timeout)");
            }
        }
    }

    private org.bukkit.util.Vector getDirectionFromYawPitch(float yaw, float pitch) {
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);

        double x = -Math.sin(yawRad) * Math.cos(pitchRad);
        double y = -Math.sin(pitchRad);
        double z = Math.cos(yawRad) * Math.cos(pitchRad);

        return new org.bukkit.util.Vector(x, y, z);
    }

    private void makePlayerInvisible(Player player) {
        // Make player invisible to others
        for (Player other : player.getServer().getOnlinePlayers()) {
            if (!other.equals(player)) {
                other.hidePlayer(Loadscreens.getInstance(), player);
            }
        }

        // Apply invisibility effect
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.INVISIBILITY,
                Integer.MAX_VALUE,
                0,
                true,
                false,
                false
        ));
    }

    private void hideSelectiveUI(Player player) {
        // TIMEOUT FIX: Don't apply blindness as it can cause issues with packet handling
        // Apply only necessary effects for UI hiding without causing timeouts
        
        // Use adventure mode instead of spectator to reduce timeout risk
        player.setGameMode(GameMode.ADVENTURE);
    }

    private void freezePlayerOptimized(Player player) {
        // TIMEOUT FIX: Less aggressive movement restrictions
        player.setWalkSpeed(0.0f);
        player.setFlySpeed(0.0f);

        // Apply moderate effects (not maximum intensity to prevent timeout)
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOWNESS,
                Integer.MAX_VALUE,
                10, // Reduced from 255 to prevent timeout
                true,
                false,
                false
        ));

        // Don't apply extreme jump boost as it can cause packet issues
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.JUMP_BOOST,
                Integer.MAX_VALUE,
                250, // Negative jump boost to prevent jumping
                true,
                false,
                false
        ));

        // Prevent flying
        if (player.isFlying()) {
            player.setFlying(false);
        }
        player.setAllowFlight(false);
    }

    private void restorePlayerState(Player player, PlayerState originalState) {
        // TIMEOUT FIX: Restore camera view FIRST before other restoration (no camera entity to restore now)
        // Since we're not using camera entities anymore, just ensure normal view
        
        // Remove ALL effects
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);

        // Restore gamemode FIRST
        player.setGameMode(originalState.gameMode);

        // THEN restore speeds
        player.setWalkSpeed(originalState.walkSpeed);
        player.setFlySpeed(originalState.flySpeed);
        player.setAllowFlight(originalState.allowFlight);

        if (originalState.wasFlying && originalState.allowFlight) {
            player.setFlying(true);
        }

        // Make player visible again
        for (Player other : player.getServer().getOnlinePlayers()) {
            if (!other.equals(player)) {
                other.showPlayer(Loadscreens.getInstance(), player);
            }
        }

        // Clear velocity
        player.setVelocity(new org.bukkit.util.Vector(0, 0, 0));

        // CRITICAL FIX: Force inventory refresh after gamemode change
        // This fixes the invisible items issue when transitioning from adventure mode
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    // Force update the player's inventory
                    player.updateInventory();

                    // Also force update held item slot
                    org.bukkit.inventory.ItemStack heldItem = player.getInventory().getItemInMainHand();
                    if (heldItem != null) {
                        // Force the client to see the held item
                        player.getInventory().setItemInMainHand(heldItem);
                    }

                    // Force armor update
                    org.bukkit.inventory.ItemStack[] armor = player.getInventory().getArmorContents();
                    if (armor != null) {
                        player.getInventory().setArmorContents(armor);
                    }
                }
            }
        }.runTaskLater(Loadscreens.getInstance(), 2L); // Small delay to ensure gamemode change is complete

        // Allow inventory packets during restoration
        playersBeingRestored.add(player.getUniqueId());
        new BukkitRunnable() {
            @Override
            public void run() {
                playersBeingRestored.remove(player.getUniqueId());
            }
        }.runTaskLater(Loadscreens.getInstance(), 5L); // Adjust delay as needed
    }

    // Helper method to check if we should allow packet (timeout prevention)
    private boolean shouldAllowPacket(Player player, PacketTypeCommon packetType) {
        UUID uuid = player.getUniqueId();
        
        // Update last keepalive time for any packet (connection health)
        lastKeepAlive.put(uuid, System.currentTimeMillis());
        
        // Check packet block rate to prevent timeout
        int currentBlocks = packetBlockCount.getOrDefault(uuid, 0);
        long currentTime = System.currentTimeMillis();
        
        // Reset counter every second
        if (currentTime - lastKeepAlive.getOrDefault(uuid, 0L) > 1000) {
            packetBlockCount.put(uuid, 0);
            currentBlocks = 0;
        }
        
        // If we're blocking too many packets, allow this one to prevent timeout
        if (currentBlocks >= MAX_PACKET_BLOCKS_PER_SECOND) {
            if (Loadscreens.getInstance().getConfig().getBoolean("global.debug", false)) {
                Loadscreens.getInstance().getLogger().warning("Allowing packet " + packetType + 
                    " for " + player.getName() + " to prevent timeout (blocked " + currentBlocks + " this second)");
            }
            return true;
        }
        
        return false;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        PacketTypeCommon packetType = event.getPacketType();

        // Continue with existing packet blocking logic for active loadscreens
        if (!blockedPlayers.contains(player.getUniqueId())) return;

        // TIMEOUT PREVENTION: Check if we should allow this packet
        if (shouldAllowPacket(player, packetType)) {
            return; // Allow packet to prevent timeout
        }

        // Block selective movement packets (not ALL to prevent timeout)
        if (packetType == PacketType.Play.Client.PLAYER_ROTATION ||
                packetType == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {

            // Increment block counter
            UUID uuid = player.getUniqueId();
            packetBlockCount.put(uuid, packetBlockCount.getOrDefault(uuid, 0) + 1);

            // Log for debugging - show blocked packets
            if (Loadscreens.getInstance().getConfig().getBoolean("global.debug", false)) {
                Loadscreens.getInstance().getLogger().info("BLOCKED " + packetType + " from " + player.getName());
            }

            event.setCancelled(true);
            return;
        }

        // ALLOW player position and flying packets to maintain connection
        if (packetType == PacketType.Play.Client.PLAYER_POSITION ||
                packetType == PacketType.Play.Client.PLAYER_FLYING) {
            // Allow these to prevent timeout, just log if debugging
            if (Loadscreens.getInstance().getConfig().getBoolean("global.debug", false)) {
                Loadscreens.getInstance().getLogger().info("ALLOWED " + packetType + " from " + player.getName() + " (timeout prevention)");
            }
            return;
        }

        // Block interaction packets but allow movement for connection health
        if (packetType == PacketType.Play.Client.INTERACT_ENTITY ||
                packetType == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT ||
                packetType == PacketType.Play.Client.PLAYER_DIGGING ||
                packetType == PacketType.Play.Client.USE_ITEM ||
                packetType == PacketType.Play.Client.CLICK_WINDOW ||
                packetType == PacketType.Play.Client.CREATIVE_INVENTORY_ACTION ||
                packetType == PacketType.Play.Client.CLOSE_WINDOW ||
                packetType == PacketType.Play.Client.HELD_ITEM_CHANGE) {

            event.setCancelled(true);
            return;
        }

        // Allow chat commands for admin control
        if (packetType == PacketType.Play.Client.CHAT_COMMAND) {
            if (player.hasPermission("loadscreens.admin")) {
                try {
                    WrapperPlayClientChatCommand wrapper = new WrapperPlayClientChatCommand(event);
                    String command = wrapper.getCommand().toLowerCase();
                    if (command.startsWith("loadscreen")) {
                        return; // Allow this command
                    }
                } catch (Exception e) {
                    // If we can't parse it, block it
                }
            }
            event.setCancelled(true);
            return;
        }

        // Block chat but allow other packets for connection health
        if (packetType == PacketType.Play.Client.CHAT_MESSAGE) {
            event.setCancelled(true);
            return;
        }
        
        // CRITICAL TIMEOUT FIX: Don't block everything else - allow unknown packets
        // This prevents timeout by allowing keepalive and other essential packets
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        if (!blockedPlayers.contains(player.getUniqueId())) return;

        PacketTypeCommon packetType = event.getPacketType();

        // Block ALL UI-related packets aggressively
        if (packetType == PacketType.Play.Server.SET_SLOT ||
                packetType == PacketType.Play.Server.WINDOW_ITEMS ||
                packetType == PacketType.Play.Server.SET_EXPERIENCE ||
                packetType == PacketType.Play.Server.UPDATE_HEALTH ||
                packetType == PacketType.Play.Server.BOSS_BAR ||
                packetType == PacketType.Play.Server.DISPLAY_SCOREBOARD ||
                packetType == PacketType.Play.Server.SCOREBOARD_OBJECTIVE ||
                packetType == PacketType.Play.Server.UPDATE_SCORE ||
                packetType == PacketType.Play.Server.PLAYER_INFO_UPDATE ||
                packetType == PacketType.Play.Server.PLAYER_INFO_REMOVE ||
                packetType == PacketType.Play.Server.SYSTEM_CHAT_MESSAGE) {

            event.setCancelled(true);
        }
    }

    public boolean isPlayerBlocked(Player player) {
        return blockedPlayers.contains(player.getUniqueId());
    }

    public void clearAllBlocked() {
        // Cancel all rotation lock tasks
        for (BukkitRunnable task : rotationLockTasks.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        rotationLockTasks.clear();

        // Unblock all players
        for (UUID uuid : new HashSet<>(blockedPlayers)) {
            Player player = org.bukkit.Bukkit.getPlayer(uuid);
            if (player != null) {
                unblockPackets(player);
            }
        }
        blockedPlayers.clear();
        originalStates.clear();
        
        // Clean up connection monitoring
        lastKeepAlive.clear();
        packetBlockCount.clear();
    }

    // Enhanced PlayerState class
    private static class PlayerState {
        final float walkSpeed;
        final float flySpeed;
        final boolean allowFlight;
        final boolean wasFlying;
        final GameMode gameMode;

        PlayerState(Player player) {
            this.walkSpeed = player.getWalkSpeed();
            this.flySpeed = player.getFlySpeed();
            this.allowFlight = player.getAllowFlight();
            this.wasFlying = player.isFlying();
            this.gameMode = player.getGameMode();
        }
    }
}
