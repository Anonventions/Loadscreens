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

    public void blockPackets(Player player) {
        UUID uuid = player.getUniqueId();
        blockedPlayers.add(uuid);

        // Store original state BEFORE making any changes
        PlayerState originalState = new PlayerState(player);
        originalStates.put(uuid, originalState);

        // Apply complete lockdown
        makePlayerInvisible(player);
        hideCompleteUI(player);
        freezePlayerCompletely(player);
        startRotationLock(player); // NEW: Force rotation lock

        if (Loadscreens.getInstance().getConfig().getBoolean("global.debug", false)) {
            Loadscreens.getInstance().getLogger().info("Applied NUCLEAR lockdown to " + player.getName());
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
            Loadscreens.getInstance().getLogger().info("Restored " + player.getName() + " from NUCLEAR lockdown");
        }
    }

    private void startRotationLock(Player player) {
        // Get the target rotation from LoadscreenManager if active
        Location targetLocation = player.getLocation().clone();

        // Check if player has an active loadscreen session to get the exact target rotation
        if (org.anonventions.loadscreens.util.LoadscreenManager.hasActiveLoadscreen(player)) {
            // Try to get the configured look_yaw and look_pitch from the active session
            var config = Loadscreens.getInstance().getConfig();
            float targetYaw = (float) config.getDouble("loadscreen_types.join.look_yaw", 0.0);
            float targetPitch = (float) config.getDouble("loadscreen_types.join.look_pitch", 0.0);

            // Set the target rotation and position
            targetLocation.setYaw(targetYaw);
            targetLocation.setPitch(targetPitch);

            // IMPORTANT: Adjust camera position to eye level to prevent "in ground" view
            // Player eye level is typically 1.62 blocks above their feet
            Location cameraLocation = targetLocation.clone();
            cameraLocation.add(0, 1.62, 0); // Add eye level offset

            // Create a camera entity at the target location with the exact rotation
            TextDisplay cameraEntity = player.getWorld().spawn(cameraLocation, TextDisplay.class, entity -> {
                entity.text(net.kyori.adventure.text.Component.empty()); // Empty text
                entity.setInvisible(true);
                entity.setCustomNameVisible(false);
                entity.setGlowing(false);
                // Set the entity's rotation to our target rotation
                entity.setRotation(targetYaw, targetPitch);
            });

            // Store the camera entity for cleanup
            cameraEntities.put(player.getUniqueId(), cameraEntity);

            // Send camera packet to make player view through the camera entity
            try {
                // Use PacketEvents to send the camera packet
                com.github.retrooper.packetevents.PacketEvents.getAPI().getPlayerManager().sendPacket(player,
                    new WrapperPlayServerCamera(cameraEntity.getEntityId()));

                if (Loadscreens.getInstance().getConfig().getBoolean("global.debug", false)) {
                    Loadscreens.getInstance().getLogger().info("Set camera lock for " + player.getName() +
                        " to entity " + cameraEntity.getEntityId() + " at yaw: " + targetYaw + ", pitch: " + targetPitch);
                }
            } catch (Exception e) {
                if (Loadscreens.getInstance().getConfig().getBoolean("global.debug", false)) {
                    Loadscreens.getInstance().getLogger().warning("Failed to send camera packet for " + player.getName() + ": " + e.getMessage());
                }
                // Fallback to teleporting if camera packet fails
                player.teleport(targetLocation);
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

    private void hideCompleteUI(Player player) {
        // Apply blindness if enabled in config
        if (Loadscreens.getInstance().getConfig().getBoolean("packet_settings.apply_blindness_effect", true)) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.BLINDNESS,
                    Integer.MAX_VALUE,
                    0,
                    true,
                    false,
                    false
            ));
        }

        // Set to spectator mode for maximum UI hiding
        player.setGameMode(GameMode.SPECTATOR);
    }

    private void freezePlayerCompletely(Player player) {
        // Zero out all movement
        player.setWalkSpeed(0.0f);
        player.setFlySpeed(0.0f);

        // Add extremely strong effects
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOWNESS,
                Integer.MAX_VALUE,
                255,
                true,
                false,
                false
        ));

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.JUMP_BOOST,
                Integer.MAX_VALUE,
                250,
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
        // Restore camera view FIRST before other restoration
        try {
            // Send camera packet to restore normal view (using player's own entity ID)
            com.github.retrooper.packetevents.PacketEvents.getAPI().getPlayerManager().sendPacket(player,
                new WrapperPlayServerCamera(player.getEntityId()));

            if (Loadscreens.getInstance().getConfig().getBoolean("global.debug", false)) {
                Loadscreens.getInstance().getLogger().info("Restored normal camera view for " + player.getName());
            }
        } catch (Exception e) {
            if (Loadscreens.getInstance().getConfig().getBoolean("global.debug", false)) {
                Loadscreens.getInstance().getLogger().warning("Failed to restore camera for " + player.getName() + ": " + e.getMessage());
            }
        }

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
        // This fixes the invisible items issue when transitioning from spectator mode
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

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        PacketTypeCommon packetType = event.getPacketType();

        // Continue with existing packet blocking logic for active loadscreens
        if (!blockedPlayers.contains(player.getUniqueId())) return;

        // NUCLEAR OPTION: Block ALL rotation and movement packets
        if (packetType == PacketType.Play.Client.PLAYER_ROTATION ||
                packetType == PacketType.Play.Client.PLAYER_POSITION ||
                packetType == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION ||
                packetType == PacketType.Play.Client.PLAYER_FLYING ||
                packetType == PacketType.Play.Client.PLAYER_ABILITIES ||
                packetType == PacketType.Play.Client.PLAYER_INPUT ||
                packetType == PacketType.Play.Client.VEHICLE_MOVE ||
                packetType == PacketType.Play.Client.STEER_VEHICLE) {

            // Log for debugging - show ALL blocked packets
            if (Loadscreens.getInstance().getConfig().getBoolean("global.debug", false)) {
                Loadscreens.getInstance().getLogger().info("BLOCKED " + packetType + " from " + player.getName());
            }

            event.setCancelled(true);
            return;
        }

        // Log ANY packet that's NOT being blocked for debugging
        if (Loadscreens.getInstance().getConfig().getBoolean("global.debug", false)) {
            // Only log movement/rotation related packets that we're NOT blocking
            String packetName = packetType.toString();
            if (packetName.contains("PLAYER") || packetName.contains("POSITION") ||
                packetName.contains("ROTATION") || packetName.contains("LOOK") ||
                packetName.contains("MOVE") || packetName.contains("HEAD")) {
                Loadscreens.getInstance().getLogger().warning("UNBLOCKED PACKET: " + packetType + " from " + player.getName());
            }
        }

        // Block ALL other input packets
        if (packetType == PacketType.Play.Client.INTERACT_ENTITY ||
                packetType == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT ||
                packetType == PacketType.Play.Client.PLAYER_DIGGING ||
                packetType == PacketType.Play.Client.USE_ITEM ||
                packetType == PacketType.Play.Client.CLICK_WINDOW ||
                packetType == PacketType.Play.Client.CREATIVE_INVENTORY_ACTION ||
                packetType == PacketType.Play.Client.CLOSE_WINDOW ||
                packetType == PacketType.Play.Client.HELD_ITEM_CHANGE ||
                packetType == PacketType.Play.Client.CHAT_MESSAGE) {

            event.setCancelled(true);
            return;
        }

        // Only allow specific admin commands
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

        // Block everything else as fallback
        event.setCancelled(true);
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
